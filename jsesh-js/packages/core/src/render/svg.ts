/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Draws a view tree as SVG: port of `jsesh.render.draw.ViewDrawer` and
/// `DefaultElementDrawer`.
///
/// Each view is drawn in its parent's system, at its own origin; its scale
/// applies to its subviews. Coordinates are in points.

import { ShadingCode, SymbolCodes } from "../model/constants.js";
import { TabLine } from "../model/tab-line.js";
import {
  AlphabeticCharacter,
  Cadrat,
  Cartouche,
  Hieroglyph,
  HieroglyphicText,
  HRule,
  Kerning,
  ModelElementAdapter,
  PageBreak,
  Philology,
  RunningAnnotation,
  Superscript,
  Tab,
  TopItem,
  TopItemState,
  Zone,
  ZoneContent,
  ZONE_UNSET,
  zoneScaleFactor,
} from "../model/model.js";
import { drawCartouche } from "./cartouches.js";
import { HieroglyphDrawer, type SignShape } from "./glyphs.js";
import { parsePath, pathBounds, rotation, transformPath } from "./path.js";
import { zoneStyle } from "./layout.js";
import { cssFont, fontForScript, isPaginated, percentUnit, philologyWidth, type FontDescription, type JSeshStyle } from "./style.js";
import { escapeXml, filledPath, num, PathBuilder, strokedPath } from "./svg-writer.js";
import { drawSpecialSymbol } from "./symbols.js";
import { displayString, type TextMeasurer } from "./text.js";
import { Rect, type MDCView } from "./view.js";


/// The colour of the marks of dynamic tabs.
const TAB_COLOR = "#1e64e6";

export interface DrawContext {
  style: JSeshStyle;
  drawer: HieroglyphDrawer;
  measurer: TextMeasurer;
}

export interface SvgOptions {
  /// Size of a point, in CSS pixels (default 1).
  scale?: number;
  /// Accessible description of the picture (usually the MdC text).
  title?: string;
  /// Prefix for the ids defined in the picture, which must be unique in the
  /// page (default `jsesh`).
  idPrefix?: string;
  /// Fill the background with the style's background colour.
  background?: boolean;
  /// Extra attributes for the `svg` element, already escaped.
  attributes?: string;
  /// Draw kerning operators as marks (as paragraph marks in a word processor). Meant for
  /// editors; false by default.
  showKerningMarks?: boolean;
  /// Draw dynamic tabs (`%tab`) as marks: a blue line across the text at the tab stop, with the
  /// index of the stop. Meant for editors; false by default.
  showTabs?: boolean;
}

/// Draws a view tree (usually of a whole text) as an SVG document.
export function drawSvg(view: MDCView, context: DrawContext, options: SvgOptions = {}): string {
  const scale = options.scale ?? 1;
  const width = Math.ceil(view.width) + 2;
  const height = Math.ceil(view.height) + 2;
  const prefix = options.idPrefix ?? "jsesh";
  const drawer = new SvgViewDrawer(context, prefix, options.showKerningMarks ?? false, options.showTabs ?? false);
  const content = drawer.draw(view);
  const title = options.title === undefined ? "" : `<title>${escapeXml(options.title)}</title>`;
  const role = options.title === undefined ? ' aria-hidden="true"' : ' role="img"';
  const background = options.background ? `<rect width="100%" height="100%" fill="${context.style.painting.backgroundColor}"/>` : "";
  return (
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${width} ${height}" width="${num(width * scale)}" ` +
    `height="${num(height * scale)}"${role}${options.attributes ? " " + options.attributes : ""}>` +
    title +
    drawer.defs() +
    background +
    content +
    drawer.hatching() +
    "</svg>"
  );
}

/// A transform from the current system to the page: scale, then translation.
interface PageTransform {
  sx: number;
  sy: number;
  tx: number;
  ty: number;
}

class SvgViewDrawer {
  /// The style of the views being drawn: inside a zone, the zone's (see `drawView`).
  private style: JSeshStyle;
  private readonly drawer: HieroglyphDrawer;
  private readonly measurer: TextMeasurer;
  private drawingState = new TopItemState();
  /// The underline of the next view drawn (see `underlineFor`).
  private pendingUnderline: { left: number; right: number } | undefined;
  /// Areas shaded with lines, in page coordinates (drawn at the end).
  private readonly hatched: Rect[] = [];
  /// The shapes used, each defined once: id for each code.
  private readonly shapeIds = new Map<string, string>();
  private shapeDefs = "";

  constructor(
    context: DrawContext,
    private readonly prefix: string,
    readonly showKerningMarks: boolean,
    readonly showTabs: boolean = false,
  ) {
    this.style = context.style;
    this.drawer = context.drawer;
    this.measurer = context.measurer;
  }

  draw(view: MDCView): string {
    return this.drawView(view, 0, { sx: 1, sy: 1, tx: 0, ty: 0 });
  }

  /// A reference to the shape of a sign, defined in `defs` (its `fill`
  /// is inherited).
  useShape(code: string, shape: SignShape, color: string): string {
    let id = this.shapeIds.get(code);
    if (id === undefined) {
      id = `${this.prefix}-s${this.shapeIds.size}`;
      this.shapeIds.set(code, id);
      this.shapeDefs += `<path id="${id}" d="${shape.d}"${shape.evenOdd ? ' fill-rule="evenodd"' : ""}/>`;
    }
    return `<use href="#${id}" fill="${color}"/>`;
  }

  defs(): string {
    let result = this.shapeDefs;
    if (this.hatched.length > 0) result += this.hatchingPattern();
    return result === "" ? "" : `<defs>${result}</defs>`;
  }

  private hatchingPattern(): string {
    // `Shader`: lines of 0.5 pt, going up at 45°, on x + y = 3n (in page
    // coordinates), so 3/√2 apart.
    const period = num(3 / Math.SQRT2);
    return (
      `<pattern id="${this.prefix}-hatching" patternUnits="userSpaceOnUse" width="${period}" height="${period}" ` +
      `patternTransform="rotate(45) translate(-0.25 0)"><rect width="0.5" height="${period}" ` +
      `fill="${this.style.painting.blackColor}"/></pattern>`
    );
  }

  hatching(): string {
    if (this.hatched.length === 0) return "";
    const d = new PathBuilder();
    for (const r of this.hatched) d.rect(r.x, r.y, r.width, r.height);
    return `<path d="${d}" fill="url(#${this.prefix}-hatching)"/>`;
  }

  private drawView(v: MDCView, depth: number, page: PageTransform): string {
    // The items of a zone have their own state, as those of the text.
    if (v.model instanceof TopItem && (depth === 1 || v.model.parent instanceof ZoneContent)) {
      this.drawingState = v.model.state;
    }
    // The content of a zone is drawn in the style it was laid out in (its sign height, its
    // cartouche lines...).
    const outerStyle = this.style;
    if (v.model instanceof ZoneContent && v.model.parent instanceof Zone) {
      this.style = zoneStyle(outerStyle, v.model.parent.parameters);
    }
    try {
      return this.drawViewIn(v, depth, page);
    } finally {
      this.style = outerStyle;
    }
  }

  private drawViewIn(v: MDCView, depth: number, page: PageTransform): string {
    // With "underline for red", red parts are drawn in black (the quadrats are underlined).
    const painting = this.style.painting;
    const color = this.drawingState.isRed && !painting.underlineRed ? painting.redColor : painting.blackColor;
    const element = new ElementDrawer(this, v, color, page);
    element.underline = this.pendingUnderline;
    this.pendingUnderline = undefined;
    let result = element.draw(false);
    // The subviews, in the view's inner system.
    let inner = "";
    const innerPage = { sx: page.sx * v.xScale, sy: page.sy * v.yScale, tx: page.tx, ty: page.ty };
    for (let i = 0; i < v.numberOfSubviews; i++) {
      const b = v.getSubViewBounds(i);
      const subPage = {
        sx: innerPage.sx,
        sy: innerPage.sy,
        tx: innerPage.tx + b.x * innerPage.sx,
        ty: innerPage.ty + b.y * innerPage.sy,
      };
      this.pendingUnderline = this.underlineFor(v, i, depth);
      const sub = this.drawView(v.getSubView(i), depth + 1, subPage);
      if (sub !== "") inner += group(translate(b.x, b.y), sub);
    }
    if (inner !== "") result += group(scale(v.xScale, v.yScale), inner);
    result += element.draw(true);
    return result;
  }

  /// Port of `ViewDrawer.prepareUnderline`: is the subview `i` of `parent` underlined ("underline
  /// for red")? Only the red top-level quadrats are; a run of them on a line is underlined
  /// continuously, the line extending over the gap to the next red quadrat.
  private underlineFor(parent: MDCView, i: number, parentDepth: number): { left: number; right: number } | undefined {
    if (!this.style.painting.underlineRed) return undefined;
    const isRedQuadrat = (view: MDCView) => view.model instanceof Cadrat && view.model.state.isRed;
    if (!isRedQuadrat(parent.getSubView(i)) || !(parentDepth + 1 === 1 || parent.model instanceof ZoneContent)) {
      return undefined;
    }
    let left = 0;
    let right = 0;
    if (i + 1 < parent.numberOfSubviews && isRedQuadrat(parent.getSubView(i + 1))) {
      const here = parent.getSubViewBounds(i);
      const next = parent.getSubViewBounds(i + 1);
      // Same line: the quadrats overlap vertically, but not horizontally.
      if (next.y < here.y + here.height && here.y < next.y + next.height) {
        if (next.x >= here.x + here.width) right = next.x - (here.x + here.width);
        else if (next.x + next.width <= here.x) left = here.x - (next.x + next.width);
      }
    }
    return { left, right };
  }

  // Accessors for ElementDrawer.
  get s(): JSeshStyle {
    return this.style;
  }

  get signs(): HieroglyphDrawer {
    return this.drawer;
  }

  get textMeasurer(): TextMeasurer {
    return this.measurer;
  }

  get state(): TopItemState {
    return this.drawingState;
  }

  hatch(r: Rect): void {
    this.hatched.push(r);
  }
}

function translate(x: number, y: number): string {
  return x === 0 && y === 0 ? "" : `translate(${num(x)} ${num(y)})`;
}

function scale(sx: number, sy: number): string {
  if (sx === 1 && sy === 1) return "";
  return sx === sy ? `scale(${num(sx)})` : `scale(${num(sx)} ${num(sy)})`;
}

function group(transform: string, content: string): string {
  return transform === "" ? content : `<g transform="${transform}">${content}</g>`;
}

function text(s: string, x: number, y: number, font: FontDescription, color: string): string {
  if (s.trim() === "") return "";
  return `<text x="${num(x)}" y="${num(y)}" fill="${color}" style="font:${escapeXml(cssFont(font))};white-space:pre">${escapeXml(s)}</text>`;
}

/// Draws the element of one view (port of `DefaultElementDrawer`). Like in
/// Java, an element can draw before its subviews (`postfix` false: shading
/// of signs) and after them.
class ElementDrawer extends ModelElementAdapter {
  private postfix = false;
  private out = "";
  /// Set if the view is underlined ("underline for red"): how far the line extends sideways.
  underline: { left: number; right: number } | undefined;

  constructor(
    private readonly d: SvgViewDrawer,
    private readonly view: MDCView,
    private color: string,
    private readonly page: PageTransform,
  ) {
    super();
  }

  draw(postfix: boolean): string {
    this.postfix = postfix;
    this.out = "";
    this.view.model.accept(this);
    return this.out;
  }

  /// A character, with its baseline at its font's ascent.
  override visitAlphabeticCharacter(c: AlphabeticCharacter): void {
    if (!this.postfix) return;
    const fonts = this.d.s.fonts;
    const s = displayString(c, fonts);
    const m = this.d.textMeasurer.characterMetrics(fonts, c.script, s);
    this.out += text(s, 0, m.ascent, fontForScript(fonts, c.script), this.color);
  }

  override visitCartouche(c: Cartouche): void {
    if (!this.postfix) return;
    this.out += drawCartouche(c, this.d.s, this.view.width, this.view.height, this.color,
      this.view.direction ?? this.d.s.options.textDirection,
      this.view.orientation ?? this.d.s.options.textOrientation,
    );
  }

  override visitCadrat(c: Cadrat): void {
    // Shading is drawn after the signs (shadeAfter is true by default).
    if (this.postfix) this.doShade(c.shading);
    // Monochrome convention: red quadrats are underlined (they are drawn in black); the line
    // goes `smallSkip` below the quadrat.
    if (!this.postfix && this.underline) {
      const g = this.d.s.geometry;
      const y = this.view.height + g.underlineGap;
      this.out += strokedPath(
        new PathBuilder().line(-this.underline.left, y, this.view.width + this.underline.right, y),
        this.color,
        g.fineLineWidth,
      );
    }
  }

  override visitHieroglyph(h: Hieroglyph): void {
    const style = this.d.s;
    if (!this.postfix) {
      if (h.modifiers.hasInteger("shading")) {
        this.doShade(parseShading(String(h.modifiers.getIntegerWithDefault("shading", 0))));
      }
      return;
    }
    for (const m of h.modifiers.modifiers) {
      const tagColor = style.painting.colorMap[m.name];
      if (tagColor !== undefined) this.color = tagColor;
    }
    if (h.modifiers.getBoolean("red") && !style.painting.underlineRed) this.color = style.painting.redColor;
    if (h.modifiers.getBoolean("i")) this.color = style.painting.grayColor;
    switch (h.type) {
      case SymbolCodes.SMALLTEXT: {
        const smallText = h.getSmallText();
        const r = this.d.textMeasurer.smallTextDimensions(style.fonts, smallText);
        this.out += text(smallText, 0, r.height, style.fonts.superScriptFont, this.color);
        break;
      }
      case SymbolCodes.REDPOINT:
        this.out += this.sign(h.code, 0, false, this.d.signs.scaleFromFontToStyle(style), style.painting.redColor);
        break;
      case SymbolCodes.HALFSPACE:
      case SymbolCodes.FULLSPACE:
        break;
      case SymbolCodes.FULLSHADE:
      case SymbolCodes.VERTICALSHADE:
      case SymbolCodes.HORIZONTALSHADE:
      case SymbolCodes.QUATERSHADE:
        this.shadeArea([new Rect(0, 0, this.view.width, this.view.height)]);
        break;
      default: {
        const special = h.type >= SymbolCodes.BEGINERASE && h.type <= SymbolCodes.ENDDUBIOUS;
        // Inside a zone, the direction may differ from the document's: the view knows it.
        const reversed = h.isReversed !== (this.view.direction === "RIGHT_TO_LEFT");
        const baseSignScale = special ? 1 : this.d.signs.scaleFromFontToStyle(style);
        this.out += this.sign(h.code, h.angle, reversed, baseSignScale, this.color);
      }
    }
  }

  /// A sign (`DefaultElementDrawer.drawSign`, `HieroglyphDrawer.draw`).
  private sign(code: string, angle: number, reversed: boolean, baseSignScale: number, color: string): string {
    const v = this.view;
    const style = this.d.s;
    const signs = this.d.signs;
    let result: string;
    if (signs.isKnown(code)) {
      if (HieroglyphDrawer.isSpecial(code)) {
        result = drawSpecialSymbol(code, angle, v.width, v.height, v.xScale, v.yScale, color);
      } else {
        const shape = signs.getShape(code);
        if (shape === undefined) {
          // Not loaded (yet), or a shading code: nothing to draw.
          result = "";
        } else {
          let path = this.d.useShape(signs.provider.canonicalCode(code), shape, color);
          if (angle !== 0) {
            const b = pathBounds(transformPath(parsePath(shape.d), rotation(angle)));
            path = group(`${translate(-b.minX, -b.minY)} rotate(${num(angle)})`.trim(), path);
          }
          result = group(scale(baseSignScale * v.xScale, baseSignScale * v.yScale), path);
        }
      }
    } else {
      // Unknown sign: its code, in red.
      const r = this.d.textMeasurer.smallTextDimensions(style.fonts, code);
      result = text(code, 0, r.height, style.fonts.superScriptFont, style.painting.redColor);
    }
    if (reversed && result !== "") {
      // The right side is at the view's width.
      result = group(`matrix(-1 0 0 1 ${num(v.width)} 0)`, result);
    }
    return result;
  }

  /// Kerning operators are invisible, unless kerning marks are shown. The mark, `>|<` for
  /// `%hkern`, turned for `%vkern`, is centred on the view, which has no size.
  override visitKerning(kerning: Kerning): void {
    if (!this.postfix || !this.d.showKerningMarks) return;
    const style = this.d.s;
    const half = style.geometry.maxCadratHeight / 6;
    const tip = half / 2;
    // Drawn for %hkern; coordinates are swapped for %vkern.
    const lines = [
      [0, -half, 0, half],
      [-2 * tip, -tip / 2, -tip / 2, 0],
      [-tip / 2, 0, -2 * tip, tip / 2],
      [2 * tip, -tip / 2, tip / 2, 0],
      [tip / 2, 0, 2 * tip, tip / 2],
    ] as const;
    const horizontal = kerning.axis === "HORIZONTAL";
    const d = new PathBuilder();
    for (const [x1, y1, x2, y2] of lines) {
      if (horizontal) d.line(x1, y1, x2, y2);
      else d.line(y1, x1, y2, x2);
    }
    this.out += strokedPath(d, style.painting.cursorColor, style.geometry.fineLineWidth);
  }

  override visitHRule(h: HRule): void {
    if (!this.postfix) return;
    const g = this.d.s.geometry;
    // The view has the width of the text (see Layout.visitHRule); the positions of the rule are
    // percents of it, from the start of the text: the right edge in right-to-left text.
    const w = this.view.width;
    const rtl = (this.view.direction ?? this.d.s.options.textDirection) === "RIGHT_TO_LEFT";
    const start = rtl ? 1 - h.endFraction() : h.startFraction();
    const end = rtl ? 1 - h.startFraction() : h.endFraction();
    const d = new PathBuilder().line(start * w, 0, end * w, 0);
    this.out += strokedPath(d, this.color, h.type === "L" ? g.wideLineWidth : g.fineLineWidth);
  }

  /// Draws a running annotation: a line along the text, across the middle of its view. An
  /// haplography has an arrow head at each end, and a lacuna is dashed. In columns, the line is
  /// vertical.
  override visitRunningAnnotation(a: RunningAnnotation): void {
    if (!this.postfix) return;
    const g = this.d.s.geometry;
    const horizontal = (this.view.orientation ?? this.d.s.options.textOrientation) === "HORIZONTAL";
    const length = horizontal ? this.view.width : this.view.height;
    const across = (horizontal ? this.view.height : this.view.width) / 2;
    if (length <= 0) return;
    // Coordinates are (along, across); swapped for columns.
    const point = (along: number, cross: number): [number, number] => (horizontal ? [along, cross] : [cross, along]);
    const d = new PathBuilder();
    const line = (a1: number, c1: number, a2: number, c2: number) => d.line(...point(a1, c1), ...point(a2, c2));
    if (a.kind === "HAPLOGRAPHY") {
      const head = Math.min(g.maxCadratHeight / 5, length / 2);
      line(0, across, length, across);
      d.moveTo(...point(head, across - head / 2)).lineTo(...point(0, across)).lineTo(...point(head, across + head / 2));
      d.moveTo(...point(length - head, across - head / 2))
        .lineTo(...point(length, across))
        .lineTo(...point(length - head, across + head / 2));
      this.out += strokedPath(d, this.color, g.fineLineWidth);
    } else {
      const dash = Math.max(g.fineLineWidth * 4, g.maxCadratHeight / 20);
      line(0, across, length, across);
      this.out +=
        `<path d="${d}" fill="none" stroke="${this.color}" stroke-width="${num(g.fineLineWidth)}" ` +
        `stroke-dasharray="${num(dash)} ${num(dash)}" stroke-miterlimit="10"/>`;
    }
  }

  /// Dynamic tabs are invisible, unless tabs are shown. The mark is a blue line across the line
  /// (or column) at the tab stop, with a small flag towards the text which follows, and the stop
  /// index just above the line (or on the left of the column), where the signs don't hide it.
  override visitTab(tab: Tab): void {
    if (!this.postfix || !this.d.showTabs) return;
    const style = this.d.s;
    const g = style.geometry;
    const horizontal = (this.view.orientation ?? style.options.textOrientation) === "HORIZONTAL";
    const quadrat = g.maxCadratHeight;
    const length = horizontal ? quadrat : g.maxCadratWidth;
    const flag = quadrat / 8;
    const rtl = horizontal && (this.view.direction ?? style.options.textDirection) === "RIGHT_TO_LEFT";
    const dir = rtl ? -1 : 1;
    const d = new PathBuilder();
    if (horizontal) d.line(0, 0, 0, length).line(0, 0, flag * dir, 0);
    else d.line(0, 0, length, 0).line(0, 0, 0, flag);
    const color = TAB_COLOR;
    this.out += strokedPath(d, color, g.fineLineWidth);
    const size = quadrat / 4;
    const font = `font:${escapeXml(cssFont({ family: "sans-serif", size }))}`;
    const label = escapeXml(String(TabLine.stopIndexOf(tab)));
    // The index is outside the line (or column), so that the signs don't hide it.
    const [x, y, anchor] = horizontal ? [0, -size * 0.2, dir > 0 ? "start" : "end"] : [-flag / 4, size * 0.8, "end"];
    this.out += `<text x="${num(x)}" y="${num(y)}" fill="${color}" text-anchor="${anchor}" style="${font}">${label}</text>`;
  }

  /// A page break which doesn't start a new page is a line across the page.
  override visitPageBreak(_b: PageBreak): void {
    if (!this.postfix || this.d.s.options.paged || isPaginated(this.d.s.geometry.page)) return;
    this.out += strokedPath(new PathBuilder().line(-10000, 0, 10000, 0), this.color, this.d.s.geometry.fineLineWidth);
  }

  /// Philological brackets around a group (tksesh's reading of `[[...]]`).
  override visitPhilology(p: Philology): void {
    if (!this.postfix) return;
    const w = philologyWidth(p.type);
    this.out += this.bracket(p.type * 2 + 1, this.view.width - w);
    this.out += this.bracket(p.type * 2, 0);
  }

  private bracket(code: number, x: number): string {
    const s = BRACKETS[code] ?? "??";
    const width = philologyWidth(code);
    const height = this.view.height;
    // The bracket, stretched to the size of the group.
    const font = this.d.s.fonts.plainFont;
    return (
      `<text x="0" y="0" fill="${this.color}" textLength="${num(width)}" lengthAdjust="spacingAndGlyphs" ` +
      `transform="translate(${num(x)} ${num(height * 0.8)}) scale(1 ${num(height / font.size)})" ` +
      `style="font:${escapeXml(cssFont(font))}">${escapeXml(s)}</text>`
    );
  }

  override visitSuperscript(s: Superscript): void {
    if (!this.postfix) return;
    const style = this.d.s;
    const dims = this.d.textMeasurer.smallTextDimensions(style.fonts, s.text);
    const ascent = dims.ascent ?? dims.height * 0.8;
    this.out += text(s.text, 0, ascent, style.fonts.superScriptFont, this.color);
    const d = new PathBuilder().line(dims.width / 2, dims.height + style.geometry.smallSkip, dims.width / 2, this.view.internalHeight);
    this.out += strokedPath(d, this.color, style.geometry.fineLineWidth);
  }

  override visitHieroglyphicText(_t: HieroglyphicText): void {}

  /// Draws the frame of the zone and the rules between its lines (or
  /// columns), if it has them.
  override visitZone(zone: Zone): void {
    if (!this.postfix) return;
    const g = this.d.s.geometry;
    const v = this.view;
    if (zone.parameters.frame) {
      const w = g.cartoucheLineWidth;
      this.out += strokedPath(new PathBuilder().rect(w / 2, w / 2, v.width - w, v.height - w), this.color, w);
    }
    if (zone.parameters.separator !== ZONE_UNSET) this.drawSeparators(zone);
  }

  /// The rules between the lines (or columns) of a zone, over its whole
  /// inner width (or height).
  private drawSeparators(zone: Zone): void {
    const content = this.view.firstSubView;
    if (content.separators.length === 0) return;
    // The positions are in the coordinates of the content, which a zone with a scale scales.
    const scale = zoneScaleFactor(zone.parameters);
    const thickness = (zone.parameters.separator * percentUnit(this.d.s.geometry)) * scale;
    const horizontal = content.orientation === null || content.orientation === "HORIZONTAL";
    const { x: x0, y: y0 } = content.position;
    const d = new PathBuilder();
    for (const pos of content.separators) {
      if (horizontal) {
        d.line(x0, y0 + pos * scale, x0 + content.width, y0 + pos * scale);
      } else {
        // Columns are placed left to right; right-to-left ones are drawn mirrored.
        const x = content.layoutToDrawing(new Rect(pos, 0, 0, 0)).x * scale;
        d.line(x0 + x, y0, x0 + x, y0 + content.height);
      }
    }
    this.out += strokedPath(d, this.color, thickness);
  }

  /// Shades the quarters of the view (`DefaultElementDrawer.doShade`).
  private doShade(localShading: number): void {
    const v = this.view;
    const shaded = this.d.state.isShaded;
    if (shaded) localShading = ShadingCode.FULL;
    if (localShading === ShadingCode.NONE) return;
    const leftToRight = v.parent !== null && v.parent.direction === "LEFT_TO_RIGHT";
    const [topStart, topEnd, bottomStart, bottomEnd] = leftToRight
      ? [ShadingCode.TOP_START, ShadingCode.TOP_END, ShadingCode.BOTTOM_START, ShadingCode.BOTTOM_END]
      : [ShadingCode.TOP_END, ShadingCode.TOP_START, ShadingCode.BOTTOM_END, ShadingCode.BOTTOM_START];
    const w = v.width / 2;
    const h = v.height / 2;
    const areas: Rect[] = [];
    if (localShading & topStart) areas.push(new Rect(0, 0, w, h));
    if (localShading & topEnd) areas.push(new Rect(w, 0, w, h));
    if (localShading & bottomStart) areas.push(new Rect(0, h, w, h));
    if (localShading & bottomEnd) areas.push(new Rect(w, h, w, h));
    // Shade the space between this view and the next one.
    const next = v.next;
    const orientation = v.orientation ?? this.d.s.options.textOrientation;
    if (next !== null && shaded && orientation === "HORIZONTAL" && v.nextIsHorizontallyAdjacent()) {
      const outerWidth = next.position.x - v.position.x - v.width;
      areas.push(new Rect(v.width, 0, outerWidth / v.xScale, v.height));
    }
    this.shadeArea(areas);
  }

  private shadeArea(areas: Rect[]): void {
    const painting = this.d.s.painting;
    if (painting.shadingStyle === "GRAY_SHADING") {
      const d = new PathBuilder();
      for (const r of areas) d.rect(r.x, r.y, r.width, r.height);
      this.out += filledPath(d, painting.grayColor);
    } else {
      // Lines in page coordinates, so that they join from one sign to the next.
      const p = this.page;
      for (const r of areas) this.d.hatch(new Rect(p.tx + r.x * p.sx, p.ty + r.y * p.sy, r.width * p.sx, r.height * p.sy));
    }
  }
}

/// `LexicalSymbolsUtils.getStringForPhilology`.
const BRACKETS: Record<number, string> = {
  [SymbolCodes.BEGINERASE]: "[",
  [SymbolCodes.ENDERASE]: "]",
  [SymbolCodes.BEGINEDITORADDITION]: "<",
  [SymbolCodes.ENDEDITORADDITION]: ">",
  [SymbolCodes.BEGINEDITORSUPERFLUOUS]: "{",
  [SymbolCodes.ENDEDITORSUPERFLUOUS]: "}",
  [SymbolCodes.BEGINPREVIOUSLYREADABLE]: "[|",
  [SymbolCodes.ENDPREVIOUSLYREADABLE]: "|]",
  [SymbolCodes.BEGINSCRIBEADDITION]: "'",
  [SymbolCodes.ENDSCRIBEADDITION]: "'",
};

/// `ShadingCode.parse`: the quarters named by the digits 1 to 4.
function parseShading(s: string): number {
  let result = 0;
  for (const c of s) {
    if (c === "1") result |= ShadingCode.TOP_START;
    else if (c === "2") result |= ShadingCode.TOP_END;
    else if (c === "3") result |= ShadingCode.BOTTOM_START;
    else if (c === "4") result |= ShadingCode.BOTTOM_END;
  }
  return result;
}
