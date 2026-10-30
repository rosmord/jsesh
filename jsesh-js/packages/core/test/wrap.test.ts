/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

import { beforeAll, describe, expect, it } from "vitest";
import { parseMdc } from "../src/mdcreader/builder.js";
import { signCodes } from "../src/render/codes.js";
import { HieroglyphDrawer } from "../src/render/glyphs.js";
import { buildView } from "../src/render/layout.js";
import { DEFAULT_STYLE, withStyle, type JSeshStyle } from "../src/render/style.js";
import type { MDCView } from "../src/render/view.js";
import { glyphDataAvailable, nodeGlyphProvider, ReplayMeasurer } from "./layout-support.js";

const WRAP_WIDTH = 150;
const A1S = "A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1";

async function layOut(mdc: string, style: JSeshStyle): Promise<MDCView> {
  const provider = nodeGlyphProvider();
  const text = parseMdc(mdc);
  await provider.loadShapes(signCodes(text, provider));
  return buildView(text, { style, drawer: new HieroglyphDrawer(provider), measurer: new ReplayMeasurer([]) });
}

/// The number of distinct lines among the subviews of a view.
function rows(view: MDCView): number {
  return new Set(view.subviews.map((v) => Math.round(v.position.y))).size;
}

const wrapping = withStyle(DEFAULT_STYLE, { geometry: { wrapWidth: WRAP_WIDTH } });

describe.skipIf(!glyphDataAvailable)("soft line wrapping", () => {
  beforeAll(() => {
    nodeGlyphProvider();
  });

  it("doesn't wrap by default", async () => {
    expect(rows(await layOut(A1S, DEFAULT_STYLE))).toBe(1);
  });

  it("wraps lines which are too long, within the wrapping width", async () => {
    const view = await layOut(A1S, wrapping);
    expect(rows(view)).toBeGreaterThan(1);
    expect(view.width).toBeLessThanOrEqual(WRAP_WIDTH);
  });

  it("keeps an element wider than the line alone on its line", async () => {
    const view = await layOut("A1-<-A1-A1-A1-A1-A1-A1-A1-A1->-A1", wrapping);
    expect(rows(view)).toBe(3);
  });

  it("wraps the content of a zone at the zone's width", async () => {
    const view = await layOut(`%zone[width=300](${A1S})`, DEFAULT_STYLE);
    const zone = view.getSubView(0);
    const content = zone.firstSubView;
    expect(rows(content)).toBeGreaterThan(1);
    expect(zone.width).toBeCloseTo(3 * DEFAULT_STYLE.geometry.maxCadratHeight);
  });

  it("doesn't let a zone without width inherit the wrapping width of the text", async () => {
    const view = await layOut(`%zone(${A1S})`, wrapping);
    expect(rows(view.getSubView(0).firstSubView)).toBe(1);
  });

  it("wraps a zone with a width at its own width, not at the text's", async () => {
    const wide = withStyle(DEFAULT_STYLE, { geometry: { wrapWidth: 1000 } });
    const view = await layOut(`%zone[width=300](${A1S})`, wide);
    expect(rows(view.getSubView(0).firstSubView)).toBeGreaterThan(1);
  });

  it("doesn't wrap the content of a zone in columns", async () => {
    const view = await layOut(`%zone[layout=columns,width=300](A1-A1-A1-A1-A1-A1)`, wrapping);
    const content = view.getSubView(0).firstSubView;
    expect(new Set(content.subviews.map((v) => Math.round(v.position.x))).size).toBe(1);
  });
});
