/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

import { describe, expect, it } from "vitest";
import { parseMdc } from "../src/mdcreader/builder.js";
import { readMdcDocument } from "../src/document/document.js";
import { documentPreferencesToMap } from "../src/document/preferences.js";
import { toMdc } from "../src/writer/writer.js";
import {
  fixturesAvailable,
  readCorpusFile,
  readFixture,
  type CorpusCase,
  type ParserCase,
} from "./fixtures.js";

const cases: ParserCase[] = fixturesAvailable ? readFixture("parser-cases.json") : [];
const corpus: string[] = fixturesAvailable ? readFixture("corpus/index.json") : [];

describe.skipIf(!fixturesAvailable)("model and writer, against Java", () => {
  it.each(cases.map((c) => [c.input, c] as const))("writes back %j", (_input, c) => {
    if (c.writtenError !== undefined) {
      expect(() => parseMdc(c.input)).toThrow();
    } else {
      expect(toMdc(parseMdc(c.input))).toEqual(c.written);
    }
  });
});

describe.skipIf(!fixturesAvailable)("JSesh corpus documents, against Java", () => {
  it.each(corpus)("reads and writes back %s", (file) => {
    const expected = readFixture<CorpusCase>("corpus/" + file.replace(/\.gly$/, ".json"));
    const document = readMdcDocument(readCorpusFile(file), { failFast: true });
    expect(documentPreferencesToMap(document.preferences)).toEqual(expected.preferences);
    const written = toMdc(document.text);
    expect(written).toEqual(expected.written);
    // Round trip: what we write reads back to the same text.
    expect(toMdc(parseMdc(written))).toEqual(written);
  });
});
