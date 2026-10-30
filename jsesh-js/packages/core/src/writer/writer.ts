/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Writes the document model as Manuel de Codage: port of
/// `jsesh.io.mdc.MdCModelWriter` (JSESH1 dialect).

import {
  cartoucheTypeCode,
  closeCodeForPhilology,
  INSERTION_DIRECTION_CODES,
  KERNING_AXIS_CODES,
  openCodeForPhilology,
  shadingToString,
} from "../model/constants.js";
import {
  AlphabeticCharacter,
  LineBreak,
  PageBreak,
  TopItemState,
  Zone,
  ZONE_UNSET,
  type AbsoluteGroup,
  type BasicItemList,
  type Cadrat,
  type Cartouche,
  type ComplexLigature,
  type HBox,
  type HieroglyphicText,
  type Hieroglyph,
  type HRule,
  type Insert,
  type Kerning,
  type Ligature,
  type MdcComment,
  type ModelElement,
  type ModelElementVisitor,
  type Modifier,
  type ModifiersList,
  type Overwrite,
  type Philology,
  type SubCadrat,
  type Superscript,
  type TabStop,
  type Tabbing,
  type ZoneContent,
} from "../model/model.js";

export interface MdcWriterOptions {
  /// First top item to write (default 0).
  start?: number;
  /// End of the range to write, exclusive (default: the whole text).
  end?: number;
  /// Replace phonetic codes by their Gardiner counterpart.
  normalize?: (code: string) => string;
}

/// Returns the MdC representation of `text`.
export function toMdc(text: HieroglyphicText, options: MdcWriterOptions = {}): string {
  const writer = new ModelWriter(options.start ?? 0, options.end ?? text.numberOfChildren, options.normalize);
  text.accept(writer);
  return writer.out;
}

/// End of the run of characters of the same script (and, if `splitOnState`,
/// the same state) starting at `start` (`AlphabeticRuns.runEnd`).
export function alphabeticRunEnd(container: ModelElement, start: number, splitOnState: boolean): number {
  const first = container.children[start];
  if (!(first instanceof AlphabeticCharacter)) return start;
  let end = start + 1;
  for (; end < container.numberOfChildren; end++) {
    const c = container.children[end];
    if (!(c instanceof AlphabeticCharacter) || !c.isSameScriptAs(first)) break;
    if (splitOnState && !c.state.equals(first.state)) break;
  }
  return end;
}

class ModelWriter implements ModelElementVisitor<void> {
  out = "";
  /// Avoid writing two minus signs after `|....-`.
  private wantsMinus = true;

  constructor(
    private readonly startIndex: number,
    private readonly endIndex: number,
    private readonly normalize?: (code: string) => string,
  ) {}

  private write(s: string): void {
    this.out += s;
  }

  private visitElementList(e: ModelElement, separator: string): void {
    let i = 0;
    while (i < e.numberOfChildren) {
      if (i > 0) this.write(separator);
      i = this.writeElementOrRun(e, i, e.numberOfChildren);
    }
  }

  /// Writes the child at `index`, or, if it's an alphabetic character, the
  /// whole run of text it starts (cut at `end`). Returns the next index.
  private writeElementOrRun(container: ModelElement, index: number, end: number): number {
    const child = container.getChildAt(index);
    if (child instanceof AlphabeticCharacter) {
      const runEnd = Math.min(alphabeticRunEnd(container, index, true), end);
      let text = "";
      for (let i = index; i < runEnd; i++) text += (container.getChildAt(i) as AlphabeticCharacter).mdcText;
      this.writeTextBlock(child.mdcScriptCode, text);
      return runEnd;
    }
    child.accept(this);
    return index + 1;
  }

  /// Writes `+` code text `+s`, escaping `\` and `+` in the text.
  private writeTextBlock(code: string, text: string): void {
    this.write("+" + code + text.replaceAll("\\", "\\\\").replaceAll("+", "\\+") + "+s");
  }

  visitAlphabeticCharacter(c: AlphabeticCharacter): void {
    this.writeTextBlock(c.mdcScriptCode, c.mdcText);
  }

  visitComment(c: MdcComment): void {
    this.writeTextBlock("+", c.text);
  }

  visitBasicItemList(l: BasicItemList): void {
    this.visitElementList(l, "-");
  }

  visitCadrat(c: Cadrat): void {
    this.visitElementList(c, ":");
    this.write(shadingToString("#", c.shading));
  }

  visitCartouche(c: Cartouche): void {
    const code = c.type === "CARTOUCHE" ? "" : cartoucheTypeCode(c.type);
    let start: string;
    let end: string;
    if (c.startPart === 1 && c.endPart === 2) {
      start = "<" + code.toUpperCase() + "-";
      end = "->";
    } else {
      start = "<" + code + c.startPart + "-";
      end = "-" + code + c.endPart + ">";
    }
    this.write(start);
    c.basicItemList.accept(this);
    this.write(end);
  }

  visitHBox(b: HBox): void {
    this.visitElementList(b, "*");
  }

  visitHieroglyph(h: Hieroglyph): void {
    if (h.grammar) this.write("=");
    this.write(this.normalize ? this.normalize(h.code) : h.code);
    const l = h.modifiers;
    if (l.reversed) this.write("\\");
    // The scale is written with the explicit position, if any.
    const explicitPosition = h.x !== 0 || h.y !== 0;
    if (!explicitPosition && l.scale !== 100) this.write("\\" + l.scale);
    if (l.angle !== 0) this.write("\\R" + l.angle);
    this.visitModifiersList(l);
    if (explicitPosition) this.write(`{{${h.x},${h.y},${l.scale}}}`);
    if (h.endingCode === "WORD_END") this.write("_");
    else if (h.endingCode === "SENTENCE_END") this.write("__");
  }

  visitHRule(h: HRule): void {
    this.write(`{${h.type}${h.startPos},${h.endPos}}`);
  }

  visitLigature(l: Ligature): void {
    this.visitElementList(l, "&");
  }

  visitModifier(m: Modifier): void {
    this.write("\\" + m.name + (m.value ?? ""));
  }

  visitModifiersList(l: ModifiersList): void {
    for (const m of l.modifiers) this.visitModifier(m);
  }

  visitInsert(insert: Insert): void {
    this.write("%insert[dir=" + INSERTION_DIRECTION_CODES[insert.direction]);
    if (insert.target !== 1) this.write(",target=" + insert.target);
    if (insert.dw !== 0) this.write(",dw=" + insert.dw);
    if (insert.dh !== 0) this.write(",dh=" + insert.dh);
    this.write("](");
    insert.first.basicItemList.accept(this);
    this.write(",");
    insert.second.basicItemList.accept(this);
    this.write(")");
  }

  visitKerning(kerning: Kerning): void {
    this.write("%" + KERNING_AXIS_CODES[kerning.axis]);
    if (kerning.options !== "") this.write("[" + kerning.options + "]");
  }

  visitOverwrite(o: Overwrite): void {
    o.first.accept(this);
    this.write("##");
    o.second.accept(this);
  }

  visitPhilology(p: Philology): void {
    // JSESH1: editorial marks as signs, so the group is protected by parenthesis.
    this.write("(" + (openCodeForPhilology(p.type) ?? "null") + "-");
    this.visitElementList(p, "-");
    this.write("-" + (closeCodeForPhilology(p.type) ?? "null") + ")");
  }

  visitSubCadrat(c: SubCadrat): void {
    this.write("(");
    this.visitElementList(c, "-");
    this.write(")");
  }

  visitSuperscript(s: Superscript): void {
    this.write("|" + s.text.replaceAll("\\", "\\\\").replaceAll("-", "\\-") + "-");
    this.wantsMinus = false;
  }

  visitTabStop(t: TabStop): void {
    this.write("?" + t.stopPos);
  }

  visitTabbing(t: Tabbing): void {
    this.write(
      `%[id=${t.id},orientation=${t.orientation.toLowerCase()},justification=${t.justification.toLowerCase()}]`,
    );
  }

  visitTabbingClear(): void {
    this.write("%clear");
  }

  visitHieroglyphicText(t: HieroglyphicText): void {
    this.writeSequence(t, this.startIndex, this.endIndex);
  }

  /// Writes the items of a text or of a zone between two gaps, with the
  /// toggles needed for their states. All toggles are closed at the end, so
  /// that a zone is self-contained.
  private writeSequence(t: HieroglyphicText | ZoneContent, start: number, end: number): void {
    let state = new TopItemState();
    // Was the last opening red toggle closer than the last opening gray toggle?
    let redCloser = false;
    let i = start;
    while (i < end) {
      const sub = t.getTopItemAt(i);
      let newState = sub.state;
      if (i > start && this.wantsMinus) this.write("-");
      this.wantsMinus = true;
      // For compatibility with HieroTeX, all toggles are closed at line or page breaks.
      // A zone has no state of its own (its content has), so toggles are closed before it too.
      if (sub instanceof LineBreak || sub instanceof PageBreak || sub instanceof Zone) newState = new TopItemState();
      redCloser = this.fixToggles(state, newState, redCloser);
      // A run of text shares one state, so the toggles above hold for all of it.
      i = this.writeElementOrRun(t, i, end);
      state = newState;
    }
    this.fixToggles(state, new TopItemState(), redCloser);
  }

  /// Writes `%zone[options](content)`, with only the options which are set.
  visitZone(zone: Zone): void {
    const p = zone.parameters;
    const options: string[] = [];
    if (p.orientation !== null) options.push("layout=" + (p.orientation === "HORIZONTAL" ? "lines" : "columns"));
    if (p.direction !== null) options.push("dir=" + (p.direction === "LEFT_TO_RIGHT" ? "lr" : "rl"));
    const lengths: [string, number][] = [
      ["width", p.width],
      ["height", p.height],
      ["lineskip", p.lineSkip],
      ["colskip", p.columnSkip],
      ["separator", p.separator],
      ["quadratwidth", p.quadratWidth],
      ["quadratheight", p.quadratHeight],
    ];
    for (const [name, value] of lengths) if (value !== ZONE_UNSET) options.push(name + "=" + value);
    if (p.frame) options.push("frame");
    this.write("%zone");
    if (options.length > 0) this.write("[" + options.join(",") + "]");
    this.write("(");
    zone.content.accept(this);
    this.write(")");
    this.wantsMinus = true;
  }

  visitZoneContent(content: ZoneContent): void {
    this.writeSequence(content, 0, content.numberOfChildren);
  }

  /// Writes the toggles needed to go from `state` to `newState`, avoiding
  /// crossing toggles. Returns true if a red toggle was closer than a gray one.
  private fixToggles(state: TopItemState, newState: TopItemState, redCloser: boolean): boolean {
    if (newState.isRed === state.isRed && newState.isShaded === state.isShaded) return redCloser;
    const closeGray = !newState.isShaded && state.isShaded;
    const openGray = newState.isShaded && !state.isShaded;
    const closeRed = !newState.isRed && state.isRed;
    const openRed = newState.isRed && !state.isRed;
    // Close before opening.
    if (closeGray && closeRed) {
      this.write(redCloser ? "$b-#e-" : "#e-$b-");
    } else if (closeGray) {
      if (state.isRed && redCloser) {
        this.write("$b-#e-$r-");
        redCloser = true;
      } else {
        this.write("#e-");
      }
    } else if (closeRed) {
      if (state.isShaded && !redCloser) {
        this.write("#e-$b-#b-");
        redCloser = false;
      } else {
        this.write("$b-");
      }
    }
    if (openGray) {
      this.write("#b-");
      redCloser = false;
    }
    if (openRed) {
      this.write("$r-");
      redCloser = true;
    }
    return redCloser;
  }

  visitLineBreak(b: LineBreak): void {
    this.write("!");
    if (b.spacing !== 100) this.write("=" + b.spacing + "%");
    this.write("\n");
    this.wantsMinus = false;
  }

  visitPageBreak(): void {
    this.write("!!\n");
    this.wantsMinus = false;
  }

  visitAbsoluteGroup(g: AbsoluteGroup): void {
    this.visitElementList(g, "**");
  }

  visitComplexLigature(l: ComplexLigature): void {
    if (l.beforeGroup !== null) {
      l.beforeGroup.accept(this);
      this.write("^^^");
    }
    l.hieroglyph.accept(this);
    if (l.afterGroup !== null) {
      this.write("&&&");
      l.afterGroup.accept(this);
    }
  }
}
