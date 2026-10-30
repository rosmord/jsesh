/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Literal AST of a Manuel de Codage text: port of `jsesh.parser.ast`.
///
/// Each Java record `AstXxx` becomes a plain object with `nodeType: "Xxx"` and
/// the same field names. Java enums become their constant names, `char`s
/// one-character strings. This shape is also the JSON written by the Java
/// fixture exporter (`jseshJsExport`), so parsed trees can be compared
/// directly with Java output.
///
/// The AST shares no types with the model (`../model`); `../mdcreader`
/// bridges the two, as in Java.

import type { PhilologyKind, SignSubType, ToggleType } from "./symbols.js";

export type CartoucheType = "CARTOUCHE" | "SEREKH" | "HWT" | "CASTLE" | "CIRCULAR_ENCLOSURE";
export type CartouchePart = "NONE" | "FIRST" | "SECOND" | "THIRD";
export type WordEnding = "NONE" | "WORD_END" | "SENTENCE_END";
export type InsertDirection =
  | "START"
  | "END"
  | "TOP"
  | "BOTTOM"
  | "TOP_START"
  | "BOTTOM_START"
  | "TOP_END"
  | "BOTTOM_END"
  | "CENTER";

export type AstOptionValue =
  | { nodeType: "Flag" }
  | { nodeType: "Text"; value: string }
  | { nodeType: "Int"; value: number };

export interface AstOption {
  nodeType: "Option";
  name: string;
  value: AstOptionValue;
}

export interface AstOptionList {
  nodeType: "OptionList";
  options: AstOption[];
}

export interface AstModifier {
  nodeType: "Modifier";
  name: string;
  value: number | null;
}

export interface AstModifierList {
  nodeType: "ModifierList";
  modifiers: AstModifier[];
}

export interface AstHieroglyph {
  nodeType: "Hieroglyph";
  isGrammar: boolean;
  type: SignSubType;
  code: string;
  modifiers: AstModifierList;
  endingCode: WordEnding;
  x: number;
  y: number;
  scale: number;
}

export interface AstLigature {
  nodeType: "Ligature";
  hieroglyphs: AstHieroglyph[];
}

export interface AstAbsoluteGroup {
  nodeType: "AbsoluteGroup";
  hieroglyphs: AstHieroglyph[];
}

export interface AstOverwrite {
  nodeType: "Overwrite";
  first: AstHieroglyph;
  second: AstHieroglyph;
}

export interface AstSubCadrat {
  nodeType: "SubCadrat";
  content: AstBasicItemList;
}

export interface AstPhilology {
  nodeType: "Philology";
  openingKind: PhilologyKind;
  closingKind: PhilologyKind;
  content: AstBasicItemList;
}

export interface AstCartouche {
  nodeType: "Cartouche";
  type: CartoucheType;
  startPart: CartouchePart;
  endPart: CartouchePart;
  content: AstBasicItemList;
}

export interface AstInsert {
  nodeType: "Insert";
  direction: InsertDirection;
  /** Which argument is the target: 1 (the default) or 2. */
  target: 1 | 2;
  /** The other options; `dir` and `target` are not among them. */
  options: AstOptionList;
  /** The first argument: any group (including another insertion). */
  firstItems: AstBasicItemList;
  /** The second argument, in text order. */
  secondItems: AstBasicItemList;
}

export type KerningAxis = "HORIZONTAL" | "VERTICAL";

/// A kerning operator, `%hkern` or `%vkern`, written where a group could be:
/// `p*%hkern*q`, `p:%vkern:q`, `p-%hkern-q`.
export interface AstKerning {
  nodeType: "Kerning";
  axis: KerningAxis;
  /** Not used yet; empty if none. */
  options: AstOptionList;
}

export type AstInnerGroup =
  | AstHieroglyph
  | AstCartouche
  | AstLigature
  | AstSubCadrat
  | AstOverwrite
  | AstPhilology
  | AstAbsoluteGroup
  | AstInsert
  | AstKerning;

export interface AstComplexLigature {
  nodeType: "ComplexLigature";
  beforeGroup: AstInnerGroup | null;
  hieroglyph: AstHieroglyph;
  afterGroup: AstInnerGroup | null;
}

export type AstHorizontalListElement = AstInnerGroup | AstComplexLigature;

export interface AstHBox {
  nodeType: "HBox";
  elements: AstHorizontalListElement[];
}

export interface AstCadrat {
  nodeType: "Cadrat";
  hBoxes: AstHBox[];
  shading: number;
  options: AstOptionList | null;
}

export interface AstAlphabeticText {
  nodeType: "AlphabeticText";
  scriptCode: string;
  text: string;
}

export interface AstStartHieroglyphicText {
  nodeType: "StartHieroglyphicText";
}

export interface AstToggle {
  nodeType: "Toggle";
  toggleType: ToggleType;
}

export type AstBasicItem = AstCadrat | AstAlphabeticText | AstStartHieroglyphicText | AstToggle;

export interface AstBasicItemList {
  nodeType: "BasicItemList";
  items: AstBasicItem[];
}

export interface AstSuperscript {
  nodeType: "Superscript";
  text: string;
}

export interface AstLineBreak {
  nodeType: "LineBreak";
  skip: number;
}

export interface AstPageBreak {
  nodeType: "PageBreak";
}

export interface AstTabStop {
  nodeType: "TabStop";
  stopWidth: number;
}

export interface AstHRule {
  nodeType: "HRule";
  lineType: string;
  startPos: number;
  endPos: number;
}

export interface AstZone {
  nodeType: "Zone";
  options: AstOptionList;
  list: AstTopItemList;
}

export type AstTopItem =
  | AstBasicItem
  | AstSuperscript
  | AstLineBreak
  | AstPageBreak
  | AstTabStop
  | AstHRule
  | AstZone;

export interface AstTopItemList {
  nodeType: "TopItemList";
  items: AstTopItem[];
}

export interface AstDocument {
  nodeType: "Document";
  topItems: AstTopItemList;
}

// ---- Helpers on option lists (methods of the Java record)

export function optionString(list: AstOptionList, name: string, defaultValue: string): string;
export function optionString(list: AstOptionList, name: string, defaultValue: null): string | null;
export function optionString(list: AstOptionList, name: string, defaultValue: string | null): string | null {
  let result = defaultValue;
  for (const o of list.options) {
    if (o.name === name && o.value.nodeType === "Text") result = o.value.value;
  }
  return result;
}

export function optionInt(list: AstOptionList, name: string, defaultValue: number): number {
  let result = defaultValue;
  for (const o of list.options) {
    if (o.name === name && o.value.nodeType === "Int") result = o.value.value;
  }
  return result;
}

export function hasOption(list: AstOptionList, name: string): boolean {
  return list.options.some((o) => o.name === name);
}

export function optionsWithout(list: AstOptionList, name: string): AstOptionList {
  const options = list.options.filter((o) => o.name !== name);
  return options.length === list.options.length ? list : { nodeType: "OptionList", options };
}
