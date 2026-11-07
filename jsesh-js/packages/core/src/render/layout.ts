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

import type { KerningAxis, LineAlignment, TextDirection, TextOrientation } from "../model/constants.js";
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
  type AbsoluteTab,
  Tab,
  Zone,
  ZoneContent,
  ZONE_UNSET,
  zoneScaleFactor,
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
import type { Outline } from "./insert/shape-mask.js";
import { ViewSilhouette } from "./insert/view-silhouette.js";
import { transformPath } from "./path.js";
import {
  computeCartouchePartLength,
  computeCartoucheSecondaryLength,
  isWrapping,
  isPaginated,
  layoutSettingsChanges,
  maxColumnHeight,
  maxLineWidth,
  NO_PAGE,
  pageStride,
  percentUnit,
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

/// The style in which the content of a zone is laid out and drawn
/// (`JSeshRenderContext.forZone`).
///
/// Settings which the zone doesn't set are inherited from the enclosing
/// style (see `layoutSettingsChanges`, shared with the document preferences). A
/// zone has no margins, and is never paged (a page break in a zone is a line break).
///
/// A zone in lines wraps at its width, if it has one. It never inherits the
/// wrapping width of the enclosing text. A zone in columns aligns its columns in its
/// height, if it has one. The alignment of its lines is never ignored.
///
/// The `scale` of a zone is not part of this style: the content is laid out at full size, and
/// its view is scaled afterwards. So the lengths of the zone are in its own quadrats.
export function zoneStyle(parent: JSeshStyle, p: ZoneParameters): JSeshStyle {
  const unit = zoneLengthUnit(parent);
  const orientation = p.settings.layout ?? parent.options.textOrientation;
  const geometry: Partial<JSeshStyle["geometry"]> = {
    leftMargin: 0,
    topMargin: 0,
    page: NO_PAGE,
    wrapWidth: orientation === "HORIZONTAL" && p.width !== ZONE_UNSET ? f(p.width * unit) : 0,
    columnHeight: orientation !== "HORIZONTAL" && p.height !== ZONE_UNSET ? f(p.height * unit) : 0,
  };
  if (p.width !== ZONE_UNSET) geometry.textWidth = f(p.width * unit);
  const base = withStyle(parent, {
    options: {
      paged: false,
      // A zone keeps its own width, even when the text is wrapped to the width of an editor.
      ignoreLineAlignment: false,
    },
    geometry,
  });
  return withStyle(base, layoutSettingsChanges(p.settings, parent.geometry.unit));
}

/// The unit of the lengths of [ZoneParameters]: one percent of the unit of the
/// document (the height of a quadrat).
export function zoneLengthUnit(style: JSeshStyle): number {
  return percentUnit(style.geometry);
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

  /// The style of the zone or text which encloses the zone being laid out (the one which gives
  /// the unit of the zone's lengths). Only meaningful while the content of a zone is laid out.
  get parentStyle(): JSeshStyle | undefined {
    return this.savedZoneStyles[this.savedZoneStyles.length - 1];
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
  /// The result is given in *drawing* coordinates, as the signs are drawn (this matters for
  /// right-to-left text, where views are mirrored). The sub views' positions are then converted to
  /// layout coordinates.
  ///
  /// Right-to-left text must be drawn as the exact mirror image of left-to-right text, but the fit
  /// (which rasterises the shapes on a grid anchored on the left of the frame) is not symmetric. So,
  /// as in Java's `InsertPlacer`, the fit is always computed in the left-to-right orientation: in
  /// right-to-left text, the silhouettes are mirrored back before the fit, and the result is
  /// mirrored afterwards.
  override visitInsert(insert: Insert): void {
    const v = this.l.v;
    // The sub views are in reading order, and the target may be either of them.
    const mainView = v.getSubView(insert.target - 1);
    const insertedView = v.getSubView(2 - insert.target);
    const rightToLeft = v.direction === "RIGHT_TO_LEFT";

    const direction = insert.direction;
    let horizontal: HorizontalSide = "CENTER";
    if (isHorizontalInsertion(direction)) {
      // In the left-to-right orientation of the fit, "start" is on the left.
      horizontal = isStartInsertion(direction) ? "LEFT" : "RIGHT";
    }
    let vertical: VerticalSide = "CENTER";
    if (isVerticalInsertion(direction)) vertical = isTopInsertion(direction) ? "TOP" : "BOTTOM";

    const silhouette = new ViewSilhouette(this.l.drawer, this.l.computeScale(), v.direction);
    const mainWidth = mainView.width;
    const insertedWidth = insertedView.width;
    const mainBox = new Rect(0, 0, mainWidth, mainView.height);
    const insertedBox = new Rect(0, 0, insertedWidth, insertedView.height);
    let mainShapes = silhouette.silhouette(mainView);
    let insertedShapes = silhouette.silhouette(insertedView);
    if (rightToLeft) {
      mainShapes = mirrorOutlines(mainShapes, mainWidth);
      insertedShapes = mirrorOutlines(insertedShapes, insertedWidth);
    }
    let frame = enlargedFrame(mainBox, insertedBox, horizontal, vertical, insert.dw, insert.dh);
    let placement = fitInsertion({
      target: mainShapes,
      frame,
      inserted: insertedShapes,
      insertedBox,
      horizontal,
      vertical,
    });
    if (rightToLeft) {
      // Back to drawing coordinates: the frame is mirrored across the main element's box. A point
      // x of the (drawn) inserted element is the point insertedWidth - x of the fitted one, which
      // went to tx + s (insertedWidth - x), mirrored into mainWidth - tx - s insertedWidth + s x.
      frame = new Rect(mainWidth - frame.maxX, frame.y, frame.width, frame.height);
      placement = {
        ...placement,
        translateX: mainWidth - placement.translateX - placement.scale * insertedWidth,
      };
    }

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
  override visitAbsoluteTab(_t: AbsoluteTab): void {}

  /// Tabs are handled when laying out the top items.
  override visitTab(_t: Tab): void {}

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
    if (p.scale !== ZONE_UNSET) {
      // The content is laid out at full size, in the zone's own quadrats: its view is scaled.
      content.reScale(zoneScaleFactor(p));
      width = content.width;
      height = content.height;
    }
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

  /// The minimal space between a tab cell and the next tab stop, in a zone: its `tabgap`, or
  /// else the space between two quadrats.
  ///
  /// `parentStyle` is the style of the enclosing zone or text (which gives the unit of the zone's
  /// lengths), `zoneStyle` the style of the content of the zone.
  private tabGap(p: ZoneParameters, parentStyle: JSeshStyle, zoneStyle: JSeshStyle): number {
    if (p.tabGap !== ZONE_UNSET) return f(p.tabGap * zoneLengthUnit(parentStyle));
    return this.l.orientation === "HORIZONTAL" ? zoneStyle.geometry.smallSkip : zoneStyle.geometry.columnQuadratSkip;
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
    const parentStyle = this.l.parentStyle;
    if (v.model instanceof ZoneContent && v.model.parent instanceof Zone && parentStyle !== undefined) {
      topItemLayout.enableTabs(this.tabGap(v.model.parent.parameters, parentStyle, style));
    }
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
    // In horizontal text, the width limit is optional. In columns, it always applies.
    const limitWidthOfHBoxes =
      this.currentTextOrientation !== "HORIZONTAL" || this.style.options.limitQuadratWidthInLines;
    if (honourMaxWidth || (limitWidthOfHBoxes && lineCount !== 1 && !wide)) {
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
      if (this.style.options.fitLoneSigns) this.fitLoneSignsToColumnWidth(c, width);
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
/// A dynamic tab (`%tab`) of a line: the cell before it ends before the unit `unitIndex`.
interface TabMark {
  /// The index of the first unit after the tab.
  unitIndex: number;
  /// The tab stop index.
  index: number;
  /// The alignment of the cell before the tab.
  alignment: LineAlignment;
  /// The (empty) view of the tab.
  view: MDCView;
}

/// The end tab of the last cell of a line, which has none.
const LAST_CELL = -1;

/// A tab cell: the units between two tabs (or between the start of the line and the first tab,
/// or between the last tab and the end of the line).
interface TabCell {
  /// The index of the tab stop where the cell starts.
  startTab: number;
  /// The index of the tab stop where the cell ends, or [LAST_CELL].
  endTab: number;
  /// The alignment of the cell (meaningless for the last cell: it takes the alignment of the line).
  alignment: LineAlignment;
  units: MDCView[][];
}

class LineAccumulator {
  readonly currentPoint: Point = { x: 0, y: 0 };
  /// The dynamic tabs of the line, in order.
  private readonly tabMarks: TabMark[] = [];
  private readonly zoneArea: Rect;
  private readonly views: MDCView[] = [];
  /// The views, grouped in units moved as a whole by justification (a run
  /// of text is one unit).
  private readonly units: MDCView[][] = [];
  /// The view of the line break which ends a column, if any: it gives the column a minimal
  /// size, but is not part of its visible content.
  private columnEnd: MDCView | undefined;

  constructor(minWidth: number, minHeight: number) {
    this.zoneArea = new Rect(0, 0, minWidth, minHeight);
  }

  add(view: MDCView): void {
    view.position = { x: this.currentPoint.x + view.deltaBaseX, y: this.currentPoint.y + view.deltaBaseY };
    this.zoneArea.add(new Rect(view.position.x, view.position.y, view.width, view.height));
    this.views.push(view);
    this.units.push([view]);
  }

  /// Adds the view of the line break which ends a column. It is placed as in [add], but the
  /// alignment of the column ignores it (it is moved with the column, as the line break of a
  /// line, which has no width).
  addColumnEnd(view: MDCView): void {
    view.position = { x: this.currentPoint.x + view.deltaBaseX, y: this.currentPoint.y + view.deltaBaseY };
    this.zoneArea.add(new Rect(view.position.x, view.position.y, view.width, view.height));
    this.views.push(view);
    this.columnEnd = view;
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

  /// Adds a dynamic tab (`%tab`): it ends the current tab cell.
  ///
  /// The view of the tab is placed at the current point, but doesn't count in the size of the
  /// line; [placeTabViews] moves it to its tab stop. An index which is not greater than the
  /// previous one is replaced by the previous one plus one.
  addTab(view: MDCView, requestedIndex: number, alignment: LineAlignment): number {
    const previous = this.tabMarks.length === 0 ? 0 : this.tabMarks[this.tabMarks.length - 1]!.index;
    const index = Tab.resolveIndex(requestedIndex, previous);
    if (requestedIndex > 0 && requestedIndex <= previous) {
      console.warn(`%tab[n=${requestedIndex}] after tab stop ${previous} in the same line: using ${index}`);
    }
    view.position = { x: this.currentPoint.x + view.deltaBaseX, y: this.currentPoint.y + view.deltaBaseY };
    this.views.push(view);
    this.tabMarks.push({ unitIndex: this.units.length, index, alignment, view });
    return index;
  }

  /// True if the line contains dynamic tabs.
  hasTabs(): boolean {
    return this.tabMarks.length > 0;
  }

  /// The tab cells of the line, in order; the last one is the cell after the last tab (possibly
  /// empty).
  tabCells(): TabCell[] {
    const cells: TabCell[] = [];
    let startTab = 0;
    let from = 0;
    for (const m of this.tabMarks) {
      cells.push({ startTab, endTab: m.index, alignment: m.alignment, units: this.units.slice(from, m.unitIndex) });
      startTab = m.index;
      from = m.unitIndex;
    }
    cells.push({ startTab, endTab: LAST_CELL, alignment: "START", units: this.units.slice(from) });
    return cells;
  }

  /// Moves the views of the tabs to their tab stops. `origin` is the start of the line (or the
  /// top of the column), `tabPositions` the position of each tab stop from the origin.
  placeTabViews(origin: number, tabPositions: number[], vertical: boolean): void {
    for (const m of this.tabMarks) {
      if (vertical) m.view.position.y = origin + tabPositions[m.index]!;
      else m.view.position.x = origin + tabPositions[m.index]!;
    }
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

  /// Places the content of the line in the width given to it, as an alignment requires. The
  /// line is supposed to be at the start (its first view at `startMargin`).
  alignTo(alignment: LineAlignment, startMargin: number, width: number): void {
    this.alignAlong(false, alignment, startMargin, width);
  }

  /// Places the content of a column in the height given to it, as an alignment requires
  /// (`START` is the top). The column is supposed to be at the top (its first view at
  /// `topMargin`).
  alignVerticallyTo(alignment: LineAlignment, topMargin: number, height: number): void {
    this.alignAlong(true, alignment, topMargin, height);
  }

  private alignAlong(vertical: boolean, alignment: LineAlignment, start: number, length: number): void {
    switch (alignment) {
      case "START":
        break;
      case "END": {
        const extent = this.visibleExtent(vertical);
        if (extent) this.shift(vertical, start + length - extent[1]);
        break;
      }
      case "CENTER":
        this.centerIn(vertical, start, length);
        break;
      case "JUSTIFY":
        this.justifyTo(vertical, start, length);
        break;
    }
  }

  /// Justifies the line to `width` (after `startMargin`), by distributing the free space between
  /// its units. The line stays as it is if it has only one unit (there is nothing to distribute),
  /// or if it is already wider than `width`.
  justifyWidthTo(startMargin: number, width: number): void {
    this.justifyTo(false, startMargin, width);
  }

  private justifyTo(vertical: boolean, start: number, length: number): void {
    justifyUnits(this.units, vertical, start, length);
  }

  /// The first and last coordinate, along an axis, of the non-empty views, or null if there are
  /// none.
  private visibleExtent(vertical: boolean): [number, number] | null {
    let currentStart = Infinity;
    let currentEnd = -Infinity;
    for (const v of this.views) {
      const l = v === this.columnEnd ? 0 : viewLength(v, vertical);
      if (l > 0) {
        currentStart = Math.min(currentStart, coordinate(v, vertical));
        currentEnd = Math.max(currentEnd, coordinate(v, vertical) + l);
      }
    }
    return currentStart > currentEnd ? null : [currentStart, currentEnd];
  }

  /// The width of the visible content of the line.
  get visibleWidth(): number {
    const extent = this.visibleExtent(false);
    return extent ? extent[1] - extent[0] : 0;
  }

  /// The visible area of the line: the x extent of its non-empty views, and the given vertical
  /// extent; null if it has no non-empty views.
  visibleBounds(minY: number, height: number): Rect | null {
    const extent = this.visibleExtent(false);
    return extent ? new Rect(extent[0], minY, extent[1] - extent[0], height) : null;
  }

  /// The visible area of a column: the y extent of its non-empty views, and the given
  /// horizontal extent; null if it has no non-empty views.
  visibleVerticalBounds(minX: number, width: number): Rect | null {
    const extent = this.visibleExtent(true);
    return extent ? new Rect(minX, extent[0], width, extent[1] - extent[0]) : null;
  }

  private shift(vertical: boolean, delta: number): void {
    this.translateBy(vertical ? { x: 0, y: delta } : { x: delta, y: 0 });
  }

  private centerIn(vertical: boolean, start: number, length: number): void {
    const extent = this.visibleExtent(vertical);
    if (!extent) return;
    const centerMargin = start + (length - (extent[1] - extent[0])) / 2;
    this.shift(vertical, centerMargin - extent[0]);
  }
}

function coordinate(v: MDCView, vertical: boolean): number {
  return vertical ? v.position.y : v.position.x;
}

function viewLength(v: MDCView, vertical: boolean): number {
  return vertical ? v.height : v.width;
}

function unitMin(unit: MDCView[], vertical: boolean): number {
  return Math.min(...unit.map((v) => coordinate(v, vertical)));
}

function unitLength(unit: MDCView[], vertical: boolean): number {
  if (unit.length === 1) return viewLength(unit[0]!, vertical);
  return Math.max(...unit.map((v) => coordinate(v, vertical) + viewLength(v, vertical))) - unitMin(unit, vertical);
}

/// Justifies some units: distributes the free space of `length` between the non-empty ones.
function justifyUnits(units: MDCView[][], vertical: boolean, start: number, length: number): void {
  let minimalLength = 0;
  const nonEmptyUnits: MDCView[][] = [];
  for (const unit of units) {
    const l = unitLength(unit, vertical);
    minimalLength += l;
    if (l !== 0) nonEmptyUnits.push(unit);
  }
  const space = length - minimalLength;
  if (nonEmptyUnits.length > 1 && space > 0) {
    const skip = space / (nonEmptyUnits.length - 1);
    let pos = start;
    for (const unit of nonEmptyUnits) {
      moveUnitTo(unit, pos, vertical);
      pos += unitLength(unit, vertical) + skip;
    }
  }
}

/// The first and last coordinate, along an axis, of the non-empty views of some units, or null
/// if there are none.
function unitsExtent(units: MDCView[][], vertical: boolean): [number, number] | null {
  let currentStart = Infinity;
  let currentEnd = -Infinity;
  for (const unit of units) {
    for (const v of unit) {
      const l = viewLength(v, vertical);
      if (l > 0) {
        currentStart = Math.min(currentStart, coordinate(v, vertical));
        currentEnd = Math.max(currentEnd, coordinate(v, vertical) + l);
      }
    }
  }
  return currentStart > currentEnd ? null : [currentStart, currentEnd];
}

/// The length, along an axis, of the visible content of some units.
function unitsLength(units: MDCView[][], vertical: boolean): number {
  const extent = unitsExtent(units, vertical);
  return extent === null ? 0 : extent[1] - extent[0];
}

/// Places some units (a tab cell) in an interval, as an alignment requires. The units keep their
/// relative positions, except for `JUSTIFY`, which spreads the free space between them.
function placeUnits(units: MDCView[][], alignment: LineAlignment, start: number, length: number, vertical: boolean): void {
  const extent = unitsExtent(units, vertical);
  if (extent === null) return;
  const size = extent[1] - extent[0];
  let target = start;
  if (alignment === "END") target = start + length - size;
  else if (alignment === "CENTER") target = start + (length - size) / 2;
  const delta = target - extent[0];
  for (const unit of units) {
    for (const v of unit) {
      if (vertical) v.position.y += delta;
      else v.position.x += delta;
    }
  }
  if (alignment === "JUSTIFY") justifyUnits(units, vertical, start, length);
}

/// A line (or column) with tabs, for [TabLayout].
interface TabbedLine {
  /// The line, already at its final place.
  line: LineAccumulator;
  /// The coordinate where the line starts (tab stop 0).
  origin: number;
  /// The alignment of the cell after the last tab (the alignment of the line).
  lastCellAlignment: LineAlignment;
}

/// Port of `TabLayout`: places the tab cells of the lines (or columns) of a zone on their tab
/// stops (see [Tab]). Shared by [LineLayout] and [ColumnLayout], which call it at the end of the
/// layout, once all lines are placed, and before the alignment of the other lines.
///
/// 1. the stops are computed from the natural size of the cells: a cell which ends at stop *i*
///    pushes it to its start, plus its size, plus the tab gap. A stop is never before the
///    previous one. An empty cell doesn't push anything.
/// 2. each cell is moved, as a block, to its interval: from its start stop to its end stop minus
///    the gap; for the last cell of a line, from its start stop to the end of the table.
///
/// Positions are along the x axis for lines, the y axis for columns, and are always computed from
/// the start of the line.
class TabLayout {
  /// `vertical` is true for columns; `gap` is the minimal space between a cell and the next tab
  /// stop.
  constructor(
    private readonly vertical: boolean,
    private readonly gap: number,
  ) {}

  /// Computes the tab stops of the lines and places their cells.
  ///
  /// The last cell of each line is aligned in the interval which ends at the end of the table:
  /// `availableLength` (the length imposed by the parent) if it is positive, else the longest of
  /// the tabbed lines and of `untabbedLength` (the length of the longest line without tabs).
  apply(lines: TabbedLine[], availableLength: number, untabbedLength: number): number[] {
    const cells = lines.map((l) => l.line.tabCells());
    let maxIndex = 0;
    for (const lineCells of cells) for (const c of lineCells) maxIndex = Math.max(maxIndex, c.endTab);
    const tabPos = this.tabPositions(cells, maxIndex);
    let end = availableLength > 0 ? availableLength : untabbedLength;
    if (availableLength <= 0) {
      for (const lineCells of cells) {
        const last = lineCells[lineCells.length - 1]!;
        end = Math.max(end, tabPos[last.startTab]! + this.size(last));
      }
    }
    lines.forEach((l, i) => {
      for (const c of cells[i]!) {
        const start = tabPos[c.startTab]!;
        const isLast = c.endTab === LAST_CELL;
        const cellEnd = isLast ? end : tabPos[c.endTab]! - this.gap;
        const length = Math.max(cellEnd - start, this.size(c));
        const alignment = isLast ? l.lastCellAlignment : c.alignment;
        placeUnits(c.units, alignment, l.origin + start, length, this.vertical);
      }
      l.line.placeTabViews(l.origin, tabPos, this.vertical);
    });
    return tabPos;
  }

  /// The position of each tab stop, from 0 to `maxIndex`.
  private tabPositions(cells: TabCell[][], maxIndex: number): number[] {
    const endingAt: TabCell[][] = [];
    for (let i = 0; i <= maxIndex; i++) endingAt.push([]);
    for (const lineCells of cells) {
      for (const c of lineCells) if (c.endTab !== LAST_CELL) endingAt[c.endTab]!.push(c);
    }
    const tabPos: number[] = new Array<number>(maxIndex + 1).fill(0);
    for (let i = 1; i <= maxIndex; i++) {
      let pos = tabPos[i - 1]!;
      for (const c of endingAt[i]!) {
        const size = this.size(c);
        if (size > 0) pos = Math.max(pos, tabPos[c.startTab]! + size + this.gap);
      }
      tabPos[i] = pos;
    }
    return tabPos;
  }

  private size(c: TabCell): number {
    return unitsLength(c.units, this.vertical);
  }
}

/// Moves a unit rigidly so that its start (left or top) is at `pos`.
function moveUnitTo(unit: MDCView[], pos: number, vertical: boolean): void {
  const delta = pos - unitMin(unit, vertical);
  for (const v of unit) {
    if (vertical) v.position.y += delta;
    else v.position.x += delta;
  }
}

/// Port of `TopItemLayout`: places the top items of the text.
interface TopItemLayout {
  startLayout(): void;
  /// Lays out the dynamic tabs (`%tab`) of the text, with this minimal space between a tab cell
  /// and the next tab stop. They are only laid out in zones; elsewhere, they are ignored.
  enableTabs(gap: number): void;
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
/// A line placed in the document: its views, and its vertical extent in document coordinates.
interface PlacedLine {
  line: LineAccumulator;
  top: number;
  height: number;
}

class LineLayout extends ModelElementAdapter implements TopItemLayout {
  readonly documentArea = new Rect();
  private zone: LineAccumulator = new LineAccumulator(0, 0);
  /// The lines placed so far (with their vertical extent), and the alignment they will get in
  /// `endLayout`.
  private readonly alignedLines: PlacedLine[] = [];
  private readonly alignments: LineAlignment[] = [];
  /// The lines of the current logical line (from the previous line break): several when it is
  /// soft-wrapped. Their alignment is only known at the line break which ends it.
  private readonly currentLogicalLine: PlacedLine[] = [];
  /// The alignment of the last line ended by a line break: the last line of the text, if it
  /// has no line break, takes it.
  private previousAlignment: LineAlignment = "START";
  /// The vertical extent (top, bottom) of each line of text, for the separators.
  private readonly lineExtents: [number, number][] = [];
  /// Where the current zone will be placed.
  private zoneOriginPosition: Point = { x: 0, y: 0 };
  private readonly currentTextDirection: TextDirection;
  private subView!: MDCView;
  /// True for the first line of a page, which gets the top margin.
  private addTopMargin = true;
  /// The page of the last line placed, when the text is paginated.
  private pageIndex = 0;
  /// In lines, only `%hkern` is meaningful.
  private readonly kerning: TopLevelKerning;
  /// The minimal space between a tab cell and the next tab stop, or negative if dynamic tabs are
  /// not laid out.
  private tabGap = -1;
  /// Has the current logical line been soft-wrapped? Only the tabs before its first soft wrap are
  /// active.
  private wrappedLogicalLine = false;
  /// The lines whose cells were placed by [TabLayout]: [alignLines] leaves them alone.
  private readonly tabbedLines = new Set<LineAccumulator>();

  constructor(
    private readonly documentView: MDCView,
    private readonly style: JSeshStyle,
    kerner: Kerner,
  ) {
    super();
    this.currentTextDirection = style.options.textDirection;
    this.kerning = new TopLevelKerning(kerner, "HORIZONTAL", style.geometry.smallSkip);
  }

  enableTabs(gap: number): void {
    this.tabGap = Math.max(0, gap);
  }

  private get tabsEnabled(): boolean {
    return this.tabGap >= 0;
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
    this.endLogicalLine(this.previousAlignment);
    this.layoutTabs();
    if (!this.style.options.ignoreLineAlignment) this.alignLines();
    if (isPaginated(g.page)) {
      // The view covers all the pages.
      this.documentArea.add(new Rect(0, 0, g.page.pageWidth, this.pageIndex * pageStride(g.page) + g.page.pageHeight));
    }
    this.documentArea.addPoint(this.documentArea.maxX + g.leftMargin, this.documentArea.minY + g.topMargin);
  }

  /// Ends the current logical line: gives its alignment to its lines. When a justified logical
  /// line is soft-wrapped, its last line is placed at the start, as the last line of a paragraph.
  /// Otherwise, justification always applies.
  private endLogicalLine(alignment: LineAlignment): void {
    this.currentLogicalLine.forEach((line, i) => {
      let a = alignment;
      const lastOfWrapped = i > 0 && i === this.currentLogicalLine.length - 1;
      if (a === "JUSTIFY" && lastOfWrapped) a = "START";
      this.alignedLines.push(line);
      this.alignments.push(a);
    });
    this.currentLogicalLine.length = 0;
  }

  /// Places the cells of the lines which have tabs on their tab stops (see [TabLayout]). The cell
  /// after the last tab takes the alignment of the line, in the width of the parent if it has
  /// one, else in the width of the longest line.
  private layoutTabs(): void {
    if (!this.tabsEnabled) return;
    const g = this.style.geometry;
    const margin = g.leftMargin;
    const ignoreAlignment = this.style.options.ignoreLineAlignment;
    const lines: TabbedLine[] = [];
    let untabbedWidth = 0;
    this.alignedLines.forEach((placed, i) => {
      const line = placed.line;
      if (line.hasTabs()) {
        lines.push({ line, origin: margin, lastCellAlignment: ignoreAlignment ? "START" : this.alignments[i]! });
        this.tabbedLines.add(line);
      } else {
        const bounds = line.visibleBounds(0, 0);
        if (bounds) untabbedWidth = Math.max(untabbedWidth, bounds.maxX - margin);
      }
    });
    if (lines.length === 0) return;
    const available = isWrapping(g) ? this.maxLineWidth() : 0;
    new TabLayout(false, this.tabGap).apply(lines, available, untabbedWidth);
    for (const l of this.alignedLines) {
      if (!this.tabbedLines.has(l.line)) continue;
      const bounds = l.line.visibleBounds(l.top, l.height);
      if (bounds) this.documentArea.add(bounds);
    }
  }

  /// Places the lines according to their alignment, in the width of the parent if it has one (a
  /// page, or a zone with a width), else in the width of the longest line.
  private alignLines(): void {
    if (!this.alignments.some((a) => a !== "START")) return;
    const g = this.style.geometry;
    let width = 0;
    if (isWrapping(g)) {
      width = this.maxLineWidth();
    } else {
      // The right side of the visible content of the longest line (lines are placed at the margin).
      for (const l of this.alignedLines) {
        const bounds = l.line.visibleBounds(0, 0);
        if (bounds) width = Math.max(width, bounds.maxX - g.leftMargin);
      }
    }
    this.alignedLines.forEach((l, i) => {
      const alignment = this.alignments[i]!;
      if (alignment === "START" || this.tabbedLines.has(l.line)) return;
      l.line.alignTo(alignment, g.leftMargin, width);
      const bounds = l.line.visibleBounds(l.top, l.height);
      if (bounds) this.documentArea.add(bounds);
    });
  }

  /// The width available for the content of a line when wrapping, i.e. the
  /// wrapping width, minus the margins on both sides.
  private maxLineWidth(): number {
    return maxLineWidth(this.style.geometry);
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
    this.wrappedLogicalLine = true;
    const verticalSkip = this.zone.height + this.zone.minY + this.style.geometry.lineSkip;
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
    this.alignedLines.length = 0;
    this.alignments.length = 0;
    this.currentLogicalLine.length = 0;
    this.tabbedLines.clear();
    this.wrappedLogicalLine = false;
    this.previousAlignment = "START";
    this.pageIndex = 0;
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

  /// Moves the current line to the top of the next page if it doesn't fit in the current one.
  /// A line which is alone on its page stays, however tall (`LineLayout.avoidPageBottom`).
  private avoidPageBottom(marginy: number): void {
    const page = this.style.geometry.page;
    const pageTop = this.pageIndex * pageStride(page);
    const top = this.zoneOriginPosition.y + marginy;
    const bottom = top + this.zone.height;
    const limit = pageTop + page.pageHeight - page.marginBottom;
    if (bottom > limit && top > pageTop + page.marginTop + 1e-3) {
      this.pageIndex++;
      this.zoneOriginPosition.y = this.pageIndex * pageStride(page) + page.marginTop - marginy;
    }
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
    if (textLine && isPaginated(g.page)) this.avoidPageBottom(marginy);
    this.zoneOriginPosition.y = this.zoneOriginPosition.y + marginy - miny;
    this.zoneOriginPosition.x = marginx - minx;
    this.zone.translateBy(this.zoneOriginPosition);
    this.documentArea.add(
      new Rect(this.zoneOriginPosition.x + minx, this.zoneOriginPosition.y + miny, this.zone.width, this.zone.height),
    );
    if (textLine) {
      this.lineExtents.push([this.zoneOriginPosition.y + miny, this.zoneOriginPosition.y + miny + this.zone.height]);
      this.currentLogicalLine.push({ line: this.zone, top: this.zoneOriginPosition.y + miny, height: this.zone.height });
    }
  }

  override visitLineBreak(b: LineBreak): void {
    this.kerning.reset();
    const g = this.style.geometry;
    // Top of the next line, relative to the top of this one.
    let verticalSkip = this.zone.height + this.zone.minY + g.lineSkip;
    if (b.spacing !== 100) verticalSkip = (verticalSkip * b.spacing) / 100;
    this.zone.add(this.subView);
    this.flushZone();
    this.endLogicalLine(b.alignment);
    this.previousAlignment = b.alignment;
    this.wrappedLogicalLine = false;
    this.startNextZone(verticalSkip);
  }

  override visitPageBreak(b: PageBreak): void {
    this.kerning.reset();
    const g = this.style.geometry;
    const dh = this.zone.height;
    this.flushZone();
    this.endLogicalLine(b.alignment);
    this.previousAlignment = b.alignment;
    this.wrappedLogicalLine = false;
    if (isPaginated(g.page)) {
      // Pages are stacked: the next line goes to the top of the next page.
      this.pageIndex++;
      this.zone = new LineAccumulator(0, g.maxCadratHeight);
      this.zoneOriginPosition.y = this.pageIndex * pageStride(g.page);
    } else if (!this.style.options.paged) {
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
  override visitAbsoluteTab(tab: AbsoluteTab): void {
    this.kerning.reset();
    if (this.tabsEnabled && this.zone.hasTabs()) {
      // Absolute positions mean nothing after a dynamic tab.
      this.zone.add(this.subView);
      return;
    }
    const pos = f(tab.stopPos * this.style.geometry.tabUnitWidth);
    this.zone.currentPoint.x = pos;
    this.zone.currentPoint.y = 0;
    this.zone.add(this.subView);
  }

  /// A dynamic tab ends the current tab cell, if tabs are laid out, and if the logical line has
  /// not been soft-wrapped yet. Otherwise, it is an empty element.
  override visitTab(tab: Tab): void {
    this.kerning.reset();
    if (this.tabsEnabled && !this.wrappedLogicalLine) this.zone.addTab(this.subView, tab.index, tab.alignment);
    else this.zone.add(this.subView);
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
  /// The columns placed so far (their views, the y where their content starts, and their
  /// horizontal extent), with the alignment they will get in [endLayout].
  private readonly placedColumns: { column: LineAccumulator; top: number; left: number; width: number }[] = [];
  /// Their alignments (same indexes as [placedColumns]).
  private readonly alignments: LineAlignment[] = [];
  /// The alignment of the last column ended by a line break: the last column of the text, if it
  /// has no line break, takes it.
  private previousAlignment: LineAlignment = "START";
  /// The minimal space between a tab cell and the next tab stop, or negative if dynamic tabs are
  /// not laid out.
  private tabGap = -1;
  /// The columns whose cells were placed by [TabLayout]: [alignColumns] leaves them alone.
  private readonly tabbedColumns = new Set<LineAccumulator>();

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

  enableTabs(gap: number): void {
    this.tabGap = Math.max(0, gap);
  }

  private get tabsEnabled(): boolean {
    return this.tabGap >= 0;
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
    if (height !== 0) this.zone.moveCurrentPoint(0, height + this.style.geometry.columnQuadratSkip);
  }

  endLayout(): void {
    if (!this.zone.isEmpty()) this.flushZone(this.previousAlignment);
    this.layoutTabs();
    this.alignColumns();
    this.documentArea.addPoint(this.documentArea.maxX + this.style.geometry.leftMargin, this.documentArea.minY);
  }

  separatorPositions(): number[] {
    return separatorsBetween(this.columnExtents);
  }

  startLayout(): void {
    this.columnExtents.length = 0;
    this.placedColumns.length = 0;
    this.alignments.length = 0;
    this.tabbedColumns.clear();
    this.previousAlignment = "START";
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

  /// Places the cells of the columns which have tabs on their tab stops (see [TabLayout]). The
  /// cell after the last tab takes the alignment of the column, in the height of the parent if it
  /// has one, else in the height of the longest column.
  private layoutTabs(): void {
    if (!this.tabsEnabled) return;
    const columns: TabbedLine[] = [];
    let untabbedHeight = 0;
    this.placedColumns.forEach((c, i) => {
      if (c.column.hasTabs()) {
        columns.push({ line: c.column, origin: c.top, lastCellAlignment: this.alignments[i]! });
        this.tabbedColumns.add(c.column);
      } else {
        const bounds = c.column.visibleVerticalBounds(0, 0);
        if (bounds) untabbedHeight = Math.max(untabbedHeight, bounds.maxY - c.top);
      }
    });
    if (columns.length === 0) return;
    new TabLayout(true, this.tabGap).apply(columns, maxColumnHeight(this.style.geometry), untabbedHeight);
    for (const c of this.placedColumns) {
      if (!this.tabbedColumns.has(c.column)) continue;
      const bounds = c.column.visibleVerticalBounds(c.left, c.width);
      if (bounds) this.documentArea.add(bounds);
    }
  }

  /// Places the columns according to their alignment, in the height of the parent if it has one
  /// (a page with a height, or a zone with a height), else in the height of the longest column.
  /// Unlike lines, columns keep their alignment when the text is wrapped to the width of an
  /// editor (their height doesn't depend on it).
  private alignColumns(): void {
    if (this.alignments.every((a) => a === "START")) return;
    let height = maxColumnHeight(this.style.geometry);
    if (height <= 0) {
      // The bottom of the visible content of the longest column.
      for (const c of this.placedColumns) {
        const bounds = c.column.visibleVerticalBounds(0, 0);
        if (bounds) height = Math.max(height, bounds.maxY - c.top);
      }
    }
    this.placedColumns.forEach((c, i) => {
      const alignment = this.alignments[i]!;
      if (alignment === "START" || this.tabbedColumns.has(c.column)) return;
      c.column.alignVerticallyTo(alignment, c.top, height);
      const bounds = c.column.visibleVerticalBounds(c.left, c.width);
      if (bounds) this.documentArea.add(bounds);
    });
  }

  private flushZone(alignment: LineAlignment): void {
    // The zone's top left is normally 0,0, unless it's higher than expected.
    this.zoneStart.y += -this.zone.minY;
    this.zoneStart.x += -this.zone.minX;
    this.zone.translateBy(this.zoneStart);
    this.columnExtents.push([this.zoneStart.x, this.zoneStart.x + this.zone.width]);
    this.placedColumns.push({
      column: this.zone,
      top: this.zoneStart.y + this.style.geometry.topMargin,
      left: this.zoneStart.x,
      width: this.zone.width,
    });
    this.alignments.push(alignment);
    this.documentArea.add(new Rect(this.zoneStart.x, this.zoneStart.y, this.zone.width, this.zone.height));
  }

  override visitLineBreak(b: LineBreak): void {
    this.endColumn(b.alignment);
  }

  /// Ends the current column (at a line break, or a page break).
  private endColumn(alignment: LineAlignment): void {
    this.kerning.reset();
    // A minimal height, else empty columns would have no height.
    this.subView.height = 0.1;
    this.subView.width = this.style.geometry.maxCadratWidth;
    this.zone.addColumnEnd(this.subView);
    this.flushZone(alignment);
    this.previousAlignment = alignment;
    this.zoneStart.x += this.zone.width + this.style.geometry.columnSkip;
    this.zone = this.createNewZone();
  }

  override visitPageBreak(b: PageBreak): void {
    if (!this.style.options.paged) this.endColumn(b.alignment);
  }

  override visitAbsoluteTab(t: AbsoluteTab): void {
    this.visitDefault(t);
  }

  /// A dynamic tab ends the current tab cell, if tabs are laid out. Otherwise, it is an empty
  /// element.
  override visitTab(t: Tab): void {
    this.kerning.reset();
    if (this.tabsEnabled) this.zone.addTab(this.subView, t.index, t.alignment);
    else this.visitDefault(t);
  }

  override visitDefault(_t: ModelElement): void {
    if (!this.kerning.add(this.zone, this.subView)) return;
    if (this.subView.height !== 0) this.zone.moveCurrentPoint(0, this.subView.height + this.style.geometry.columnQuadratSkip);
  }
}

/// Mirrors outlines across the vertical axis of a box of a given width.
function mirrorOutlines(outlines: Outline[], width: number): Outline[] {
  return outlines.map((o) => ({ ...o, path: transformPath(o.path, [-1, 0, 0, 1, width, 0]) }));
}
