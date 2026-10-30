/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Recursive descent parser for Manuel de Codage texts: port of
/// `jsesh.parser.MDCParser`.
///
/// It builds an [AstDocument], a literal AST of the code. Use
/// `parseMdc` (in `../mdcreader`) to get the document model instead.
///
/// Any syntax error aborts the parse with an [MdcSyntaxError]: there is no
/// error recovery.

import type {
  AstBasicItem,
  AstBasicItemList,
  AstCadrat,
  AstCartouche,
  AstDocument,
  AstHBox,
  AstHieroglyph,
  AstHorizontalListElement,
  AstInnerGroup,
  AstInsert,
  AstKerning,
  AstModifier,
  AstModifierList,
  AstOption,
  AstOptionList,
  AstPhilology,
  AstSubCadrat,
  AstTopItem,
  AstZone,
  CartouchePart,
  CartoucheType,
  InsertDirection,
  WordEnding,
} from "./ast.js";
import { optionInt, optionString, optionsWithout } from "./ast.js";
import { MdcLexer } from "./lexer.js";
import {
  MdcSymbolCode as S,
  type AlphabeticTextValue,
  type CartoucheValue,
  type HRuleValue,
  type MdcSignValue,
  type MdcSymbol,
  type ModifierValue,
  type OldCartoucheStartValue,
  type PhilologyKind,
  type ToggleType,
} from "./symbols.js";

/// A syntax error, with its position in the source.
export class MdcSyntaxError extends Error {
  constructor(
    message: string,
    /// 0-based line number.
    readonly line: number,
    /// Offset of the offending token in the source.
    readonly charPos: number,
    /// Text of the offending token.
    readonly token: string,
  ) {
    super(message);
    this.name = "MdcSyntaxError";
  }
}

export interface MdcParserOptions {
  /// Are philological markers, such as `[[` and `]]`, considered as simple
  /// signs? If false, they work as parenthesis (tksesh only). Defaults to true.
  philologyAsSigns?: boolean;
}

const CARTOUCHE_TYPES: Record<string, CartoucheType> = {
  c: "CARTOUCHE",
  s: "SEREKH",
  h: "HWT",
  f: "CASTLE",
  g: "CIRCULAR_ENCLOSURE",
};

const CARTOUCHE_PARTS: CartouchePart[] = ["NONE", "FIRST", "SECOND", "THIRD"];

const INSERT_DIRECTIONS: Record<string, InsertDirection> = {
  s: "START",
  e: "END",
  t: "TOP",
  b: "BOTTOM",
  ts: "TOP_START",
  bs: "BOTTOM_START",
  te: "TOP_END",
  be: "BOTTOM_END",
  c: "CENTER",
};

function cartoucheType(code: string): CartoucheType {
  const type = CARTOUCHE_TYPES[code.toLowerCase()];
  if (type === undefined) throw new Error("Not a valid cartouche type code: " + code);
  return type;
}

function cartouchePart(digit: number): CartouchePart {
  const part = CARTOUCHE_PARTS[digit];
  if (part === undefined) throw new Error("Not a valid cartouche part digit: " + digit);
  return part;
}

/// Parses `mdc` into a literal AST.
export function parseMdcAst(mdc: string, options: MdcParserOptions = {}): AstDocument {
  return new MdcParser(options).parse(mdc);
}

export class MdcParser {
  private readonly philologyAsSigns: boolean;
  private lexer!: MdcLexer;
  private source = "";
  /// The current lookahead symbol.
  private token!: MdcSymbol;

  constructor(options: MdcParserOptions = {}) {
    this.philologyAsSigns = options.philologyAsSigns ?? true;
  }

  parse(text: string): AstDocument {
    this.source = text;
    this.lexer = new MdcLexer(text);
    this.lexer.philologyAsSigns = this.philologyAsSigns;
    try {
      this.advance();
      return this.parseMdcFile();
    } catch (e) {
      if (e instanceof MdcSyntaxError) throw e;
      throw new MdcSyntaxError("Generic Error: " + (e as Error).message, 0, 0, "");
    }
  }

  // ---- Error reporting

  private lineOf(pos: number): number {
    let line = 0;
    const limit = Math.min(pos, this.source.length);
    for (let i = 0; i < limit; i++) {
      if (this.source[i] === "\n") line++;
    }
    return line;
  }

  private error(message: string): MdcSyntaxError {
    const line = this.lineOf(this.token.position);
    const charPos = this.token.position;
    const text = this.token.text;
    return new MdcSyntaxError(
      `${message} line ${line} char ${charPos} at token '${text}'`,
      line,
      charPos,
      text,
    );
  }

  // ---- Token handling

  private advance(): void {
    this.token = this.lexer.nextSymbol() ?? {
      code: S.EOF,
      value: null,
      text: "",
      position: this.lexer.position(),
    };
  }

  private at(sym: S): boolean {
    return this.token.code === sym;
  }

  /// Checks that the current token is of the given type, consumes it, and
  /// returns its value.
  private expect<T = unknown>(sym: S, what: string): T {
    if (!this.at(sym)) throw this.error("expected " + what);
    const value = this.token.value as T;
    this.advance();
    return value;
  }

  private atHieroglyphStart(): boolean {
    return this.at(S.GRAMMAR) || this.at(S.HIEROGLYPH);
  }

  /// Everything that can be found at the start of a cadrat.
  private atCadratStart(): boolean {
    return (
      this.atHieroglyphStart() ||
      this.at(S.BEGIN_PHIL) ||
      this.at(S.LPAREN) ||
      this.at(S.BEGIN_CARTOUCHE) ||
      this.at(S.BEGIN_OLD_CARTOUCHE) ||
      this.at(S.QUADRAT) ||
      this.at(S.INSERT) ||
      this.at(S.HKERN) ||
      this.at(S.VKERN)
    );
  }

  // ---- Top level

  /// ```
  /// mdcfile ::= topitems optSeparator EOF
  /// ```
  private parseMdcFile(): AstDocument {
    const items = this.parseTopItems();
    if (!this.at(S.EOF)) throw this.error("unexpected or unknown item.");
    return { nodeType: "Document", topItems: { nodeType: "TopItemList", items } };
  }

  /// ```
  /// topitems ::= ε | topitems optSeparator topitem
  /// topitem ::= cadrat optShading | TOGGLE | STARTHIEROGLYPHS | TEXT | TEXTSUPER
  ///           | LINEEND | PAGEEND | TABSTOP | TABBING optionList | TABBINGCLEAR
  ///           | HRULE | zone
  /// ```
  private parseTopItems(): AstTopItem[] {
    const items: AstTopItem[] = [];
    for (;;) {
      this.skipOptSeparator();
      let item: AstTopItem | null;
      const value = this.token.value;
      switch (this.token.code) {
        case S.TOGGLE:
          item = { nodeType: "Toggle", toggleType: value as ToggleType };
          this.advance();
          break;
        case S.START_HIEROGLYPHS:
          item = { nodeType: "StartHieroglyphicText" };
          this.advance();
          break;
        case S.TEXT: {
          const t = value as AlphabeticTextValue;
          item = { nodeType: "AlphabeticText", scriptCode: t.code, text: t.text };
          this.advance();
          break;
        }
        case S.TEXT_SUPER:
          item = { nodeType: "Superscript", text: value as string };
          this.advance();
          break;
        case S.LINE_END:
          item = { nodeType: "LineBreak", skip: value as number };
          this.advance();
          break;
        case S.PAGE_END:
          item = { nodeType: "PageBreak" };
          this.advance();
          break;
        case S.TAB_STOP:
          item = { nodeType: "TabStop", stopWidth: value as number };
          this.advance();
          break;
        case S.TABBING:
          this.advance();
          item = { nodeType: "Tabbing", options: this.parseOptionList() };
          break;
        case S.TABBING_CLEAR:
          item = { nodeType: "TabbingClear" };
          this.advance();
          break;
        case S.HRULE: {
          const rule = value as HRuleValue;
          item = { nodeType: "HRule", lineType: rule.type, startPos: rule.start, endPos: rule.end };
          this.advance();
          break;
        }
        case S.ZONE:
          item = this.parseZone();
          break;
        default:
          item = this.atCadratStart() ? this.parseCadratWithShading() : null;
      }
      if (item === null) break;
      items.push(item);
    }
    return items;
  }

  /// `optSeparator ::= ε | SEPARATOR`
  private skipOptSeparator(): void {
    if (this.at(S.SEPARATOR)) this.advance();
  }

  /// `zoneStart ::= ZONE optionList? '(' topitems ')'`
  private parseZone(): AstZone {
    this.expect(S.ZONE, "zone");
    let options: AstOptionList = { nodeType: "OptionList", options: [] };
    if (this.at(S.OPEN_BRACE)) options = this.parseOptionList();
    this.expect(S.LPAREN, "'('");
    const items = this.parseTopItems();
    this.expect(S.RPAREN, "')'");
    return { nodeType: "Zone", options, list: { nodeType: "TopItemList", items } };
  }

  /// ```
  /// optionList ::= OPENBRACE options CLOSEBRACE
  /// options ::= option | options COMMA option
  /// option ::= IDENTIFIER | IDENTIFIER EQUAL IDENTIFIER | IDENTIFIER EQUAL INTEGER
  /// ```
  private parseOptionList(): AstOptionList {
    this.expect(S.OPEN_BRACE, "'['");
    const options: AstOption[] = [];
    for (;;) {
      const name = this.expect<string>(S.IDENTIFIER, "option name");
      if (this.at(S.EQUAL)) {
        this.advance();
        if (this.at(S.IDENTIFIER)) {
          options.push({ nodeType: "Option", name, value: { nodeType: "Text", value: this.token.value as string } });
        } else if (this.at(S.INTEGER)) {
          options.push({ nodeType: "Option", name, value: { nodeType: "Int", value: this.token.value as number } });
        } else {
          throw this.error("expected option value");
        }
        this.advance();
      } else {
        options.push({ nodeType: "Option", name, value: { nodeType: "Flag" } });
      }
      if (!this.at(S.COMMA)) break;
      this.advance();
    }
    this.expect(S.CLOSE_BRACE, "']'");
    return { nodeType: "OptionList", options };
  }

  // ---- Cadrats

  /// `cadrat optShading`, with `optShading ::= ε | SHADING`
  private parseCadratWithShading(): AstCadrat {
    const cadrat: AstCadrat = { nodeType: "Cadrat", hBoxes: [], shading: 0, options: null };
    this.parseCadrat(cadrat);
    if (this.at(S.SHADING)) {
      cadrat.shading = parseShadingDigits(this.token.value as string);
      this.advance();
    }
    return cadrat;
  }

  /// ```
  /// cadrat ::= verticalStack
  ///          | CADRAT BPAR verticalStack EPAR
  ///          | CADRAT optionList BPAR verticalStack EPAR
  /// ```
  private parseCadrat(cadrat: AstCadrat): void {
    if (this.at(S.QUADRAT)) {
      this.advance();
      if (this.at(S.OPEN_BRACE)) cadrat.options = this.parseOptionList();
      this.expect(S.LPAREN, "'('");
      this.parseVerticalStack(cadrat);
      this.expect(S.RPAREN, "')'");
    } else {
      this.parseVerticalStack(cadrat);
    }
  }

  /// `verticalStack ::= horizontalList | verticalStack COLON horizontalList`
  private parseVerticalStack(cadrat: AstCadrat): void {
    cadrat.hBoxes.push(this.parseHorizontalList());
    while (this.at(S.COLON)) {
      this.advance();
      cadrat.hBoxes.push(this.parseHorizontalList());
    }
  }

  /// `horizontalList ::= horizontalListElement | horizontalList STAR horizontalListElement`
  private parseHorizontalList(): AstHBox {
    const elements: AstHorizontalListElement[] = [this.parseHorizontalListElement()];
    while (this.at(S.STAR)) {
      this.advance();
      elements.push(this.parseHorizontalListElement());
    }
    return { nodeType: "HBox", elements };
  }

  /// ```
  /// horizontalListElement ::= complexLigature | innerGroup | cartouche
  /// complexLigature ::= innerGroup LIGBEFORE hieroglyph LIGAFTER innerGroup
  ///                   | innerGroup LIGBEFORE hieroglyph
  ///                   | hieroglyph LIGAFTER innerGroup
  /// ```
  ///
  /// A leading hieroglyph is parsed first; the next token then tells which
  /// construct it starts.
  private parseHorizontalListElement(): AstHorizontalListElement {
    if (this.at(S.BEGIN_CARTOUCHE) || this.at(S.BEGIN_OLD_CARTOUCHE)) {
      return this.parseCartouche();
    }
    let first: AstInnerGroup;
    if (this.atHieroglyphStart()) {
      const h = this.parseHieroglyph();
      if (this.at(S.LIG_AFTER)) {
        this.advance();
        return { nodeType: "ComplexLigature", beforeGroup: null, hieroglyph: h, afterGroup: this.parseInnerGroup() };
      }
      first = this.continueInnerGroup(h);
    } else {
      first = this.parseInnerGroup();
    }
    if (this.at(S.LIG_BEFORE)) {
      this.advance();
      const middle = this.parseHieroglyph();
      let after: AstInnerGroup | null = null;
      if (this.at(S.LIG_AFTER)) {
        this.advance();
        after = this.parseInnerGroup();
      }
      return { nodeType: "ComplexLigature", beforeGroup: first, hieroglyph: middle, afterGroup: after };
    }
    return first;
  }

  /// ```
  /// innerGroup ::= ligature | hieroglyph | overwrite | philology
  ///              | subgroup | absoluteGroup | insert | kerning
  /// ```
  private parseInnerGroup(): AstInnerGroup {
    if (this.atHieroglyphStart()) return this.continueInnerGroup(this.parseHieroglyph());
    if (this.at(S.BEGIN_PHIL)) return this.parsePhilology();
    if (this.at(S.LPAREN)) return this.parseSubgroup();
    if (this.at(S.INSERT)) return this.parseInsert();
    if (this.at(S.HKERN) || this.at(S.VKERN)) return this.parseKerning();
    throw this.error("unexpected or unknown item.");
  }

  /// `insert ::= INSERT optionList '(' basicitems ',' basicitems ')'`
  private parseInsert(): AstInsert {
    this.expect(S.INSERT, "insert");
    const options = this.parseOptionList();
    const dir = optionString(options, "dir", null);
    if (dir === null) throw this.error("insert: missing 'dir' option");
    const direction = INSERT_DIRECTIONS[dir];
    if (direction === undefined) throw this.error("insert: unknown 'dir' option value: " + dir);
    const target = optionInt(options, "target", 1);
    if (target !== 1 && target !== 2) throw this.error("insert: 'target' option should be 1 or 2, not " + target);
    this.expect(S.LPAREN, "'('");
    const firstItems = this.parseBasicItems();
    this.expect(S.COMMA, "','");
    const secondItems = this.parseBasicItems();
    this.expect(S.RPAREN, "')'");
    return {
      nodeType: "Insert",
      direction,
      target,
      options: optionsWithout(optionsWithout(options, "dir"), "target"),
      firstItems,
      secondItems,
    };
  }

  /// `kerning ::= (HKERN | VKERN) optionList?`
  ///
  /// It is parsed as a group, so it can be found in a horizontal list (`p*%hkern*q`),
  /// alone in a line of a cadrat (`p:%vkern:q`), or alone in a cadrat (`p-%hkern-q`).
  private parseKerning(): AstKerning {
    const axis = this.at(S.HKERN) ? "HORIZONTAL" : "VERTICAL";
    this.advance();
    const options: AstOptionList = this.at(S.OPEN_BRACE) ? this.parseOptionList() : { nodeType: "OptionList", options: [] };
    return { nodeType: "Kerning", axis, options };
  }

  /// The inner groups which start with a hieroglyph, once it has been read.
  ///
  /// ```
  /// ligature ::= hieroglyph (AMP hieroglyph)+
  /// absoluteGroup ::= hieroglyph (DOUBLEAMP hieroglyph)+
  /// overwrite ::= hieroglyph OVERWRITE hieroglyph
  /// ```
  private continueInnerGroup(first: AstHieroglyph): AstInnerGroup {
    switch (this.token.code) {
      case S.AMP:
      case S.DOUBLE_AMP: {
        const separator = this.token.code;
        const hieroglyphs = [first];
        while (this.at(separator)) {
          this.advance();
          hieroglyphs.push(this.parseHieroglyph());
        }
        return separator === S.AMP
          ? { nodeType: "Ligature", hieroglyphs }
          : { nodeType: "AbsoluteGroup", hieroglyphs };
      }
      case S.OVERWRITE:
        this.advance();
        return { nodeType: "Overwrite", first, second: this.parseHieroglyph() };
      default:
        return first;
    }
  }

  /// `philology ::= BEGINPHIL basicitems optSeparator ENDPHIL`
  private parsePhilology(): AstPhilology {
    const openingKind = this.expect<PhilologyKind>(S.BEGIN_PHIL, "philology start");
    const content = this.parseBasicItems();
    const closingKind = this.expect<PhilologyKind>(S.END_PHIL, "philology end");
    return { nodeType: "Philology", openingKind, closingKind, content };
  }

  /// `subgroup ::= BPAR basicitems optSeparator EPAR`
  private parseSubgroup(): AstSubCadrat {
    this.expect(S.LPAREN, "'('");
    const content = this.parseBasicItems();
    this.expect(S.RPAREN, "')'");
    return { nodeType: "SubCadrat", content };
  }

  /// ```
  /// cartouche ::= BEGINCARTOUCHE basicitems optSeparator ENDCARTOUCHE modifiers
  ///             | BEGINOLDCARTOUCHE basicitems optSeparator ENDCARTOUCHE
  /// ```
  ///
  /// The modifiers after a cartouche are parsed, but not kept.
  private parseCartouche(): AstCartouche {
    if (this.at(S.BEGIN_CARTOUCHE)) {
      const start = this.token.value as CartoucheValue;
      this.advance();
      const content = this.parseBasicItems();
      const end = this.expect<CartoucheValue>(S.END_CARTOUCHE, "end of cartouche");
      this.parseModifiers();
      return {
        nodeType: "Cartouche",
        type: cartoucheType(start.type),
        startPart: cartouchePart(start.part),
        endPart: cartouchePart(end.part),
        content,
      };
    }
    const start = this.expect<OldCartoucheStartValue>(S.BEGIN_OLD_CARTOUCHE, "cartouche");
    const content = this.parseBasicItems();
    this.expect(S.END_CARTOUCHE, "end of cartouche");
    let startPart: CartouchePart;
    let endPart: CartouchePart;
    switch (start.part.toLowerCase()) {
      case "b":
        startPart = "FIRST";
        endPart = "NONE";
        break;
      case "m":
        startPart = "NONE";
        endPart = "NONE";
        break;
      case "e":
        startPart = "NONE";
        endPart = "SECOND";
        break;
      default:
        startPart = "FIRST";
        endPart = "SECOND";
    }
    return { nodeType: "Cartouche", type: cartoucheType(start.code), startPart, endPart, content };
  }

  /// ```
  /// basicitems ::= ε | basicitems optSeparator basicitem
  /// basicitem ::= cadrat optShading | TEXT | STARTHIEROGLYPHS | TOGGLE
  /// ```
  ///
  /// Also consumes the optSeparator which follows basicitems in all rules
  /// using it.
  private parseBasicItems(): AstBasicItemList {
    const items: AstBasicItem[] = [];
    for (;;) {
      this.skipOptSeparator();
      if (this.at(S.TEXT)) {
        const t = this.token.value as AlphabeticTextValue;
        items.push({ nodeType: "AlphabeticText", scriptCode: t.code, text: t.text });
        this.advance();
      } else if (this.at(S.START_HIEROGLYPHS)) {
        items.push({ nodeType: "StartHieroglyphicText" });
        this.advance();
      } else if (this.at(S.TOGGLE)) {
        items.push({ nodeType: "Toggle", toggleType: this.token.value as ToggleType });
        this.advance();
      } else if (this.atCadratStart()) {
        items.push(this.parseCadratWithShading());
      } else {
        break;
      }
    }
    return { nodeType: "BasicItemList", items };
  }

  // ---- Signs

  /// ```
  /// hieroglyph ::= optGrammar HIEROGLYPH modifiers optPos optWordEnd
  /// optPos ::= ε | DOUBLELEFTCURLY INTEGER COMMA INTEGER COMMA INTEGER DOUBLERIGHTCURLY
  /// optWordEnd ::= ε | WORDEND | SENTENCEEND
  /// ```
  private parseHieroglyph(): AstHieroglyph {
    let isGrammar = false;
    if (this.at(S.GRAMMAR)) {
      isGrammar = true;
      this.advance();
    }
    const sign = this.expect<MdcSignValue>(S.HIEROGLYPH, "sign code");
    const modifiers = this.parseModifiers();
    let x = 0;
    let y = 0;
    let scale = 100;
    if (this.at(S.DOUBLE_LEFT_CURLY)) {
      this.advance();
      x = this.expect<number>(S.INTEGER, "integer");
      this.expect(S.COMMA, "','");
      y = this.expect<number>(S.INTEGER, "integer");
      this.expect(S.COMMA, "','");
      scale = this.expect<number>(S.INTEGER, "integer");
      this.expect(S.DOUBLE_RIGHT_CURLY, "'}}'");
    }
    let endingCode: WordEnding = "NONE";
    if (this.at(S.WORD_END)) {
      endingCode = "WORD_END";
      this.advance();
    } else if (this.at(S.SENTENCE_END)) {
      endingCode = "SENTENCE_END";
      this.advance();
    }
    return {
      nodeType: "Hieroglyph",
      isGrammar,
      type: sign.subtype,
      code: sign.text,
      modifiers,
      endingCode,
      x,
      y,
      scale,
    };
  }

  /// `modifiers ::= ε | modifiers MODIFIER`
  private parseModifiers(): AstModifierList {
    const modifiers: AstModifier[] = [];
    while (this.at(S.MODIFIER)) {
      const m = this.token.value as ModifierValue;
      modifiers.push({ nodeType: "Modifier", name: m.name, value: m.value });
      this.advance();
    }
    return { nodeType: "ModifierList", modifiers };
  }
}

/// Decodes a shading marker's digits (e.g. `"1234"` from `"#1234"`) into an
/// or-combination of the shading bits (TOP_START=1, TOP_END=2,
/// BOTTOM_START=4, BOTTOM_END=8).
function parseShadingDigits(digits: string): number {
  let sh = 0;
  for (const c of digits) {
    switch (c) {
      case "1":
        sh |= 1;
        break;
      case "2":
        sh |= 2;
        break;
      case "3":
        sh |= 4;
        break;
      case "4":
        sh |= 8;
        break;
    }
  }
  return sh;
}
