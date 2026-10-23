/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

import { describe, expect, it } from "vitest";
import { MdcLexer } from "../src/parser/lexer.js";
import { parseMdcAst } from "../src/parser/parser.js";
import { stripJSeshHeader } from "../src/document/header.js";
import {
  fixturesAvailable,
  readCorpusFile,
  readFixture,
  stripRecordTypes,
  type CorpusCase,
  type ParserCase,
} from "./fixtures.js";

const cases: ParserCase[] = fixturesAvailable ? readFixture("parser-cases.json") : [];
const corpus: string[] = fixturesAvailable ? readFixture("corpus/index.json") : [];

function lex(input: string) {
  const lexer = new MdcLexer(input);
  lexer.philologyAsSigns = true;
  const result = [];
  for (let s = lexer.nextSymbol(); s !== undefined; s = lexer.nextSymbol()) {
    result.push({ code: s.code, text: s.text, value: s.value });
  }
  return result;
}

function astOrError(input: string, philologyAsSigns: boolean): { ast?: unknown; error?: boolean } {
  try {
    return { ast: parseMdcAst(input, { philologyAsSigns }) };
  } catch {
    return { error: true };
  }
}

describe.skipIf(!fixturesAvailable)("lexer, against Java", () => {
  it.each(cases.map((c) => [c.input, c] as const))("lexes %j", (_input, c) => {
    expect(stripRecordTypes(lex(c.input))).toEqual(stripRecordTypes(c.symbols));
  });
});

describe.skipIf(!fixturesAvailable)("parser, against Java", () => {
  it.each(cases.map((c) => [c.input, c] as const))("parses %j", (_input, c) => {
    const result = astOrError(c.input, true);
    if (c.astError !== undefined) {
      expect(result.error).toBe(true);
    } else {
      expect(result.ast).toEqual(c.ast);
    }
    const grouping = astOrError(c.input, false);
    if (c.astPhilologyGroupingError !== undefined) {
      expect(grouping.error).toBe(true);
    } else {
      expect(grouping.ast).toEqual(c.astPhilologyGrouping);
    }
  });
});

describe.skipIf(!fixturesAvailable)("parser on the JSesh corpus, against Java", () => {
  it.each(corpus)("parses %s", (file) => {
    const expected = readFixture<CorpusCase>("corpus/" + file.replace(/\.gly$/, ".json"));
    const { body } = stripJSeshHeader(readCorpusFile(file));
    const result = astOrError(body, true);
    if (expected.astError !== undefined) {
      expect(result.error).toBe(true);
    } else {
      expect(result.ast).toEqual(expected.ast);
    }
  });
});
