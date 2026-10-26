/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// `@hkern` and `@vkern`: the model, the layout and the marks. The comparison with Java's parser,
/// writer and layout is in the fixture tests; these tests port the Java unit tests
/// (`KerningLayoutTest`), which check the properties of the result rather than its numbers.

import { describe, expect, it } from "vitest";
import type { KerningAxis, TextDirection, TextOrientation } from "../src/model/constants.js";
import { parseMdc } from "../src/mdcreader/builder.js";
import { Cadrat, HBox, Kerning, type ModelElement } from "../src/model/model.js";
import { signCodes } from "../src/render/codes.js";
import { HieroglyphDrawer } from "../src/render/glyphs.js";
import { Kerner } from "../src/render/insert/kerner.js";
import { ViewSilhouette } from "../src/render/insert/view-silhouette.js";
import { buildView } from "../src/render/layout.js";
import { ApproximateTextMeasurer } from "../src/render/measurers.js";
import { pathBounds } from "../src/render/path.js";
import { renderToSvg } from "../src/render/render.js";
import { DEFAULT_STYLE, withStyle, type JSeshStyle } from "../src/render/style.js";
import type { MDCView, Rect } from "../src/render/view.js";
import { glyphDataAvailable, nodeGlyphProvider } from "./layout-support.js";

function kernings(e: ModelElement): Kerning[] {
  const found: Kerning[] = e instanceof Kerning ? [e] : [];
  for (const c of e.children) found.push(...kernings(c));
  return found;
}

describe("kerning in the model", () => {
  it("is built from MdC, with its options", () => {
    const [h, v] = kernings(parseMdc("p*@hkern[a=1,b]*q-r:@vkern:a"));
    expect(h!.axis).toBe("HORIZONTAL");
    expect(h!.options).toBe("a=1,b");
    expect(v!.axis).toBe("VERTICAL");
    expect(v!.options).toBe("");
  });

  it("tells which boxes and quadrats stand for kerning operators", () => {
    const text = parseMdc("p-@hkern-q:@vkern:r");
    const kerningQuadrat = text.getChildAt(1) as Cadrat;
    expect(kerningQuadrat.getKerningAxis()).toBe("HORIZONTAL");
    const q = text.getChildAt(2) as Cadrat;
    expect(q.getKerningAxis()).toBeUndefined();
    expect((q.getChildAt(1) as HBox).getKerningAxis()).toBe("VERTICAL");
    expect(q.containsKerning()).toBe(true);
    expect((text.getChildAt(0) as Cadrat).containsKerning()).toBe(false);
  });
});

describe.skipIf(!glyphDataAvailable)("kerning layout", () => {
  function style(orientation: TextOrientation, direction: TextDirection): JSeshStyle {
    return withStyle(DEFAULT_STYLE, { options: { textOrientation: orientation, textDirection: direction } });
  }

  async function layOut(mdc: string, s: JSeshStyle): Promise<{ view: MDCView; drawer: HieroglyphDrawer }> {
    const provider = nodeGlyphProvider();
    const text = parseMdc(mdc);
    await provider.loadShapes(signCodes(text, provider));
    const drawer = new HieroglyphDrawer(provider);
    return { view: buildView(text, { style: s, drawer, measurer: new ApproximateTextMeasurer() }), drawer };
  }

  /// The bounds of the signs of a text, as drawn, in document coordinates.
  async function signBounds(mdc: string, orientation: TextOrientation, direction: TextDirection): Promise<Rect[]> {
    const s = style(orientation, direction);
    const { view, drawer } = await layOut(mdc, s);
    const silhouette = new ViewSilhouette(drawer, drawer.scaleFromFontToStyle(s), direction);
    return silhouette.silhouette(view).map((o) => pathBounds(o.path));
  }

  async function expectSameDrawing(expected: string, actual: string, orientation: TextOrientation): Promise<void> {
    for (const direction of ["LEFT_TO_RIGHT", "RIGHT_TO_LEFT"] as const) {
      const e = await signBounds(expected, orientation, direction);
      const a = await signBounds(actual, orientation, direction);
      expect(a.length, actual).toBe(e.length);
      e.forEach((r, i) => {
        const s = a[i]!;
        const message = `${actual} ${orientation} ${direction} sign ${i}`;
        expect(s.minX, message).toBeCloseTo(r.minX, 3);
        expect(s.minY, message).toBeCloseTo(r.minY, 3);
        expect(s.width, message).toBeCloseTo(r.width, 3);
        expect(s.height, message).toBeCloseTo(r.height, 3);
      });
    }
  }

  it.each([
    ["p:q", "p:@hkern:q"],
    ["p*q", "p*@vkern*q"],
    ["p:q-n", "p:@hkern:q-n"],
    ["p:q*t", "p:@hkern[a=1]:q*@vkern*t"],
    ["p:q:n", "@hkern:p:q:n:@hkern"],
  ])("ignores kerning in the wrong context: %s = %s", async (expected, actual) => {
    await expectSameDrawing(expected, actual, "HORIZONTAL");
    await expectSameDrawing(expected, actual, "VERTICAL");
  });

  // In columns, the elements of a sub-group or of a cartouche are placed one below the other:
  // there, `@vkern` is in its own context, and kerns.
  it.each([
    ["(p-q)*t", "(p-@vkern-q)*t"],
    ["<-p-q->", "<-p-@vkern-q->"],
  ])("ignores @vkern in groups in lines: %s = %s", async (expected, actual) => {
    await expectSameDrawing(expected, actual, "HORIZONTAL");
  });

  it("ignores @vkern in lines", async () => {
    await expectSameDrawing("p-q-n", "p-@vkern-q-n", "HORIZONTAL");
  });

  it("ignores @hkern in columns", async () => {
    await expectSameDrawing("p-q-n", "p-@hkern-q-n", "VERTICAL");
  });

  /// In columns, quadrats are separated by `lineSkip`; `@vkern` brings them to about `smallSkip`.
  it("kerns with @vkern in columns", async () => {
    const plain = await signBounds("t-A1", "VERTICAL", "LEFT_TO_RIGHT");
    const kerned = await signBounds("t-@vkern-A1", "VERTICAL", "LEFT_TO_RIGHT");
    const plainGap = plain[1]!.minY - plain[0]!.maxY;
    const kernedGap = kerned[1]!.minY - kerned[0]!.maxY;
    expect(kernedGap).toBeLessThan(plainGap - 1);
    // The signs don't touch, and t is not moved.
    expect(kernedGap).toBeGreaterThan(0);
    expect(kerned[0]!.minY).toBeCloseTo(plain[0]!.minY, 3);
  });

  /// The kerned groups can't be brought any closer.
  it.each([
    ["HORIZONTAL", "w-@hkern-D58"],
    ["HORIZONTAL", "A1-@hkern-x"],
    ["VERTICAL", "t-@vkern-A1"],
  ] as const)("is stable: %s %s", async (orientation, mdc) => {
    for (const direction of ["LEFT_TO_RIGHT", "RIGHT_TO_LEFT"] as const) {
      const s = style(orientation, direction);
      const { view, drawer } = await layOut(mdc, s);
      const kerner = new Kerner(drawer, drawer.scaleFromFontToStyle(s), direction);
      const axis: KerningAxis = orientation === "HORIZONTAL" ? "HORIZONTAL" : "VERTICAL";
      const shift = kerner.shift([view.getSubView(0)], view.getSubView(2), axis, s.geometry.smallSkip);
      expect(Math.abs(shift), `${mdc} ${direction}`).toBeLessThan(0.5);
    }
  });

  it("narrows a group with @hkern", async () => {
    for (const direction of ["LEFT_TO_RIGHT", "RIGHT_TO_LEFT"] as const) {
      const s = style("HORIZONTAL", direction);
      const plain = (await layOut("w*D58", s)).view.getSubView(0).getSubView(0).width;
      const kerned = (await layOut("w*@hkern*D58", s)).view.getSubView(0).getSubView(0).width;
      expect(kerned).toBeLessThan(plain - 1);
    }
  });

  it("lays out right-to-left text as the mirror of left-to-right text", async () => {
    for (const mdc of ["w-@hkern-D58", "w*@hkern*D58-n"]) {
      const a = (await layOut(mdc, style("HORIZONTAL", "LEFT_TO_RIGHT"))).view;
      const b = (await layOut(mdc, style("HORIZONTAL", "RIGHT_TO_LEFT"))).view;
      expect(b.width, mdc).toBeCloseTo(a.width, 3);
      for (let i = 0; i < a.numberOfSubviews; i++) {
        expect(b.getSubView(i).position.x, mdc).toBeCloseTo(a.getSubView(i).position.x, 3);
      }
    }
  });

  /// Justification moves kerned elements together: the kerned gap is kept, while the others grow.
  it("keeps kerning in justified lines", async () => {
    const mdc = "w-@hkern-D58-A1-A1-A1-A1-A1-A1-!A1-A1-A1-A1-A1-A1-A1-A1-A1";
    // The space between two sub views of the text, in layout coordinates.
    const gap = (text: MDCView, first: number, second: number) =>
      text.getSubView(second).position.x - (text.getSubView(first).position.x + text.getSubView(first).width);
    for (const direction of ["LEFT_TO_RIGHT", "RIGHT_TO_LEFT"] as const) {
      const plain = (await layOut(mdc, style("HORIZONTAL", direction))).view;
      const justified = (await layOut(mdc, withStyle(style("HORIZONTAL", direction), { options: { justified: true } }))).view;
      // Sub views: w, @hkern, D58, A1...
      expect(gap(justified, 0, 2), `kerned gap, ${direction}`).toBeCloseTo(gap(plain, 0, 2), 3);
      expect(gap(justified, 2, 3), `the line is justified, ${direction}`).toBeGreaterThan(gap(plain, 2, 3) + 0.5);
    }
  });

  it("draws kerning marks only when asked", async () => {
    const options = { glyphs: nodeGlyphProvider(), measurer: new ApproximateTextMeasurer() };
    const cursorColor = DEFAULT_STYLE.painting.cursorColor;
    const plain = await renderToSvg("p-@hkern-q:@vkern:r", options);
    expect(plain).not.toContain(`stroke="${cursorColor}"`);
    const marked = await renderToSvg("p-@hkern-q:@vkern:r", { ...options, showKerningMarks: true });
    // One mark per operator, of five strokes each.
    const marks = marked.match(new RegExp(`<path d="[^"]*" fill="none" stroke="${cursorColor}"`, "g")) ?? [];
    expect(marks.length).toBe(2);
    for (const m of marks) expect(m.match(/M/g)?.length).toBe(5);
  });
});
