/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Computes the size and position of views: port of
/// `jsesh.render.layout.Layout` with `QuadratLayout`, `LineLayout`,
/// `ColumnLayout` and `LineAccumulator`, and of `jsesh.render.view.ViewBuilder`.
///
/// The code follows Java closely (names included), so that the two can be
/// compared view by view. `Math.fround` marks Java `float` computations.

import type { KerningAxis, TextDirection, TextOrientation } from "../model/constants.js";
import { isHorizontalInsertion, isStartInsertion, isTopInsertion, isVerticalInsertion, SymbolCodes } from "../model/constants.js";
import {
  AbsoluteGroup,
  AlphabeticCharacter,
  Cartouche,
  HorizontalListElement,
  ModelElementAdapter,
  type BasicItemList,
  type Cadrat,
  type ComplexLigature,
  type HBox,
  type HieroglyphicText,
  type Hieroglyph,
  type HRule,
  type Insert,
  type Kerning,
  type Ligature,
  type LineBreak,
  type MdcComment,
  type ModelElement,
  type Modifier,
  type ModifiersList,
  type Overwrite,
  type PageBreak,
  type Philology,
  type SubCadrat,
  type Superscript,
  type TabStop,
  Zone,
  ZoneContent,
  ZONE_UNSET,
  type ZoneParameters,
} from "../model/model.js";
import { HieroglyphDrawer, scaleZone, type LigatureZone } from "./glyphs.js";
import {
  enlargedFrame,
  fitInsertion,
  placedBox,
  type HorizontalSide,
  type VerticalSide,
} from "./insert/insertion-fitter.js";
import { Kerner } from "./insert/kerner.js";
import { ViewSilhouette } from "./insert/view-silhouette.js";
import {
  computeCartouchePartLength,
  computeCartoucheSecondaryLength,
  isWrapping,
  philologyWidth,
  withStyle,
  type JSeshStyle,
} from "./style.js";
import { hasKerning, kerningOf, KerningPacker } from "./kerning-packer.js";
import { displayString, TextRun, type TextMeasurer } from "./text.js";
import { MDCView, Rect, type Point } from "./view.js";

const f = Math.fround;

/// Everything the layout needs.
export interface LayoutContext {
  style: JSeshStyle;
  drawer: HieroglyphDrawer;
  measurer: TextMeasurer;
}

/// Builds the view tree of a model element (port of `ViewBuilder`). With
/// `start` and `end`, only these children of the element are laid out.
export function buildView(element: ModelElement, context: LayoutContext, start = 0, end = element.numberOfChildren): MDCView {
  const layout = new Layout(context);
  let topLevel = true;
  let currentView: MDCView | null = null;

  const builder = new (class extends ModelElementAdapter {
    override visitModifier(_mod: Modifier): void {
      throw new Error("this method shouldn't be called");
    }

    override visitModifiersList(_l: ModifiersList): void {
      throw new Error("this method shouldn't be called");
    }

    override visitDefault(t: ModelElement): void {
      const result = new MDCView(t);
      layout.preLayoutHook(result);
      const [a, b] = topLevel ? [start, end] : [0, t.numberOfChildren];
      topLevel = false;
      for (let i = a; i < b; i++) {
        t.getChildAt(i).accept(this);
        result.add(currentView!);
      }
      layout.layout(result);
      layout.postLayoutHook(result);
      currentView = result;
    }

    override visitHieroglyph(h: Hieroglyph): void {
      const result = new MDCView(h);
      layout.preLayoutHook(result);
      layout.layout(result);
      currentView = result;
    }
  })();
  element.accept(builder);
  return currentView!;
}

/// The style in which the content of a zone is laid out (`Layout.zoneRenderContext`).
///
/// Settings which the zone doesn't set are inherited from the enclosing
/// style. A zone has no margins, and is never paged (a page break in a zone
/// is a line break).
///
/// A zone in lines wraps at its width, if it has one. It never inherits the
/// wrapping width of the enclosing text.
export function zoneStyle(parent: JSeshStyle, p: ZoneParameters): JSeshStyle {
  const unit = zoneLengthUnit(parent);
  const orientation = p.orientation ?? parent.options.textOrientation;
  const geometry: Partial<JSeshStyle["geometry"]> = {
    leftMargin: 0,
    topMargin: 0,
    wrapWidth: orientation === "HORIZONTAL" && p.width !== ZONE_UNSET ? f(p.width * unit) : 0,
  };
  if (p.width !== ZONE_UNSET) geometry.textWidth = f(p.width * unit);
  if (p.lineSkip !== ZONE_UNSET) geometry.lineSkip = f(p.lineSkip * unit);
  if (p.columnSkip !== ZONE_UNSET) geometry.columnSkip = f(p.columnSkip * unit);
  if (p.quadratWidth !== ZONE_UNSET) geometry.maxCadratWidth = f(p.quadratWidth * unit);
  if (p.quadratHeight !== ZONE_UNSET) geometry.maxCadratHeight = f(p.quadratHeight * unit);
  return withStyle(parent, {
    options: {
      textOrientation: orientation,
      textDirection: p.direction ?? parent.options.textDirection,
      paged: false,
    },
    geometry,
  });
}

/// The unit of the lengths of [ZoneParameters]: one percent of the height
/// of a quadrat.
export function zoneLengthUnit(style: JSeshStyle): number {
  return f(style.geometry.maxCadratHeight / 100);
}

/// The space between the frame of a zone and its content, frame included.
export function zoneFramePadding(style: JSeshStyle): number {
  return f(style.geometry.cartoucheMargin + style.geometry.cartoucheLineWidth);
}

/// Port of `Layout`: computes the size of a view, and the position and scale
/// of its subviews (which are already laid out).
class Layout {
  private currentView!: MDCView;
  /// True if small signs are centered (it changes in cartouches).
  private centerSigns: boolean;
  private currentTextOrientation: TextOrientation;
  private currentTextDirection: TextDirection;
  private inAbsoluteGroup = false;
  private style: JSeshStyle;
  /// The styles of the enclosing zones, saved while the content of a zone is
  /// laid out with its own style (see [preLayoutHook]).
  private readonly savedZoneStyles: JSeshStyle[] = [];
  private readonly hieroglyphsDrawer: HieroglyphDrawer;
  private readonly measurer: TextMeasurer;
  private readonly aux = new LayoutAux(this);

  constructor(context: LayoutContext) {
    this.style = context.style;
    this.hieroglyphsDrawer = context.drawer;
    this.measurer = context.measurer;
    this.currentTextOrientation = this.style.options.textOrientation;
    this.currentTextDirection = this.style.options.textDirection;
    this.centerSigns = this.style.options.smallSignCentered;
  }

  layout(view: MDCView): void {
    this.currentView = view;
    view.direction = this.currentTextDirection;
    view.orientation = this.currentTextOrientation;
    view.model.accept(this.aux);
  }

  /// Called before the subviews are built.
  preLayoutHook(view: MDCView): void {
    if (view.model instanceof Cartouche) this.centerSigns = true;
    else if (view.model instanceof AbsoluteGroup) this.inAbsoluteGroup = true;
    else if (view.model instanceof ZoneContent && view.model.parent instanceof Zone) {
      // The content of a zone, and everything in it, is laid out with the
      // zone's own settings, until postLayoutHook.
      this.savedZoneStyles.push(this.style);
      this.setStyle(zoneStyle(this.style, view.model.parent.parameters));
    }
  }

  /// Called once the view is laid out.
  postLayoutHook(view: MDCView): void {
    if (view.model instanceof ZoneContent && this.savedZoneStyles.length > 0) {
      this.setStyle(this.savedZoneStyles.pop()!);
    }
  }

  private setStyle(style: JSeshStyle): void {
    this.style = style;
    this.currentTextOrientation = style.options.textOrientation;
    this.currentTextDirection = style.options.textDirection;
  }

  // The visitor, as an inner class, shares the layout's state.
  get v(): MDCView {
    return this.currentView;
  }

  get s(): JSeshStyle {
    return this.style;
  }

  get drawer(): HieroglyphDrawer {
    return this.hieroglyphsDrawer;
  }

  get textMeasurer(): TextMeasurer {
    return this.measurer;
  }

  get orientation(): TextOrientation {
    return this.currentTextOrientation;
  }

  get centered(): boolean {
    return this.centerSigns;
  }

  set centered(c: boolean) {
    this.centerSigns = c;
  }

  get absoluteGroup(): boolean {
    return this.inAbsoluteGroup;
  }

  set absoluteGroup(b: boolean) {
    this.inAbsoluteGroup = b;
  }

  /// The scale from font units to points.
  computeScale(): number {
    return this.hieroglyphsDrawer.scaleFromFontToStyle(this.style);
  }

  getGroupUnitScale(): number {
    return this.hieroglyphsDrawer.getGroupUnitLength() * this.computeScale();
  }

  kerner(): Kerner {
    return new Kerner(this.hieroglyphsDrawer, this.computeScale(), this.currentTextDirection);
  }

  packer(axis: KerningAxis): KerningPacker {
    return new KerningPacker(this.kerner(), axis, this.style.geometry.smallSkip);
  }
}

class LayoutAux extends ModelElementAdapter {
  constructor(private readonly l: Layout) {
    super();
  }

  override visitAbsoluteGroup(g: AbsoluteGroup): void {
    const v = this.l.v;
    for (let i = 0; i < v.numberOfSubviews; i++) {
      const subv = v.getSubView(i);
      const h = g.hieroglyphs[i]!;
      subv.resetPos();
      subv.position.x = h.x * this.l.getGroupUnitScale();
      subv.position.y = h.y * this.l.getGroupUnitScale();
    }
    v.fitToSubViews(false);
    this.l.absoluteGroup = false;
  }

  /// A character on its own. In a run of text, its width is then adjusted
  /// by [TextRun.layoutRuns], and its position set by its parent's layout.
  override visitAlphabeticCharacter(c: AlphabeticCharacter): void {
    const style = this.l.s;
    const m = this.l.textMeasurer.characterMetrics(style.fonts, c.script, displayString(c, style.fonts));
    const v = this.l.v;
    v.width = m.advance;
    // The font's ascent and descent, so that all characters of a run share
    // the same box and baseline.
    v.height = f(f(m.ascent) + f(m.descent));
    // The baseline is alphabeticTextBaseline above the quadrats' bottom line.
    const g = style.geometry;
    v.deltaBaseY = f(f(g.maxCadratHeight - g.alphabeticTextBaseline) - f(m.ascent));
  }

  /// Comments are not displayed.
  override visitComment(_c: MdcComment): void {
    this.l.v.reset();
    this.l.v.resetPos();
  }

  /// Lays out the subviews one after another, in the text orientation,
  /// keeping each run of text together, on one line.
  private layoutWithRuns(runs: TextRun[], horizontal: boolean, skip: number): void {
    const v = this.l.v;
    const mirrored = v.direction !== "LEFT_TO_RIGHT";
    const n = v.numberOfSubviews;
    let totalMain = 0;
    let maxCross = 0;
    let units = 0;
    let runIndex = 0;
    for (let i = 0; i < n; ) {
      let main: number;
      let cross: number;
      const run = runs[runIndex];
      if (run !== undefined && run.start === i) {
        runIndex++;
        let h = 0;
        for (const rv of run.views(v)) h = Math.max(h, rv.height);
        main = horizontal ? run.width : h;
        cross = horizontal ? h : run.width;
        i = run.end;
      } else {
        const sv = v.getSubView(i++);
        main = horizontal ? sv.width : sv.height;
        cross = horizontal ? sv.height : sv.width;
      }
      totalMain += main;
      maxCross = Math.max(maxCross, cross);
      units++;
    }
    totalMain += skip * (units - 1);
    if (horizontal) {
      v.width = totalMain;
      v.height = maxCross;
    } else {
      v.width = maxCross;
      v.height = totalMain;
    }
    let pos = 0;
    runIndex = 0;
    for (let i = 0; i < n; ) {
      const run = runs[runIndex];
      if (run !== undefined && run.start === i) {
        runIndex++;
        const views = run.views(v);
        let lineHeight = 0;
        views.forEach((rv, k) => {
          rv.resetPos();
          if (horizontal) {
            rv.position = { x: pos + run.layoutOffset(k, mirrored), y: 0 };
          } else {
            const x0 = (maxCross - run.width) / 2;
            rv.position = { x: x0 + run.layoutOffset(k, mirrored), y: pos };
          }
          lineHeight = Math.max(lineHeight, rv.height);
        });
        pos += (horizontal ? run.width : lineHeight) + skip;
        i = run.end;
      } else {
        const sv = v.getSubView(i++);
        sv.resetPos();
        if (horizontal) {
          sv.position = { x: pos, y: 0 };
          pos += sv.width + skip;
        } else {
          sv.position = { x: (maxCross - sv.width) / 2, y: pos };
          pos += sv.height + skip;
        }
      }
    }
  }

  override visitBasicItemList(l: BasicItemList): void {
    const style = this.l.s;
    const runs = TextRun.layoutRuns(this.l.v, style.fonts, this.l.textMeasurer);
    const horizontal = style.options.textOrientation === "HORIZONTAL";
    if (runs.length > 0) {
      this.layoutWithRuns(runs, horizontal, style.geometry.smallSkip);
    } else if (hasKerning(this.l.v)) {
      this.layoutKernedList(horizontal);
    } else if (horizontal) {
      this.visitDefault(l);
    } else {
      this.l.v.stackTop(style.geometry.smallSkip);
      this.l.v.centerSubViewHorizontally();
    }
  }

  /// Lays out a list of elements with kerning operators, one after another in the text
  /// orientation. The result is the same as without them, except for the kerning.
  private layoutKernedList(horizontal: boolean): void {
    const v = this.l.v;
    const cross = horizontal ? v.maximalHeightOfSubView : v.maximalWidthOfSubView;
    for (const sub of v.subviews) {
      sub.resetPos();
      const kerning = kerningOf(sub) !== undefined;
      if (horizontal) sub.position.y = kerning ? f(cross - sub.height) / 2 : 0;
      else sub.position.x = f(cross - sub.width) / 2;
    }
    const length = f(this.l.packer(horizontal ? "HORIZONTAL" : "VERTICAL").pack(v));
    v.width = horizontal ? length : cross;
    v.height = horizontal ? cross : length;
  }

  override visitCadrat(c: Cadrat): void {
    new QuadratLayout(this.l.s, this.l.drawer, this.l.kerner(), this.l.centered, this.l.orientation).layout(this.l.v, c);
  }

  /// A cartouche: its content, and space for the frame. A cartouche has only
  /// one subview.
  override visitCartouche(c: Cartouche): void {
    const style = this.l.s;
    const v = this.l.v;
    const subView = v.firstSubView;
    // Dimension along the main axis, and the other one.
    let longDim = 0;
    const smallDim = f(2 * computeCartoucheSecondaryLength(style, c.type));
    const dSmall = computeCartoucheSecondaryLength(style, c.type);
    longDim = f(longDim + computeCartouchePartLength(style, c.type, c.startPart));
    const dLong = longDim;
    longDim = f(longDim + computeCartouchePartLength(style, c.type, c.endPart));
    const g = style.geometry;
    if (this.l.orientation === "HORIZONTAL") {
      v.width = subView.width + longDim;
      v.height = Math.max(f(subView.height + smallDim), f(g.maxCadratHeight + smallDim));
      subView.position = { x: dLong, y: dSmall };
    } else {
      v.height = subView.height + longDim;
      if (subView.width < f(g.maxCadratWidth + smallDim)) {
        v.width = g.maxCadratWidth + smallDim;
      } else {
        v.width = subView.width + smallDim;
      }
      const innerMargin = f(f(v.width - subView.width) / 2);
      subView.position = { x: innerMargin, y: dLong };
    }
    // Align the cartouche's content with the text.
    if (this.l.orientation === "HORIZONTAL") {
      v.deltaBaseY = -dSmall;
      v.deltaBaseX = 0;
    } else {
      v.deltaBaseY = 0;
      v.deltaBaseX = -dSmall;
    }
    // Reset the centering, changed by preLayoutHook.
    this.l.centered = style.options.smallSignCentered;
  }

  override visitComplexLigature(ligature: ComplexLigature): void {
    const zones: (LigatureZone | undefined)[] = [];
    for (let i = 0; i < 3; i++) {
      const scale = this.l.computeScale();
      const scale1 =
        ligature.hieroglyph.relativeSize !== 100 ? f(scale * f(ligature.hieroglyph.relativeSize / 100)) : scale;
      const z = this.l.drawer.getLigatureZone(i, ligature.hieroglyph.code);
      zones.push(z === undefined ? undefined : scaleZone(z, scale1));
    }
    const v = this.l.v;
    let index = 0;
    // Subviews: before group, hieroglyph, after group.
    if (ligature.beforeGroup !== null) {
      // Without a zone, ^^^ means "*".
      if (zones[0] !== undefined) placeView(zones[0], v.getSubView(index));
      index++;
    }
    // The hieroglyph stays at 0,0, with scale 1.
    index++;
    if (ligature.afterGroup !== null) {
      const afterGroupView = v.getSubView(index);
      // A "bottom" zone is preferred to the "after" zone.
      if (zones[2] !== undefined) placeView(zones[2], afterGroupView);
      else if (zones[1] !== undefined) placeView(zones[1], afterGroupView);
    }
    // Zones may have negative coordinates.
    v.fitToSubViews(true);
  }

  override visitDefault(_t: ModelElement): void {
    const style = this.l.s;
    const v = this.l.v;
    if (v.numberOfSubviews !== 0) {
      if (style.options.textOrientation === "HORIZONTAL") {
        // Elements one after another.
        v.width = f(v.sumOfSubViewsWidths + f(style.geometry.smallSkip * (v.numberOfSubviews - 1)));
        v.height = v.maximalHeightOfSubView;
        v.distributeHorizontally();
      } else {
        // Elements one on top of the other.
        const height = f(v.sumOfSubViewsHeights + f(style.geometry.smallSkip * (v.numberOfSubviews - 1)));
        v.width = v.maximalWidthOfSubView;
        v.height = height;
        v.distributeFromTopToBottom();
      }
    }
  }

  override visitHBox(b: HBox): void {
    const style = this.l.s;
    const v = this.l.v;
    const h = v.maximalHeightOfSubView;
    // Enlarge high signs which stand alone, as in sw*(i*i:Z2) in columns,
    // where sw should be as high as the whole quadrat.
    for (const subView of v.subviews) {
      const group = subView.model as HorizontalListElement;
      let shouldEnlarge = false;
      if (group.containsOnlyOneSign()) {
        if (subView.height > style.geometry.largeSignSizeRatio * this.l.drawer.getHeightOfA1()) {
          shouldEnlarge = true;
        }
      }
      if (shouldEnlarge) {
        // Only for signs which are not explicitly scaled.
        const hiero = group.getLoneSign();
        if (hiero.relativeSize === 100) subView.reScale(f(h / subView.height));
      }
      new ViewVerticalEnlarger().enlarge(subView, h);
    }
    const w = f(v.sumOfSubViewsWidths + f((v.numberOfSubviews - 1) * style.geometry.smallSkip));
    v.height = h;
    v.width = w;
    // The positions are set by the quadrat.
    for (const subView of v.subviews) new ViewVerticalEnlarger().enlarge(subView, h);
    // With kerning, the horizontal positions are fixed here, as kerned elements must stay
    // together. Elements are aligned on the bottom line, as in the cadrat.
    if (b.containsKerning()) {
      for (const subView of v.subviews) {
        subView.position.y = kerningOf(subView) !== undefined ? h / 2 : f(h - subView.height);
      }
      v.width = this.l.packer("HORIZONTAL").pack(v);
    }
  }

  /// Kerning operators take no room: their place only depends on the elements around them.
  override visitKerning(_k: Kerning): void {
    this.l.v.width = 0;
    this.l.v.height = 0;
  }

  override visitHieroglyph(h: Hieroglyph): void {
    const style = this.l.s;
    const g = style.geometry;
    const v = this.l.v;
    const drawer = this.l.drawer;
    switch (h.type) {
      case SymbolCodes.SMALLTEXT: {
        const r = this.l.textMeasurer.smallTextDimensions(style.fonts, h.getSmallText());
        v.height = r.height;
        v.width = r.width;
        break;
      }
      case SymbolCodes.FULLSHADE:
      case SymbolCodes.FULLSPACE:
        v.width = g.maxCadratWidth;
        v.height = g.maxCadratHeight;
        break;
      case SymbolCodes.QUATERSHADE:
      case SymbolCodes.HALFSPACE:
        v.width = g.maxCadratWidth / 2;
        v.height = g.maxCadratHeight / 2;
        break;
      case SymbolCodes.VERTICALSHADE:
        v.width = g.maxCadratWidth / 2;
        v.height = g.maxCadratHeight;
        break;
      case SymbolCodes.HORIZONTALSHADE:
        v.width = g.maxCadratWidth;
        v.height = g.maxCadratHeight / 2;
        break;
      case SymbolCodes.BEGINERASE:
      case SymbolCodes.ENDERASE:
      case SymbolCodes.REDPOINT:
      case SymbolCodes.BLACKPOINT:
      case SymbolCodes.BEGINEDITORADDITION:
      case SymbolCodes.ENDEDITORADDITION:
      case SymbolCodes.BEGINEDITORSUPERFLUOUS:
      case SymbolCodes.ENDEDITORSUPERFLUOUS:
      case SymbolCodes.BEGINPREVIOUSLYREADABLE:
      case SymbolCodes.ENDPREVIOUSLYREADABLE:
      case SymbolCodes.BEGINMINORADDITION:
      case SymbolCodes.ENDMINORADDITION:
      case SymbolCodes.BEGINSCRIBEADDITION:
      case SymbolCodes.ENDSCRIBEADDITION:
      case SymbolCodes.BEGINDUBIOUS:
      case SymbolCodes.ENDDUBIOUS: {
        // Either a fixed shape (one quadrat high), or a shape which takes its
        // height from the surrounding signs (then small for now).
        const fixed = h.relativeSize !== 100 || this.l.absoluteGroup || h.isAloneInQuadrat() || h.angle !== 0;
        if (!fixed) v.yStretchable = true;
        const rect = drawer.getBBox(h.code, h.angle, fixed);
        v.width = rect?.width ?? 0;
        v.height = rect?.height ?? 0;
        break;
      }
      case SymbolCodes.MDCCODE: {
        const code = h.code;
        if (drawer.isKnown(code)) {
          const s = drawer.getBBox(code, h.angle, true)!;
          v.height = s.height * this.l.computeScale();
          v.width = s.width * this.l.computeScale();
        } else {
          const r = this.l.textMeasurer.smallTextDimensions(style.fonts, code);
          v.height = r.height;
          v.width = r.width;
        }
        break;
      }
    }
    if (h.relativeSize !== 100) v.reScale(f(h.relativeSize / 100));
  }

  override visitHRule(_h: HRule): void {
    this.l.v.width = this.l.s.geometry.textWidth;
    this.l.v.height = this.l.s.geometry.wideLineWidth;
  }

  /// Layout of an insertion: the inserted group is fitted into the free space of the main sign,
  /// according to the actual shapes of both.
  ///
  /// The fit is computed in *drawing* coordinates, as the signs are drawn (this matters for
  /// right-to-left text, where views are mirrored). The sub views' positions are then converted to
  /// layout coordinates.
  override visitInsert(insert: Insert): void {
    const v = this.l.v;
    // The sub views are in reading order, and the target may be either of them.
    const mainView = v.getSubView(insert.target - 1);
    const insertedView = v.getSubView(2 - insert.target);
    const rightToLeft = v.direction === "RIGHT_TO_LEFT";

    const direction = insert.direction;
    let horizontal: HorizontalSide = "CENTER";
    if (isHorizontalInsertion(direction)) {
      // "start" is on the right in right-to-left text.
      horizontal = isStartInsertion(direction) !== rightToLeft ? "LEFT" : "RIGHT";
    }
    let vertical: VerticalSide = "CENTER";
    if (isVerticalInsertion(direction)) vertical = isTopInsertion(direction) ? "TOP" : "BOTTOM";

    const silhouette = new ViewSilhouette(this.l.drawer, this.l.computeScale(), v.direction);
    const mainBox = new Rect(0, 0, mainView.width, mainView.height);
    const insertedBox = new Rect(0, 0, insertedView.width, insertedView.height);
    const frame = enlargedFrame(mainBox, insertedBox, horizontal, vertical, insert.dw, insert.dh);
    const placement = fitInsertion({
      target: silhouette.silhouette(mainView),
      frame,
      inserted: silhouette.silhouette(insertedView),
      insertedBox,
      horizontal,
      vertical,
    });

    insertedView.reScale(placement.scale);
    const placed = placedBox(placement, insertedBox);
    // The whole group: the (possibly enlarged) frame and the inserted element.
    const whole = frame.union(placed);
    v.width = whole.width;
    v.height = whole.height;

    let mainX = mainBox.x - whole.minX;
    let insertedX = placed.minX - whole.minX;
    if (rightToLeft) {
      // Drawing mirrors layout positions across the view's width.
      mainX = v.width - mainX - mainView.width;
      insertedX = v.width - insertedX - insertedView.width;
    }
    mainView.position = { x: mainX, y: mainBox.y - whole.minY };
    insertedView.position = { x: insertedX, y: placed.minY - whole.minY };
  }

  override visitLigature(l: Ligature): void {
    const v = this.l.v;
    const drawer = this.l.drawer;
    const codes = l.hieroglyphs.map((h) => h.code);
    const pos = drawer.getPositions(codes);
    if (pos !== undefined) {
      // Predefined ligature, for unscaled signs.
      pos.forEach((posAtk, k) => {
        const subv = v.getSubView(k);
        const signScale = this.l.computeScale();
        const x = f(f(posAtk.x * signScale) * this.l.getGroupUnitScale());
        const y = f(f(posAtk.y * signScale) * this.l.getGroupUnitScale());
        subv.resetPos();
        subv.reScale(f(posAtk.scale / 100));
        subv.position = { x, y };
      });
    } else if (l.numberOfChildren === 2) {
      // First consider that the first sign is larger, and put the second
      // "below" it.
      let largerSignIndex = 0;
      let smallerSignIndex = 1;
      let ligatureZonePosition = 2;
      // If the second sign is larger, use it and link *before* it.
      if (v.getSubView(1).height * 0.8 > v.getSubView(0).height) {
        largerSignIndex = 1;
        smallerSignIndex = 0;
        ligatureZonePosition = 0;
      }
      const largerCode = l.hieroglyphs[largerSignIndex]!.code;
      // If the larger sign has no zone 2, try zone 1.
      if (ligatureZonePosition === 2 && drawer.getLigatureZone(ligatureZonePosition, largerCode) === undefined) {
        ligatureZonePosition = 1;
      }
      const ligatureZone = drawer.getLigatureZone(ligatureZonePosition, largerCode);
      if (ligatureZone !== undefined) {
        v.getSubView(largerSignIndex).resetPos();
        placeView(scaleZone(ligatureZone, this.l.computeScale()), v.getSubView(smallerSignIndex));
      }
    } else if (l.numberOfChildren === 3) {
      // A ligature centered on the second sign.
      const code = l.hieroglyphs[1]!.code;
      const r1 = drawer.getLigatureZone(0, code);
      const r2 = drawer.getLigatureZone(1, code);
      if (r1 !== undefined && r2 !== undefined) {
        v.getSubView(1).resetPos();
        placeView(scaleZone(r1, this.l.computeScale()), v.getSubView(0));
        placeView(scaleZone(r2, this.l.computeScale()), v.getSubView(2));
      }
    }
    v.fitToSubViews(true);
  }

  /// The skip after a line break is decided by the text layout.
  override visitLineBreak(_b: LineBreak): void {
    this.l.v.width = 0;
    this.l.v.height = this.l.s.geometry.maxCadratHeight;
  }

  override visitOverwrite(_o: Overwrite): void {
    const v = this.l.v;
    v.width = Math.max(v.getSubView(0).width, v.getSubView(1).width);
    v.height = Math.max(v.getSubView(0).height, v.getSubView(1).height);
    v.centerSubViewHorizontally();
    v.centerSubViewsVertically();
  }

  override visitPageBreak(_b: PageBreak): void {}

  override visitPhilology(p: Philology): void {
    const v = this.l.v;
    const subView = v.firstSubView;
    const margin = f(philologyWidth(p.type) + this.l.s.geometry.smallSkip);
    subView.position = { x: margin, y: 0 };
    v.width = f(margin * 2) + subView.width;
    v.height = subView.height;
  }

  override visitSubCadrat(_c: SubCadrat): void {
    const v = this.l.v;
    v.width = v.sumOfSubViewsWidths;
    v.height = v.maximalHeightOfSubView;
    v.distributeHorizontally();
  }

  override visitSuperscript(s: Superscript): void {
    const style = this.l.s;
    const dims = this.l.textMeasurer.smallTextDimensions(style.fonts, s.text);
    this.l.v.width = dims.width;
    this.l.v.height = Math.max(style.geometry.maxCadratHeight, dims.height + style.geometry.smallSkip * 2);
  }

  /// Tab stops are handled by the text layout.
  override visitTabStop(_t: TabStop): void {}

  override visitHieroglyphicText(_t: HieroglyphicText): void {
    this.layoutTopItems();
  }

  /// The content of a zone is laid out as a text, in the zone's style (see
  /// [Layout.preLayoutHook]).
  override visitZoneContent(_content: ZoneContent): void {
    this.layoutTopItems();
  }

  /// A zone is its content, of natural or fixed size, plus the frame's
  /// padding. It is aligned so that its content starts where the quadrats of
  /// the line (or column) start.
  override visitZone(zone: Zone): void {
    const style = this.l.s;
    const p = zone.parameters;
    const v = this.l.v;
    const content = v.firstSubView;
    const unit = zoneLengthUnit(style);
    const padding = p.frame ? zoneFramePadding(style) : 0;
    let width = p.width !== ZONE_UNSET ? f(p.width * unit) : content.width;
    let height = p.height !== ZONE_UNSET ? f(p.height * unit) : content.height;
    if (zone.content.numberOfChildren === 0) {
      // An empty zone stays visible, and can be clicked into.
      width = Math.max(width, style.geometry.maxCadratHeight);
      height = Math.max(height, style.geometry.maxCadratHeight);
    }
    // The content takes the whole inner area, so that right-to-left content
    // is mirrored across the zone's width, not its own.
    content.width = width;
    content.height = height;
    content.resetPos();
    content.position = { x: padding, y: padding };
    v.width = f(width + 2 * padding);
    v.height = f(height + 2 * padding);
    if (this.l.orientation === "HORIZONTAL") {
      v.deltaBaseX = 0;
      v.deltaBaseY = -padding;
    } else {
      v.deltaBaseX = -padding;
      v.deltaBaseY = 0;
    }
  }

  /// Lays out the top items of a text or of a zone (the subviews of the
  /// current view) in lines or columns.
  private layoutTopItems(): void {
    const style = this.l.s;
    const v = this.l.v;
    const topItemLayout: TopItemLayout =
      this.l.orientation === "HORIZONTAL"
        ? new LineLayout(v, style, this.l.kerner())
        : new ColumnLayout(v, style, this.l.kerner());
    const runs = TextRun.layoutRuns(v, style.fonts, this.l.textMeasurer);
    let runIndex = 0;
    topItemLayout.startLayout();
    let i = 0;
    while (i < v.numberOfSubviews) {
      const run = runs[runIndex];
      if (run !== undefined && run.start === i) {
        // A run of text is placed as a block.
        runIndex++;
        const views = run.views(v);
        for (const rv of views) rv.resetPos();
        topItemLayout.layoutTextRun(run, views);
        i = run.end;
      } else {
        const sv = v.getSubView(i++);
        sv.resetPos();
        topItemLayout.layoutElement(sv);
      }
    }
    topItemLayout.endLayout();
    v.width = topItemLayout.documentArea.width;
    v.height = topItemLayout.documentArea.height;
    v.separators = topItemLayout.separatorPositions();
  }
}

/// Enlarges views vertically: sets the stretchable parts of sub-cadrats to
/// the height of their hbox.
class ViewVerticalEnlarger extends ModelElementAdapter {
  private height = 0;
  private view!: MDCView;

  enlarge(subView: MDCView, h: number): void {
    this.height = h;
    this.view = subView;
    subView.model.accept(this);
  }

  override visitSubCadrat(_c: SubCadrat): void {
    const basicElementsListView = this.view.firstSubView;
    let changed = false;
    for (const elementView of basicElementsListView.subviews) {
      if (elementView.yStretchable) {
        elementView.height = this.height;
        elementView.distributeFromTopToBottom();
        changed = true;
      }
    }
    if (changed) {
      basicElementsListView.fitToSubViews(false);
      this.view.fitToSubViews(false);
    }
  }

  override visitHieroglyph(_h: Hieroglyph): void {
    if (this.view.yStretchable) this.view.height = this.height;
  }
}

/// Places a view in a ligature zone, scaled down to fit it.
function placeView(z: LigatureZone, subView: MDCView): void {
  subView.scaleToFitDimensions(z.width, z.height);
  let xpos: number;
  let ypos: number;
  if (z.horizontalGravity === "START") xpos = z.x;
  else if (z.horizontalGravity === "END") xpos = z.x + z.width - subView.width;
  else xpos = z.x + (z.width - subView.width) / 2;
  if (z.verticalGravity === "TOP") ypos = z.y;
  else if (z.verticalGravity === "BOTTOM") ypos = z.y + z.height - subView.height;
  else ypos = z.y + (z.height - subView.height) / 2;
  subView.position = { x: xpos, y: ypos };
}

/// Port of `QuadratLayout`: the layout of a quadrat, which depends on its
/// context.
class QuadratLayout {
  private currentView!: MDCView;

  constructor(
    private readonly style: JSeshStyle,
    private readonly hieroglyphsDrawer: HieroglyphDrawer,
    private readonly kerner: Kerner,
    private readonly centerSigns: boolean,
    private readonly currentTextOrientation: TextOrientation,
  ) {}

  layout(currentView: MDCView, c: Cadrat): void {
    this.currentView = currentView;
    if (c.getKerningAxis() !== undefined) {
      this.layoutKerningOnly(c);
      return;
    }
    const g = this.style.geometry;
    currentView.yStretchable = true;
    currentView.xStretchable = true;
    // Quadrats get their maximal height at the top level of lines, and their
    // maximal width at the top level of columns.
    let honourMaxWidth: boolean;
    let honourMaxHeight: boolean;
    if (this.currentTextOrientation === "HORIZONTAL") {
      honourMaxHeight = true;
      honourMaxWidth = false;
    } else {
      honourMaxHeight = false;
      honourMaxWidth = true;
    }
    const embedded = c.isEmbedded();
    if (embedded) {
      honourMaxHeight = false;
      honourMaxWidth = false;
    }
    const maxWidth = g.maxCadratWidth;
    // No hbox should be too wide, except if there is only one (a "flat"
    // quadrat) or if one contains a wide sign.
    const wide = c.isWide();
    // Lines made only of kerning operators (as in p:%vkern:q) don't count.
    const lineCount = c.hBoxes.filter((b) => b.getKerningAxis() === undefined).length;
    const kerningLines = lineCount !== c.numberOfHBoxes;
    if (honourMaxWidth || (lineCount !== 1 && !wide)) {
      for (const sub of currentView.subviews) sub.scaleForMaxWidth(maxWidth);
    }
    if (wide) {
      // Stretched quadrats.
      const maxw = honourMaxWidth ? maxWidth : currentView.maximalWidthOfSubView;
      for (let k = 0; k < currentView.numberOfSubviews; k++) {
        if (c.getHBox(k).isWide()) currentView.getSubView(k).scaleToWidth(maxw);
      }
    } else {
      // Keep the width, but scale wide lone signs so that they fill their
      // line (for instance, a lone "n" in a column).
      let width = currentView.maximalWidthOfSubView;
      if (honourMaxWidth && !embedded && width < maxWidth) width = maxWidth;
      this.fitLoneSignsToColumnWidth(c, width);
    }
    if (kerningLines) {
      this.layoutWithKerningLines(c, honourMaxWidth, honourMaxHeight, embedded, maxWidth);
    } else {
      // Height: the sum of the hboxes' heights, and the spaces between them.
      let h = currentView.sumOfSubViewsHeights;
      h = f(h + f((currentView.numberOfSubviews - 1) * g.smallSkip));
      // In lines, a quadrat which is not high enough gets some space (if it's
      // too high, it will be scaled).
      if (honourMaxHeight && h < g.maxCadratHeight) h = g.maxCadratHeight;
      currentView.height = h;
      // Width.
      let w = currentView.maximalWidthOfSubView;
      if (honourMaxWidth && !embedded && w < maxWidth) w = maxWidth;
      currentView.width = w;
      if (currentView.numberOfSubviews > 1) {
        currentView.distributeFromTopToBottom();
      } else if (this.centerSigns) {
        currentView.centerSubViewsVertically();
      } else {
        currentView.stickSubViewsTo("BOTTOM");
      }
      this.layoutLines(c, w);
    }
    const h = currentView.height;
    if (honourMaxHeight && h > g.maxCadratHeight) currentView.reScale(f(g.maxCadratHeight / h));
  }

  /// Places the elements of each line of the quadrat, once its width is known (this makes the
  /// quadrat square).
  private layoutLines(c: Cadrat, w: number): void {
    const currentView = this.currentView;
    for (let k = 0; k < currentView.numberOfSubviews; k++) {
      const sub = currentView.getSubView(k);
      if (sub.width < w) sub.width = w;
      if (c.getHBox(k).containsKerning()) {
        // Kerned elements are moved together; vertical positions are already set.
        const packer = this.packer("HORIZONTAL");
        const blocks = packer.blocks(sub);
        if (blocks.length > 1) {
          packer.distribute(sub, blocks, sub.internalWidth);
        } else if (blocks.length > 0) {
          const block = blocks[0]!;
          packer.moveBlockTo(sub, block, (sub.internalWidth - packer.blockLength(sub, block)) / 2);
        }
      } else if (sub.numberOfSubviews === 1) {
        sub.centerSubViewHorizontally();
      } else {
        sub.distributeHorizontally();
        // Align the bottom lines of the elements.
        sub.stickSubViewsTo("BOTTOM");
      }
    }
  }

  /// Lays out a quadrat with lines made only of kerning operators, as in `p:%vkern:q`.
  ///
  /// Such lines take no room. A `%vkern` line kerns the lines around it, which then move
  /// together; they are only known once the content of the lines is placed.
  private layoutWithKerningLines(
    c: Cadrat,
    honourMaxWidth: boolean,
    honourMaxHeight: boolean,
    embedded: boolean,
    maxWidth: number,
  ): void {
    const currentView = this.currentView;
    const g = this.style.geometry;
    let w = currentView.maximalWidthOfSubView;
    if (honourMaxWidth && !embedded && w < maxWidth) w = maxWidth;
    currentView.width = w;
    this.layoutLines(c, w);

    for (const sub of currentView.subviews) sub.position.x = 0;
    const packer = this.packer("VERTICAL");
    let h = f(packer.pack(currentView));
    if (honourMaxHeight && h < g.maxCadratHeight) h = g.maxCadratHeight;
    currentView.height = h;

    const blocks = packer.blocks(currentView);
    if (blocks.length > 1) {
      packer.distribute(currentView, blocks, currentView.internalHeight);
    } else if (blocks.length > 0) {
      const block = blocks[0]!;
      const free = currentView.internalHeight - packer.blockLength(currentView, block);
      packer.moveBlockTo(currentView, block, this.centerSigns ? free / 2 : free);
    }
  }

  /// Lays out a quadrat made only of kerning operators, as in `p-%hkern-q`.
  ///
  /// It takes no room along the text, but at the top level it gets the size of a quadrat across
  /// it, so that kerning marks can be drawn in the middle of the line.
  private layoutKerningOnly(c: Cadrat): void {
    const g = this.style.geometry;
    let w = 0;
    let h = 0;
    if (!c.isEmbedded()) {
      if (this.currentTextOrientation === "HORIZONTAL") h = g.maxCadratHeight;
      else w = g.maxCadratWidth;
    }
    const currentView = this.currentView;
    currentView.width = w;
    currentView.height = h;
    for (const sub of currentView.subviews) sub.position = { x: f(w / 2), y: f(h / 2) };
  }

  private packer(axis: KerningAxis): KerningPacker {
    return new KerningPacker(this.kerner, axis, this.style.geometry.smallSkip);
  }

  private fitLoneSignsToColumnWidth(c: Cadrat, width: number): void {
    const g = this.style.geometry;
    const currentView = this.currentView;
    for (let k = 0; k < currentView.numberOfSubviews; k++) {
      if (currentView.getSubView(k).numberOfSubviews !== 1) continue;
      const elt = c.getHBox(k).children[0] as HorizontalListElement;
      if (!elt.containsOnlyOneSign()) continue;
      const hiero = elt.getLoneSign();
      const subSubView = currentView.getSubView(k).getSubView(0);
      // A lone, unscaled, low broad sign: enlarge it, and its hbox.
      const heightOfA1 = this.hieroglyphsDrawer.getHeightOfA1();
      if (
        hiero.relativeSize === 100 &&
        subSubView.internalWidth >= g.largeSignSizeRatio * heightOfA1 &&
        subSubView.internalHeight <= g.smallSignSizeRatio * heightOfA1
      ) {
        subSubView.reScale(f(width / subSubView.width));
        currentView.getSubView(k).fitToSubViews(true);
      }
    }
  }
}

/// Port of `LineAccumulator`: a line or a column being built. It keeps track of its
/// size and views, and of the point where the next view goes.
class LineAccumulator {
  readonly currentPoint: Point = { x: 0, y: 0 };
  private readonly zoneArea: Rect;
  private readonly views: MDCView[] = [];
  /// The views, grouped in units moved as a whole by justification (a run
  /// of text is one unit).
  private readonly units: MDCView[][] = [];

  constructor(minWidth: number, minHeight: number) {
    this.zoneArea = new Rect(0, 0, minWidth, minHeight);
  }

  add(view: MDCView): void {
    view.position = { x: this.currentPoint.x + view.deltaBaseX, y: this.currentPoint.y + view.deltaBaseY };
    this.zoneArea.add(new Rect(view.position.x, view.position.y, view.width, view.height));
    this.views.push(view);
    this.units.push([view]);
  }

  /// Adds a view kerned with the previous one: they will be moved together when the zone is
  /// justified. The view is placed as in [add].
  addKerned(view: MDCView): void {
    const last = this.units[this.units.length - 1];
    if (last === undefined) {
      this.add(view);
      return;
    }
    view.position = { x: this.currentPoint.x + view.deltaBaseX, y: this.currentPoint.y + view.deltaBaseY };
    this.zoneArea.add(new Rect(view.position.x, view.position.y, view.width, view.height));
    this.views.push(view);
    last.push(view);
  }

  /// Adds a run of views which must stay together; `xOffsets` are their
  /// positions relative to the current point.
  addRun(runViews: MDCView[], xOffsets: number[]): void {
    runViews.forEach((view, k) => {
      view.position = {
        x: this.currentPoint.x + xOffsets[k]! + view.deltaBaseX,
        y: this.currentPoint.y + view.deltaBaseY,
      };
      this.zoneArea.add(new Rect(view.position.x, view.position.y, view.width, view.height));
      this.views.push(view);
    });
    this.units.push([...runViews]);
  }

  /// The area the zone would have if it included `view` at the current point.
  computeNewSizeWith(view: MDCView): Rect {
    const area = this.zoneArea.clone();
    area.add(new Rect(this.currentPoint.x + view.deltaBaseX, this.currentPoint.y + view.deltaBaseY, view.width, view.height));
    return area;
  }

  get width(): number {
    return this.zoneArea.width;
  }

  get height(): number {
    return this.zoneArea.height;
  }

  isEmpty(): boolean {
    return this.zoneArea.isEmpty();
  }

  get minX(): number {
    return this.zoneArea.minX;
  }

  get minY(): number {
    return this.zoneArea.minY;
  }

  get maxX(): number {
    return this.zoneArea.maxX;
  }

  moveCurrentPoint(dx: number, dy: number): void {
    this.currentPoint.x += dx;
    this.currentPoint.y += dy;
  }

  translateBy(p: Point): void {
    for (const v of this.views) {
      v.position.x += p.x;
      v.position.y += p.y;
    }
  }

  /// Justifies the zone to `width` (after `startMargin`). A lone unit is
  /// centered; if the spaces would be larger than half the units' width,
  /// the zone is centered; else, the units are equally distributed.
  justifyWidthTo(startMargin: number, width: number): void {
    let minimalWidth = 0;
    const nonEmptyUnits: MDCView[][] = [];
    for (const unit of this.units) {
      const w = unitWidth(unit);
      minimalWidth += w;
      if (w !== 0) nonEmptyUnits.push(unit);
    }
    if (minimalWidth < width) {
      const space = width - minimalWidth;
      if (nonEmptyUnits.length === 1) {
        moveUnitTo(nonEmptyUnits[0]!, startMargin + space / 2);
      } else if (space > 0.5 * minimalWidth) {
        this.centerInWidth(startMargin, width);
      } else if (nonEmptyUnits.length > 1) {
        const skip = space / (nonEmptyUnits.length - 1);
        let x = startMargin;
        for (const unit of nonEmptyUnits) {
          moveUnitTo(unit, x);
          x += unitWidth(unit) + skip;
        }
      }
    }
  }

  private centerInWidth(startMargin: number, width: number): void {
    let currentStart = Infinity;
    let currentEnd = -Infinity;
    for (const v of this.views) {
      if (v.width > 0) {
        currentStart = Math.min(currentStart, v.position.x);
        currentEnd = Math.max(currentEnd, v.position.x + v.width);
      }
    }
    if (currentStart > currentEnd) return;
    const centerMargin = startMargin + (width - (currentEnd - currentStart)) / 2;
    this.translateBy({ x: centerMargin - currentStart, y: 0 });
  }
}

function unitMinX(unit: MDCView[]): number {
  return Math.min(...unit.map((v) => v.position.x));
}

function unitWidth(unit: MDCView[]): number {
  if (unit.length === 1) return unit[0]!.width;
  return Math.max(...unit.map((v) => v.position.x + v.width)) - unitMinX(unit);
}

function moveUnitTo(unit: MDCView[], x: number): void {
  const delta = x - unitMinX(unit);
  for (const v of unit) v.position.x += delta;
}

/// Port of `TopItemLayout`: places the top items of the text.
interface TopItemLayout {
  startLayout(): void;
  layoutElement(subView: MDCView): void;
  layoutTextRun(run: TextRun, views: MDCView[]): void;
  endLayout(): void;
  readonly documentArea: Rect;
  /// The middle of the gap between each line (or column) and the next one,
  /// after layout (y for lines, x for columns). Used to draw separator rules.
  separatorPositions(): number[];
}

/// The middle of the gap between each extent (start, end) and the next one.
function separatorsBetween(extents: [number, number][]): number[] {
  const result: number[] = [];
  for (let i = 0; i + 1 < extents.length; i++) result.push((extents[i]![1] + extents[i + 1]![0]) / 2);
  return result;
}

/// The kerning state of [LineLayout] and [ColumnLayout], which handle top-level kerning the same
/// way, each along its own axis.
class TopLevelKerning {
  /// The elements of the current kerned group (since the last element which was not kerned).
  private readonly kernedGroup: MDCView[] = [];
  /// Is there a kerning operator of the layout's axis after the current kerned group?
  private pendingKerning = false;
  /// The kerning operators met since the last element; they are moved to the middle of the gap.
  private readonly kerningViews: MDCView[] = [];

  constructor(
    private readonly kerner: Kerner,
    private readonly axis: KerningAxis,
    private readonly skip: number,
  ) {}

  /// Is the next element to be kerned with the previous ones?
  get isPending(): boolean {
    return this.pendingKerning;
  }

  reset(): void {
    this.kernedGroup.length = 0;
    this.pendingKerning = false;
    this.kerningViews.length = 0;
  }

  /// Adds `subView` to the zone, kerned with the previous elements if needed. Returns false if
  /// it is a kerning operator, which takes no room (and is only placed later).
  add(zone: LineAccumulator, subView: MDCView): boolean {
    const kerning = kerningOf(subView);
    const horizontal = this.axis === "HORIZONTAL";
    if (kerning !== undefined) {
      this.pendingKerning ||= this.kernedGroup.length > 0 && kerning === this.axis;
      // In the unit of the previous element, so that justification keeps kerned elements together.
      zone.addKerned(subView);
      this.kerningViews.push(subView);
      return false;
    }
    if (this.pendingKerning) {
      // Place the view where it would go, and move it back.
      subView.position = { x: zone.currentPoint.x + subView.deltaBaseX, y: zone.currentPoint.y + subView.deltaBaseY };
      const shift = this.kerner.shift(this.kernedGroup, subView, this.axis, this.skip);
      if (horizontal) zone.moveCurrentPoint(-shift, 0);
      else zone.moveCurrentPoint(0, -shift);
      zone.addKerned(subView);
    } else {
      zone.add(subView);
    }
    const last = this.kernedGroup[this.kernedGroup.length - 1];
    if (last !== undefined) {
      if (horizontal) {
        const middle = (last.position.x + last.width + subView.position.x) / 2;
        for (const k of this.kerningViews) k.position.x = middle - k.width / 2;
      } else {
        const middle = (last.position.y + last.height + subView.position.y) / 2;
        for (const k of this.kerningViews) k.position.y = middle - k.height / 2;
      }
    }
    this.kerningViews.length = 0;
    if (!this.pendingKerning) this.kernedGroup.length = 0;
    this.pendingKerning = false;
    this.kernedGroup.push(subView);
    return true;
  }
}

/// Port of `LineLayout`: text in lines.
class LineLayout extends ModelElementAdapter implements TopItemLayout {
  readonly documentArea = new Rect();
  private zone: LineAccumulator = new LineAccumulator(0, 0);
  private readonly allZones: LineAccumulator[] = [];
  /// The vertical extent (top, bottom) of each line of text, for the separators.
  private readonly lineExtents: [number, number][] = [];
  /// Where the current zone will be placed.
  private zoneOriginPosition: Point = { x: 0, y: 0 };
  private readonly currentTextDirection: TextDirection;
  private subView!: MDCView;
  /// True for the first line of a page, which gets the top margin.
  private addTopMargin = true;
  /// In lines, only `%hkern` is meaningful.
  private readonly kerning: TopLevelKerning;
  /// The zones which ended because of soft wrapping (not at a line break).
  /// When wrapping, only those are justified: the others end a paragraph.
  private readonly softWrappedZones = new Set<LineAccumulator>();

  constructor(
    private readonly documentView: MDCView,
    private readonly style: JSeshStyle,
    kerner: Kerner,
  ) {
    super();
    this.currentTextDirection = style.options.textDirection;
    this.kerning = new TopLevelKerning(kerner, "HORIZONTAL", style.geometry.smallSkip);
  }

  layoutElement(subView: MDCView): void {
    this.subView = subView;
    subView.position = { x: 0, y: 0 };
    subView.model.accept(this);
  }

  /// The run is placed in its own reading direction: mirrored here if the
  /// text is right to left, so that drawing mirrors it back.
  layoutTextRun(run: TextRun, views: MDCView[]): void {
    this.kerning.reset();
    const mirrored = this.currentTextDirection !== "LEFT_TO_RIGHT";
    const n = views.length;
    if (!isWrapping(this.style.geometry)) {
      this.placeRunPart(run, views, 0, n, mirrored);
      return;
    }
    // Wrapping: the run is cut at spaces and punctuation, or anywhere if a
    // word is too long for a line.
    const breakAfter: boolean[] = [];
    const space: boolean[] = [];
    for (const v of views) {
      const c = v.model as AlphabeticCharacter;
      breakAfter.push(TextRun.canBreakAfter(c));
      space.push(c.isSpace());
    }
    let from = 0;
    while (from < n) {
      const to = this.runPartEnd(run, from, breakAfter, space);
      this.placeRunPart(run, views, from, to, mirrored);
      from = to;
      if (from < n) this.wrapLine();
    }
  }

  /// Adds the characters `from` to `to - 1` of a run to the current line.
  private placeRunPart(run: TextRun, views: MDCView[], from: number, to: number, mirrored: boolean): void {
    this.zone.addRun(views.slice(from, to), run.layoutOffsets(mirrored, from, to));
    const width = run.partWidth(from, to);
    if (width !== 0) this.zone.moveCurrentPoint(width + this.style.geometry.smallSkip, 0);
  }

  /// Finds how much of a run, starting at the character `from`, goes on the
  /// current line. If nothing fits, the line is wrapped (unless it is empty).
  ///
  /// The part ends after a space or punctuation. If not even a word fits on an
  /// empty line, it is cut between any two characters. Trailing spaces may go
  /// beyond the end of the line.
  ///
  /// Returns the index after the last character to place on the line (greater than `from`).
  private runPartEnd(run: TextRun, from: number, breakAfter: boolean[], space: boolean[]): number {
    let end = this.fittingEnd(run, from, breakAfter, space, true);
    if (end > from) return end;
    if (!this.zone.isEmpty()) {
      this.wrapLine();
      end = this.fittingEnd(run, from, breakAfter, space, true);
      if (end > from) return end;
    }
    // The word is larger than a line.
    return Math.max(from + 1, this.fittingEnd(run, from, breakAfter, space, false));
  }

  /// The end of the largest part of a run, starting at `from`, which fits the
  /// room left on the current line. With `atBreaksOnly`, the part ends only
  /// after a space or punctuation (or at the end of the run).
  ///
  /// Returns the index after the last character of the part, or `from` if nothing fits.
  private fittingEnd(run: TextRun, from: number, breakAfter: boolean[], space: boolean[], atBreaksOnly: boolean): number {
    const available = this.maxLineWidth() - this.zone.currentPoint.x;
    const n = breakAfter.length;
    let best = from;
    for (let end = from + 1; end <= n; end++) {
      // Trailing spaces are not counted.
      let visibleEnd = end;
      while (visibleEnd > from + 1 && space[visibleEnd - 1]) visibleEnd--;
      if (run.partWidth(from, visibleEnd) > available) break;
      if (!atBreaksOnly || end === n || breakAfter[end - 1]) best = end;
    }
    return best;
  }

  endLayout(): void {
    const g = this.style.geometry;
    if (!this.zone.isEmpty()) this.flushZone();
    if (this.style.options.justified) {
      if (isWrapping(g)) {
        // Wrapped lines are justified to the wrapping width.
        // Zones which end a paragraph stay as they are.
        for (const z of this.allZones) {
          if (this.softWrappedZones.has(z)) z.justifyWidthTo(g.leftMargin, this.maxLineWidth());
        }
      } else {
        this.justifyOnLongestZone();
      }
    }
    this.documentArea.addPoint(this.documentArea.maxX + g.leftMargin, this.documentArea.minY + g.topMargin);
  }

  /// Justifies all zones to the width of the longest one.
  private justifyOnLongestZone(): void {
    const g = this.style.geometry;
    let width = 0;
    for (const z of this.allZones) if (z.width > width) width = z.width;
    width = width - g.leftMargin;
    this.allZones.forEach((z, i) => {
      // Not the last zone, unless it looks large enough (temporary fix).
      if (i !== this.allZones.length - 1 || z.width >= 0.75 * width) z.justifyWidthTo(g.leftMargin, width);
    });
  }

  /// The width available for the content of a line when wrapping, i.e. the
  /// wrapping width, minus the margins on both sides.
  private maxLineWidth(): number {
    const g = this.style.geometry;
    return g.wrapWidth - 2 * g.leftMargin;
  }

  /// Should the current line be wrapped before adding something which would
  /// extend it up to `x`? A line is never wrapped when empty: an element too
  /// large for the wrapping width is placed alone on its line.
  private wouldOverflow(x: number): boolean {
    return isWrapping(this.style.geometry) && !this.zone.isEmpty() && x > this.maxLineWidth();
  }

  /// Ends the current line because of soft wrapping, and starts the next one.
  private wrapLine(): void {
    this.kerning.reset();
    const verticalSkip = this.zone.height + this.zone.minY + this.style.geometry.lineSkip;
    this.softWrappedZones.add(this.zone);
    this.flushZone();
    this.startNextZone(verticalSkip);
  }

  /// Creates the zone for the next line, `verticalSkip` below the top of the
  /// line which has just been flushed.
  private startNextZone(verticalSkip: number): void {
    this.zone = new LineAccumulator(0, this.style.geometry.maxCadratHeight);
    this.zoneOriginPosition.y += verticalSkip;
    this.addTopMargin = false;
  }

  separatorPositions(): number[] {
    return separatorsBetween(this.lineExtents);
  }

  startLayout(): void {
    this.allZones.length = 0;
    this.softWrappedZones.clear();
    this.lineExtents.length = 0;
    this.addTopMargin = true;
    this.documentView.reset();
    this.documentView.direction = this.currentTextDirection;
    this.initZoneForPage();
  }

  private initZoneForPage(): void {
    this.zone = new LineAccumulator(0, this.style.geometry.maxCadratHeight);
    this.zoneOriginPosition = { x: 0, y: 0 };
  }

  /// Adds the current zone to the page. `textLine`: is the zone a line of
  /// text (and not the mark of a page break)? Only lines are separated by rules.
  private flushZone(textLine = true): void {
    // Large signs (cartouches) can make the zone's top negative: the zone is
    // moved so that its real top is at least a margin below the previous line.
    const g = this.style.geometry;
    const marginx = g.leftMargin;
    const marginy = this.addTopMargin ? g.topMargin : 0;
    const minx = this.zone.minX;
    const miny = this.zone.minY;
    this.zoneOriginPosition.y = this.zoneOriginPosition.y + marginy - miny;
    this.zoneOriginPosition.x = marginx - minx;
    this.zone.translateBy(this.zoneOriginPosition);
    this.documentArea.add(
      new Rect(this.zoneOriginPosition.x + minx, this.zoneOriginPosition.y + miny, this.zone.width, this.zone.height),
    );
    if (textLine) {
      this.lineExtents.push([this.zoneOriginPosition.y + miny, this.zoneOriginPosition.y + miny + this.zone.height]);
    }
    if (this.style.options.justified) this.allZones.push(this.zone);
  }

  override visitLineBreak(b: LineBreak): void {
    this.kerning.reset();
    const g = this.style.geometry;
    // Top of the next line, relative to the top of this one.
    let verticalSkip = this.zone.height + this.zone.minY + g.lineSkip;
    if (b.spacing !== 100) verticalSkip = (verticalSkip * b.spacing) / 100;
    this.zone.add(this.subView);
    this.flushZone();
    this.startNextZone(verticalSkip);
  }

  override visitPageBreak(_b: PageBreak): void {
    this.kerning.reset();
    const g = this.style.geometry;
    const dh = this.zone.height;
    this.flushZone();
    if (!this.style.options.paged) {
      // A zone with only the page break.
      this.zone = new LineAccumulator(0, 0);
      this.zoneOriginPosition.y += g.lineSkip + dh;
      this.subView.width = 32;
      this.subView.height = g.lineSkip;
      this.zone.add(this.subView);
      this.flushZone(false);
      this.zone = new LineAccumulator(0, g.maxCadratHeight);
    } else {
      this.initZoneForPage();
    }
    this.addTopMargin = true;
  }

  /// Tabs are relative to the current zone.
  override visitTabStop(tab: TabStop): void {
    this.kerning.reset();
    const pos = f(tab.stopPos * this.style.geometry.tabUnitWidth);
    this.zone.currentPoint.x = pos;
    this.zone.currentPoint.y = 0;
    this.zone.add(this.subView);
  }

  override visitDefault(_t: ModelElement): void {
    let dx = 0;
    if (this.subView.width !== 0) dx = f(this.subView.width + this.style.geometry.smallSkip);
    // A kerned element stays with the previous one: no wrapping before it.
    if (
      kerningOf(this.subView) === undefined &&
      !this.kerning.isPending &&
      this.subView.width !== 0 &&
      this.wouldOverflow(this.zone.computeNewSizeWith(this.subView).maxX)
    ) {
      this.wrapLine();
    }
    if (this.kerning.add(this.zone, this.subView)) this.zone.moveCurrentPoint(dx, 0);
  }
}

/// Port of `ColumnLayout`: text in columns.
class ColumnLayout extends ModelElementAdapter implements TopItemLayout {
  readonly documentArea = new Rect();
  private zone!: LineAccumulator;
  private zoneStart: Point = { x: 0, y: 0 };
  private style: JSeshStyle;
  private readonly currentTextDirection: TextDirection;
  private subView!: MDCView;
  /// In columns, only `%vkern` is meaningful.
  private readonly kerning: TopLevelKerning;
  /// The horizontal extent (left, right) of each column, for the separators.
  private readonly columnExtents: [number, number][] = [];

  constructor(
    private readonly documentView: MDCView,
    style: JSeshStyle,
    kerner: Kerner,
  ) {
    super();
    this.style = style;
    this.currentTextDirection = style.options.textDirection;
    this.kerning = new TopLevelKerning(kerner, "VERTICAL", style.geometry.smallSkip);
  }

  layoutElement(subView: MDCView): void {
    this.subView = subView;
    subView.model.accept(this);
  }

  /// A run of text is one horizontal line in the column (not aligned with
  /// the hieroglyphs' baseline).
  layoutTextRun(run: TextRun, views: MDCView[]): void {
    this.kerning.reset();
    const mirrored = this.currentTextDirection !== "LEFT_TO_RIGHT";
    let height = 0;
    for (const v of views) {
      v.deltaBaseY = 0;
      height = Math.max(height, v.height);
    }
    this.zone.addRun(views, run.layoutOffsets(mirrored));
    if (height !== 0) this.zone.moveCurrentPoint(0, height + this.style.geometry.lineSkip);
  }

  endLayout(): void {
    if (!this.zone.isEmpty()) this.flushZone();
    this.documentArea.addPoint(this.documentArea.maxX + this.style.geometry.leftMargin, this.documentArea.minY);
  }

  separatorPositions(): number[] {
    return separatorsBetween(this.columnExtents);
  }

  startLayout(): void {
    this.columnExtents.length = 0;
    this.documentView.reset();
    this.style = withStyle(this.style, { options: { textOrientation: "VERTICAL" } });
    this.documentView.direction = this.currentTextDirection;
    this.zone = this.createNewZone();
    this.zoneStart = { x: this.style.geometry.leftMargin, y: 0 };
  }

  private createNewZone(): LineAccumulator {
    const z = new LineAccumulator(0, 0);
    z.moveCurrentPoint(0, this.style.geometry.topMargin);
    return z;
  }

  private flushZone(): void {
    // The zone's top left is normally 0,0, unless it's higher than expected.
    this.zoneStart.y += -this.zone.minY;
    this.zoneStart.x += -this.zone.minX;
    this.zone.translateBy(this.zoneStart);
    this.columnExtents.push([this.zoneStart.x, this.zoneStart.x + this.zone.width]);
    this.documentArea.add(new Rect(this.zoneStart.x, this.zoneStart.y, this.zone.width, this.zone.height));
  }

  override visitLineBreak(_b: LineBreak): void {
    this.endColumn();
  }

  private endColumn(): void {
    this.kerning.reset();
    // A minimal height, else empty columns would have no height.
    this.subView.height = 0.1;
    this.subView.width = this.style.geometry.maxCadratWidth;
    this.zone.add(this.subView);
    this.flushZone();
    this.zoneStart.x += this.zone.width + this.style.geometry.columnSkip;
    this.zone = this.createNewZone();
  }

  override visitPageBreak(_b: PageBreak): void {
    if (!this.style.options.paged) this.endColumn();
  }

  override visitTabStop(t: TabStop): void {
    this.visitDefault(t);
  }

  override visitDefault(_t: ModelElement): void {
    if (!this.kerning.add(this.zone, this.subView)) return;
    if (this.subView.height !== 0) this.zone.moveCurrentPoint(0, this.subView.height + this.style.geometry.lineSkip);
  }
}
