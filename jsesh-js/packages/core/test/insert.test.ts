/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// `@insert`: the model, the writer, the fitting algorithm and the layout. The comparison with
/// Java's layout is in `layout.test.ts`; these tests port the Java unit tests, which check the
/// properties of the result (no overlap, scaling...) rather than its numbers.

import { describe, expect, it } from "vitest";
import { parseMdc } from "../src/mdcreader/builder.js";
import { Insert, type HieroglyphicText, type ModelElement } from "../src/model/model.js";
import { signCodes } from "../src/render/codes.js";
import { HieroglyphDrawer } from "../src/render/glyphs.js";
import {
  enlargedFrame,
  fitInsertion,
  placementTransform,
  type FitRequest,
  type HorizontalSide,
  type VerticalSide,
} from "../src/render/insert/insertion-fitter.js";
import { rectOutline, ShapeMask, type Outline } from "../src/render/insert/shape-mask.js";
import { ViewSilhouette } from "../src/render/insert/view-silhouette.js";
import { buildView } from "../src/render/layout.js";
import { ApproximateTextMeasurer } from "../src/render/measurers.js";
import { concatenate, translation, type PathSegment } from "../src/render/path.js";
import { renderToSvg } from "../src/render/render.js";
import { DEFAULT_STYLE, withStyle, type JSeshStyle } from "../src/render/style.js";
import { MDCView, Rect } from "../src/render/view.js";
import { toMdc } from "../src/writer/writer.js";
import { glyphDataAvailable, nodeGlyphProvider } from "./layout-support.js";

/// The insertions of a text, outermost first.
function inserts(text: HieroglyphicText): Insert[] {
  const found: Insert[] = [];
  const visit = (e: ModelElement) => {
    if (e instanceof Insert) found.push(e);
    e.children.forEach(visit);
  };
  visit(text);
  return found;
}

describe("@insert in the model", () => {
  it("is built from MdC", () => {
    const insert = inserts(parseMdc("i-@insert[dir=bs,dw=20,dh=30](O1,n:t)-w"))[0]!;
    expect(insert.direction).toBe("BOTTOM_START");
    expect(insert.dw).toBe(20);
    expect(insert.dh).toBe(30);
    expect(insert.main.basicItemList.numberOfChildren).toBe(1);
    expect(insert.inserted.basicItemList.numberOfChildren).toBe(1);
  });

  it.each([
    "@insert[dir=s](O1,n)",
    "@insert[dir=ts,dw=20](O1,n:t)",
    "@insert[dir=be,dw=10,dh=40](O1,n-t)",
    "i-@insert[dir=c](O1,n*t)-w",
    "@insert[dir=te](@insert[dir=bs](O1,n),t)",
    "@insert[dir=t](O1-n:t,x)",
  ])("is written back as the same model: %s", (mdc) => {
    const first = parseMdc(mdc);
    const written = toMdc(first);
    expect(written).toContain("@insert[dir=");
    expect(toMdc(parseMdc(written))).toBe(written);
    expect(inserts(parseMdc(written)).map((i) => [i.direction, i.dw, i.dh])).toEqual(
      inserts(first).map((i) => [i.direction, i.dw, i.dh]),
    );
  });

  it("defaults the enlargement to 0, and clamps negative values", () => {
    const insert = inserts(parseMdc("@insert[dir=t](O1,n)"))[0]!;
    expect(insert.dw).toBe(0);
    expect(insert.dh).toBe(0);
    insert.dw = -5;
    expect(insert.dw).toBe(0);
  });

  it("builds nested insertions", () => {
    const found = inserts(parseMdc("@insert[dir=te](@insert[dir=bs](O1,n),t)"));
    expect(found.map((i) => i.direction)).toEqual(["TOP_END", "BOTTOM_START"]);
  });

  it("is copied with its options", () => {
    const insert = inserts(parseMdc("@insert[dir=be,dw=15](O1,n:t)"))[0]!;
    const copy = insert.deepCopy();
    expect([copy.direction, copy.dw, copy.dh]).toEqual(["BOTTOM_END", 15, 0]);
    expect(copy.main).not.toBe(insert.main);
    expect(toMdc(parseMdc("@insert[dir=be,dw=15](O1,n:t)"))).toBe("@insert[dir=be,dw=15](O1,n:t)");
  });
});

// ---- ShapeMask

function polygon(points: [number, number][]): Outline {
  const path: PathSegment[] = points.map((p, i) => ({ type: i === 0 ? "M" : "L", points: p }));
  path.push({ type: "Z" });
  return { path, evenOdd: false };
}

function rect(x: number, y: number, w: number, h: number): Outline {
  return rectOutline(new Rect(x, y, w, h));
}

function circle(cx: number, cy: number, r: number): [number, number][] {
  return Array.from({ length: 48 }, (_, i) => [cx + r * Math.cos((i * Math.PI) / 24), cy + r * Math.sin((i * Math.PI) / 24)]);
}

describe("ShapeMask", () => {
  const FRAME = new Rect(0, 0, 64, 64);

  it("fills the expected cells for a rectangle", () => {
    const mask = new ShapeMask(FRAME, 64);
    mask.fill(rect(10, 20, 5, 3));
    expect(mask.get(10, 20)).toBe(true);
    expect(mask.get(14, 22)).toBe(true);
    expect(mask.get(9, 20)).toBe(false);
    expect(mask.get(10, 19)).toBe(false);
    expect(mask.get(10, 23)).toBe(false);
  });

  it("applies the transform", () => {
    const mask = new ShapeMask(FRAME, 64);
    mask.fill(rect(0, 0, 4, 4), translation(30, 40));
    expect(mask.get(31, 41)).toBe(true);
    expect(mask.get(1, 1)).toBe(false);
  });

  it("respects holes", () => {
    const ring: Outline = {
      path: [...polygon(circle(30, 30, 30)).path, ...polygon(circle(30, 30, 15)).path],
      evenOdd: true,
    };
    const mask = new ShapeMask(FRAME, 64);
    mask.fill(ring);
    expect(mask.get(30, 30)).toBe(false);
    expect(mask.get(30, 5)).toBe(true);
  });

  it("flattens curves", () => {
    // A disc, with four cubic curves.
    const k = 0.5523 * 30;
    const disc: Outline = {
      path: [
        { type: "M", points: [60, 30] },
        { type: "C", points: [60, 30 + k, 30 + k, 60, 30, 60] },
        { type: "C", points: [30 - k, 60, 0, 30 + k, 0, 30] },
        { type: "C", points: [0, 30 - k, 30 - k, 0, 30, 0] },
        { type: "C", points: [30 + k, 0, 60, 30 - k, 60, 30] },
        { type: "Z" },
      ],
      evenOdd: false,
    };
    const mask = new ShapeMask(FRAME, 64);
    mask.fill(disc);
    expect(mask.get(30, 30)).toBe(true);
    expect(mask.get(1, 1)).toBe(false);
    expect(mask.get(30, 2)).toBe(true);
  });

  it("ignores shapes outside the frame", () => {
    const mask = new ShapeMask(FRAME, 64);
    mask.fill(rect(-100, -100, 50, 50));
    expect(mask.isEmpty()).toBe(true);
    mask.fill(rect(-100, -100, 200, 200));
    expect(mask.get(0, 0)).toBe(true);
    expect(mask.get(63, 63)).toBe(true);
  });

  it("does not lose thin strokes", () => {
    const mask = new ShapeMask(FRAME, 64);
    mask.fill(rect(10, 10.4, 30, 0.3));
    expect(mask.isEmpty()).toBe(false);
  });

  it("dilates by one cell in each direction (4-connected)", () => {
    const mask = new ShapeMask(FRAME, 64);
    mask.fill(rect(10, 10, 1, 1));
    const dilated = mask.dilate(1);
    expect(dilated.get(9, 10)).toBe(true);
    expect(dilated.get(11, 10)).toBe(true);
    expect(dilated.get(10, 9)).toBe(true);
    expect(dilated.get(10, 11)).toBe(true);
    expect(dilated.get(12, 10)).toBe(false);
    expect(dilated.get(11, 11)).toBe(false);
  });

  it("computes overlaps and intersections", () => {
    const a = new ShapeMask(FRAME, 64);
    a.fill(rect(0, 0, 10, 10));
    const b = new ShapeMask(a);
    b.fill(rect(8, 8, 10, 10));
    expect(a.intersects(b)).toBe(true);
    expect(a.overlap(b)).toBe(4);
    b.clear();
    b.fill(rect(20, 20, 5, 5));
    expect(a.intersects(b)).toBe(false);
  });
});

// ---- InsertionFitter

describe("fitInsertion", () => {
  const FRAME = new Rect(0, 0, 20, 20);

  function request(target: Outline[], inserted: Rect, h: HorizontalSide, v: VerticalSide, frame = FRAME): FitRequest {
    return { target, frame, inserted: [rectOutline(inserted)], insertedBox: inserted, horizontal: h, vertical: v };
  }

  it("keeps the natural size at the edge when the target is empty", () => {
    const p = fitInsertion(request([], new Rect(0, 0, 10, 10), "LEFT", "CENTER"));
    expect(p.fits).toBe(true);
    expect(p.scale).toBe(1);
    // left-middle of the box (0,5) goes on the left-middle of the frame (0,10)
    expect(p.translateX).toBeCloseTo(0, 9);
    expect(p.translateY).toBeCloseTo(5, 9);
  });

  it("scales an element which is too large to the frame", () => {
    const p = fitInsertion(request([], new Rect(0, 0, 30, 30), "CENTER", "CENTER"));
    expect(p.fits).toBe(true);
    // 30 * 2^(-3/4) = 17.8 fits in 20, and 30 * 2^(-2/4) = 21.2 does not.
    expect(p.scale).toBeCloseTo(Math.pow(2, -0.75), 9);
  });

  it("shrinks a side insertion until it clears the target", () => {
    const p = fitInsertion(request([rect(8, 0, 12, 20)], new Rect(0, 0, 10, 10), "LEFT", "CENTER"));
    expect(p.fits).toBe(true);
    expect(p.scale).toBeCloseTo(Math.pow(2, -0.5), 9);
    expect(p.translateX).toBeCloseTo(0, 9);
  });

  it("uses the corner for an oblique insertion", () => {
    // Free space: an 8x8 hole in the bottom-left corner.
    const target = polygon([
      [0, 0],
      [20, 0],
      [20, 20],
      [8, 20],
      [8, 12],
      [0, 12],
    ]);
    const p = fitInsertion(request([target], new Rect(0, 0, 10, 10), "LEFT", "BOTTOM"));
    expect(p.fits).toBe(true);
    const s = Math.pow(2, -0.5);
    expect(p.scale).toBeCloseTo(s, 9);
    // bottom-left of the box (0,10) goes on the bottom-left of the frame (0,20)
    expect(p.translateX).toBeCloseTo(0, 9);
    expect(p.translateY).toBeCloseTo(20 - 10 * s, 9);
  });

  it("moves away from a blocked corner", () => {
    // 10x10 hole in the bottom-left corner, with a small obstacle in the very corner.
    const target = polygon([
      [0, 0],
      [20, 0],
      [20, 20],
      [10, 20],
      [10, 10],
      [0, 10],
    ]);
    const p = fitInsertion(request([target, rect(0, 19, 1, 1)], new Rect(0, 0, 6, 6), "LEFT", "BOTTOM"));
    expect(p.fits).toBe(true);
    expect(p.scale).toBe(1);
    // The first free grid point is (1,1): one 20/16 step to the right and up.
    expect(p.translateX).toBeCloseTo(1.25, 9);
    expect(p.translateY).toBeCloseTo(20 - 1.25 - 6, 9);
  });

  it("lets the element grow when the frame is enlarged", () => {
    const inserted = new Rect(0, 0, 10, 10);
    const frame = enlargedFrame(FRAME, inserted, "RIGHT", "CENTER", 120, 0);
    expect(frame).toEqual(new Rect(0, 0, 32, 20));
    const p = fitInsertion(request([rect(0, 0, 20, 20)], inserted, "RIGHT", "CENTER", frame));
    expect(p.fits).toBe(true);
    expect(p.scale).toBe(1);
    // right-middle (10,5) goes on (32,10)
    expect(p.translateX).toBeCloseTo(22, 9);
  });

  it("reports when nothing fits", () => {
    const p = fitInsertion(request([rect(0, 0, 20, 20)], new Rect(0, 0, 10, 10), "LEFT", "CENTER"));
    expect(p.fits).toBe(false);
    expect(p.scale).toBe(0.25);
  });

  it("enlarges the frame on the side of the insertion", () => {
    const inserted = new Rect(0, 0, 10, 8);
    expect(enlargedFrame(FRAME, inserted, "LEFT", "BOTTOM", 50, 50)).toEqual(new Rect(-5, 0, 25, 24));
    expect(enlargedFrame(FRAME, inserted, "CENTER", "CENTER", 50, 50)).toEqual(new Rect(-2.5, -2, 25, 24));
    // dh is ignored when inserting from the side
    expect(enlargedFrame(FRAME, inserted, "RIGHT", "CENTER", 50, 50)).toEqual(new Rect(0, 0, 25, 20));
  });

  it("gives the transform of a placement: scale, then translate", () => {
    const m = placementTransform({ scale: 0.5, translateX: 3, translateY: 4, fits: true });
    expect(m).toEqual(concatenate(translation(3, 4), [0.5, 0, 0, 0.5, 0, 0]));
    expect([m[0] * 10 + m[4], m[3] * 10 + m[5]]).toEqual([8, 9]);
  });
});

// ---- Layout, with real signs

describe.skipIf(!glyphDataAvailable)("@insert layout", () => {
  async function layOut(mdc: string, style: JSeshStyle): Promise<{ view: MDCView; drawer: HieroglyphDrawer }> {
    const provider = nodeGlyphProvider();
    const text = parseMdc(mdc);
    await provider.loadShapes(signCodes(text, provider));
    const drawer = new HieroglyphDrawer(provider);
    return { view: buildView(text, { style, drawer, measurer: new ApproximateTextMeasurer() }), drawer };
  }

  function findInsert(view: MDCView): MDCView | undefined {
    if (view.model instanceof Insert) return view;
    for (const sub of view.subviews) {
      const found = findInsert(sub);
      if (found !== undefined) return found;
    }
    return undefined;
  }

  /// The outlines of a sub view of the insert, in the insert's drawing coordinates.
  function placedSilhouette(silhouette: ViewSilhouette, insert: MDCView, index: number): Outline[] {
    const b = insert.getSubViewBounds(index);
    return silhouette.silhouette(insert.getSubView(index)).map((o) => ({
      ...o,
      path: o.path.map((s) =>
        s.type === "Z" ? s : ({ type: s.type, points: s.points.map((v, i) => v + (i % 2 === 0 ? b.minX : b.minY)) } as PathSegment),
      ),
    }));
  }

  async function checkNoOverlap(mdc: string, direction: "LEFT_TO_RIGHT" | "RIGHT_TO_LEFT"): Promise<void> {
    const style = withStyle(DEFAULT_STYLE, { options: { textDirection: direction } });
    const { view, drawer } = await layOut(mdc, style);
    const insert = findInsert(view)!;
    expect(insert, mdc).toBeDefined();
    expect(insert.numberOfSubviews).toBe(2);
    const silhouette = new ViewSilhouette(drawer, drawer.scaleFromFontToStyle(style), direction);
    const whole = new Rect(0, 0, insert.width, insert.height);
    const main = new ShapeMask(whole, 64);
    for (const o of placedSilhouette(silhouette, insert, 0)) main.fill(o);
    const inserted = new ShapeMask(main);
    for (const o of placedSilhouette(silhouette, insert, 1)) inserted.fill(o);
    expect(main.isEmpty(), "main sign has an outline: " + mdc).toBe(false);
    expect(inserted.isEmpty(), "inserted element has an outline: " + mdc).toBe(false);
    expect(main.intersects(inserted), `no overlap: ${mdc} ${direction}`).toBe(false);
    // Both views lie in the group's box.
    for (let i = 0; i < 2; i++) {
      const b = insert.getSubViewBounds(i);
      expect(b.minX).toBeGreaterThanOrEqual(-1e-3);
      expect(b.minY).toBeGreaterThanOrEqual(-1e-3);
      expect(b.maxX).toBeLessThanOrEqual(insert.width + 1e-3);
      expect(b.maxY).toBeLessThanOrEqual(insert.height + 1e-3);
    }
  }

  it.each(["s", "e", "t", "b", "ts", "bs", "te", "be", "c"])(
    "does not overlap the inserted element and the main sign (dir=%s)",
    async (dir) => {
      await checkNoOverlap(`@insert[dir=${dir}](O1,n:t)`, "LEFT_TO_RIGHT");
      await checkNoOverlap(`@insert[dir=${dir}](O1,n:t)`, "RIGHT_TO_LEFT");
    },
  );

  it.each(["te", "bs", "ts"])("does not overlap in nested insertions (dir=%s)", async (dir) => {
    const mdc = `@insert[dir=${dir}](@insert[dir=c](O1,n:t),x)`;
    await checkNoOverlap(mdc, "LEFT_TO_RIGHT");
    await checkNoOverlap(mdc, "RIGHT_TO_LEFT");
  });

  it("accepts a group as the main element", async () => {
    await checkNoOverlap("@insert[dir=e](O1-O1,n)", "LEFT_TO_RIGHT");
  });

  it("scales the inserted element down to fit, uniformly", async () => {
    const { view } = await layOut("@insert[dir=c](O1,n:t:n:t)", DEFAULT_STYLE);
    const inserted = findInsert(view)!.getSubView(1);
    expect(inserted.xScale).toBeLessThan(1);
    expect(inserted.xScale).toBe(inserted.yScale);
  });

  it("widens the group when the frame is enlarged", async () => {
    const plain = findInsert((await layOut("@insert[dir=e](O1,n)", DEFAULT_STYLE)).view)!;
    const wide = findInsert((await layOut("@insert[dir=e,dw=100](O1,n)", DEFAULT_STYLE)).view)!;
    expect(wide.width).toBeGreaterThan(plain.width);
    expect(wide.height).toBeCloseTo(plain.height, 3);
  });

  it("is drawn: both parts are in the SVG", async () => {
    const svg = await renderToSvg("@insert[dir=bs](O1,n:t)", { glyphs: nodeGlyphProvider(), measurer: new ApproximateTextMeasurer() });
    // Two different signs are defined once each, and the inserted ones are scaled down.
    expect(svg.match(/<path id=/g)?.length).toBe(3);
    expect(svg).toMatch(/<g transform="scale\(0\.5\)">/);
  });
});
