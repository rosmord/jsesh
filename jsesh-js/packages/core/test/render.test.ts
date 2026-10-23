/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

import { writeFileSync } from "node:fs";
import { describe, expect, it } from "vitest";
import { ApproximateTextMeasurer } from "../src/render/measurers.js";
import { renderToSvg } from "../src/render/render.js";
import { fixturesAvailable, readCorpusFile, readFixture, type ParserCase } from "./fixtures.js";
import { glyphDataAvailable, nodeGlyphProvider } from "./layout-support.js";

const available = fixturesAvailable && glyphDataAvailable;
const cases: ParserCase[] = available ? readFixture("parser-cases.json") : [];
const corpus: string[] = available ? readFixture("corpus/index.json") : [];

/// Checks that tags are balanced (a cheap well-formedness test).
function checkBalanced(svg: string): void {
  const stack: string[] = [];
  for (const m of svg.matchAll(/<(\/?)([a-zA-Z]+)[^>]*?(\/?)>/g)) {
    if (m[3] === "/") continue;
    if (m[1] === "/") expect(stack.pop()).toBe(m[2]);
    else stack.push(m[2]!);
  }
  expect(stack).toEqual([]);
}

describe.skipIf(!available)("SVG rendering", () => {
  const options = () => ({ glyphs: nodeGlyphProvider(), measurer: new ApproximateTextMeasurer() });

  it.each(cases.filter((c) => c.writtenError === undefined).map((c) => c.input))("renders %j", async (mdc) => {
    const svg = await renderToSvg(mdc, options());
    expect(svg.startsWith("<svg")).toBe(true);
    checkBalanced(svg);
    expect(svg).not.toContain("NaN");
  });

  it.each(corpus)("renders %s", async (file) => {
    const svg = await renderToSvg(readCorpusFile(file), { ...options(), title: file });
    checkBalanced(svg);
    expect(svg).not.toContain("NaN");
    // eslint-disable-next-line no-control-regex
    expect(svg).not.toMatch(/[\u0000-\u0008\u000b\u000c\u000e-\u001f]/);
    if (process.env.JSESH_SVG_DIR) writeFileSync(`${process.env.JSESH_SVG_DIR}/${file}.svg`, svg);
  });
});
