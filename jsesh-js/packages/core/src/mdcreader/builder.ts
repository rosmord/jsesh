/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Builds the document model from the parser's AST: port of
/// `jsesh.mdcreader.AstModelBuilder` and `MDCParserModelGenerator`.

import type {
  AstBasicItem,
  AstBasicItemList,
  AstHieroglyph,
  AstHorizontalListElement,
  AstInnerGroup,
  AstModifierList,
  AstOptionList,
  AstTopItem,
  AstTopItemList,
  CartouchePart as AstCartouchePart,
  CartoucheType as AstCartoucheType,
} from "../parser/ast.js";
import { hasOption, optionInt, optionString } from "../parser/ast.js";
import { parseMdcAst, type MdcParserOptions } from "../parser/parser.js";
import type { PhilologyKind, SignSubType } from "../parser/symbols.js";
import { SymbolCodes, type CartouchePart, type CartoucheType } from "../model/constants.js";
import {
  AbsoluteGroup,
  AlphabeticCharacter,
  BasicItemList,
  Cadrat,
  Cartouche,
  ComplexLigature,
  HBox,
  HieroglyphicText,
  Hieroglyph,
  HorizontalListElement,
  HRule,
  InnerGroup,
  Insert,
  Kerning,
  Ligature,
  LineBreak,
  MdcComment,
  Modifier,
  ModifiersList,
  Overwrite,
  PageBreak,
  Philology,
  SubCadrat,
  Superscript,
  TabStop,
  TopItem,
  TopItemState,
  Zone,
  ZoneContent,
  ZONE_UNSET,
  zoneParameters,
  type BasicItem,
  type ZoneParameters,
} from "../model/model.js";

/// Parses MdC text into a [HieroglyphicText].
///
/// Throws an `MdcSyntaxError` if the text is not correct.
export function parseMdc(mdc: string, options: MdcParserOptions = {}): HieroglyphicText {
  return new AstModelBuilder().buildText(parseMdcAst(mdc, options).topItems);
}

const CARTOUCHE_TYPES: Record<AstCartoucheType, CartoucheType> = {
  CARTOUCHE: "CARTOUCHE",
  SEREKH: "SEREKH",
  HWT: "HWT",
  CASTLE: "CASTLE",
  CIRCULAR_ENCLOSURE: "CIRCULAR_ENCLOSURE",
};

const CARTOUCHE_PARTS: Record<AstCartouchePart, CartouchePart> = { NONE: 0, FIRST: 1, SECOND: 2, THIRD: 3 };

const PHILOLOGY_CODES: Record<PhilologyKind, number> = {
  ERASED_SIGNS: SymbolCodes.ERASEDSIGNS,
  EDITOR_ADDITION: SymbolCodes.EDITORADDITION,
  EDITOR_SUPERFLUOUS: SymbolCodes.EDITORSUPERFLUOUS,
  PREVIOUSLY_READABLE: SymbolCodes.PREVIOUSLYREADABLE,
  SCRIBE_ADDITION: SymbolCodes.SCRIBEADDITION,
  MINOR_ADDITION: SymbolCodes.MINORADDITION,
  DUBIOUS: SymbolCodes.DUBIOUS,
};

const PLAIN_SIGN_TYPES: Record<string, number> = {
  MDC_CODE: SymbolCodes.MDCCODE,
  RED_POINT: SymbolCodes.REDPOINT,
  BLACK_POINT: SymbolCodes.BLACKPOINT,
  SMALL_TEXT: SymbolCodes.SMALLTEXT,
  HALF_SPACE: SymbolCodes.HALFSPACE,
  FULL_SPACE: SymbolCodes.FULLSPACE,
  FULL_SHADE: SymbolCodes.FULLSHADE,
  VERTICAL_SHADE: SymbolCodes.VERTICALSHADE,
  QUARTER_SHADE: SymbolCodes.QUATERSHADE,
  HORIZONTAL_SHADE: SymbolCodes.HORIZONTALSHADE,
};

/// The sign type for a hieroglyph's subtype. A philological bracket read as a
/// sign gets `kind * 2` (begin) or `kind * 2 + 1` (end).
function signTypeCode(subtype: SignSubType): number {
  if (typeof subtype === "string") return PLAIN_SIGN_TYPES[subtype]!;
  const base = PHILOLOGY_CODES[subtype.kind];
  return subtype.begin ? base * 2 : base * 2 + 1;
}

/// Port of `AstModelBuilder` (JSesh dialect: the MacScribe `r` modifier
/// compatibility is not supported).
class AstModelBuilder {
  private readonly currentState = new TopItemState();

  buildText(list: AstTopItemList): HieroglyphicText {
    return new HieroglyphicText(this.buildTopItems(list));
  }

  /// Builds the items of a text or of a zone, giving each one the current
  /// red/shaded state.
  ///
  /// Toggles are read in sequence, across zone limits: `$r-A1-%zone(A2)`
  /// makes A2 red.
  private buildTopItems(list: AstTopItemList): TopItem[] {
    const result: TopItem[] = [];
    for (const item of list.items) {
      for (const topItem of this.buildTopItem(item)) {
        // A zone has no state of its own: its content has.
        if (item.nodeType !== "Zone") {
          topItem.state = this.currentState.duplicate();
        }
        result.push(topItem);
      }
    }
    return result;
  }

  /// Toggles and `+s` build nothing; alphabetic text builds one item per
  /// character.
  private buildTopItem(item: AstTopItem): TopItem[] {
    switch (item.nodeType) {
      case "Superscript":
        // Remove protection characters (only in front of "\" and "-").
        return [new Superscript(item.text.replace(/\\(\\|-)/g, "$1"))];
      case "LineBreak":
        return [new LineBreak(item.skip)];
      case "PageBreak":
        return [new PageBreak()];
      case "TabStop":
        return [new TabStop(item.stopWidth)];
      case "HRule":
        return [new HRule(item.lineType, item.startPos, item.endPos)];
      case "Zone":
        return [new Zone(zoneParametersOf(item.options), new ZoneContent(this.buildTopItems(item.list)))];
      default:
        return this.buildBasicItem(item);
    }
  }

  private buildBasicItem(item: AstBasicItem): BasicItem[] {
    switch (item.nodeType) {
      case "Cadrat": {
        const cadrat = new Cadrat(item.hBoxes.map((b) => new HBox(b.elements.map((e) => this.buildElement(e)))));
        cadrat.shading = item.shading;
        // Cadrat options (%quadrat[...]) are ignored, as in Java.
        return [cadrat];
      }
      case "AlphabeticText":
        return item.scriptCode === "+"
          ? [new MdcComment(item.text)]
          : AlphabeticCharacter.fromMdcText(item.scriptCode, item.text);
      case "Toggle":
        this.applyToggle(item.toggleType);
        return [];
      case "StartHieroglyphicText":
        return [];
    }
  }

  private applyToggle(toggle: string): void {
    const s = this.currentState;
    switch (toggle) {
      case "BLACK":
        s.isRed = false;
        break;
      case "BLACK_RED":
        s.isRed = !s.isRed;
        break;
      case "RED":
        s.isRed = true;
        break;
      case "SHADING_OFF":
        s.isShaded = false;
        break;
      case "SHADING_ON":
        s.isShaded = true;
        break;
      case "SHADING_TOGGLE":
        s.isShaded = !s.isShaded;
        break;
      // LACUNA, LINE_LACUNA, OMIT: not implemented, as in Java.
    }
  }

  private buildBasicItemList(list: AstBasicItemList): BasicItemList {
    const result = new BasicItemList();
    for (const item of list.items) {
      for (const b of this.buildBasicItem(item)) result.addBasicItem(b);
    }
    return result;
  }

  private buildElement(e: AstHorizontalListElement): HorizontalListElement {
    if (e.nodeType === "ComplexLigature") {
      return new ComplexLigature(
        e.beforeGroup === null ? null : this.buildInnerGroup(e.beforeGroup),
        this.buildHieroglyph(e.hieroglyph),
        e.afterGroup === null ? null : this.buildInnerGroup(e.afterGroup),
      );
    }
    return this.buildInnerGroup(e);
  }

  private buildInnerGroup(g: AstInnerGroup): InnerGroup {
    switch (g.nodeType) {
      case "Hieroglyph":
        return this.buildHieroglyph(g);
      case "Cartouche":
        return new Cartouche(
          CARTOUCHE_TYPES[g.type],
          CARTOUCHE_PARTS[g.startPart],
          CARTOUCHE_PARTS[g.endPart],
          this.buildBasicItemList(g.content),
        );
      case "Ligature":
        return new Ligature(g.hieroglyphs.map((h) => this.buildHieroglyph(h)));
      case "AbsoluteGroup":
        return new AbsoluteGroup(g.hieroglyphs.map((h) => this.buildHieroglyph(h)));
      case "Overwrite":
        return new Overwrite(this.buildHieroglyph(g.first), this.buildHieroglyph(g.second));
      case "SubCadrat":
        return new SubCadrat(this.buildBasicItemList(g.content));
      case "Philology":
        // Only the opening kind is kept, as in Java.
        return new Philology(PHILOLOGY_CODES[g.openingKind], this.buildBasicItemList(g.content));
      case "Insert":
        // The AST's direction names are the model's.
        return new Insert(
          g.direction,
          new SubCadrat(this.buildBasicItemList(g.firstItems)),
          new SubCadrat(this.buildBasicItemList(g.secondItems)),
          g.target,
          optionInt(g.options, "dw", 0),
          optionInt(g.options, "dh", 0),
        );
      case "Kerning":
        // The AST's axis names are the model's.
        return new Kerning(g.axis, optionsText(g.options));
    }
  }

  private buildHieroglyph(node: AstHieroglyph): Hieroglyph {
    const h = new Hieroglyph(node.code, this.buildModifiers(node.modifiers));
    h.setGrammar(node.isGrammar);
    h.setType(signTypeCode(node.type));
    h.setEndingCode(node.endingCode);
    if (node.x !== 0 || node.y !== 0 || node.scale !== 100) {
      h.setExplicitPosition(node.x, node.y, node.scale);
    }
    return h;
  }

  private buildModifiers(list: AstModifierList): ModifiersList {
    const result = new ModifiersList();
    for (const m of list.modifiers) result.includeModifier(new Modifier(m.name, m.value));
    return result;
  }
}

/// Reads `layout=lines|columns`, `dir=lr|rl`, `width`, `height`, `lineskip`,
/// `colskip`, `separator`, `quadratwidth`, `quadratheight` and `frame`.
/// Unknown options and values are ignored.
function zoneParametersOf(options: AstOptionList): ZoneParameters {
  const layout = optionString(options, "layout", "");
  const dir = optionString(options, "dir", "");
  const length = (name: string) => optionInt(options, name, ZONE_UNSET);
  return zoneParameters({
    orientation: layout === "lines" ? "HORIZONTAL" : layout === "columns" ? "VERTICAL" : null,
    direction: dir === "lr" ? "LEFT_TO_RIGHT" : dir === "rl" ? "RIGHT_TO_LEFT" : null,
    width: length("width"),
    height: length("height"),
    lineSkip: length("lineskip"),
    columnSkip: length("colskip"),
    separator: length("separator"),
    quadratWidth: length("quadratwidth"),
    quadratHeight: length("quadratheight"),
    frame: hasOption(options, "frame"),
  });
}

/// The options, written back as they would appear between brackets.
function optionsText(options: AstOptionList): string {
  return options.options
    .map((o) => (o.value.nodeType === "Flag" ? o.name : o.name + "=" + o.value.value))
    .join(",");
}
