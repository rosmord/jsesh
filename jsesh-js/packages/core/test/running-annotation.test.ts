/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Running annotations (`%haplography`, `%lacuna`): reading, writing, layout (fixed size and
/// `size=fill`, in lines and columns, with and without tabs) and drawing. Ported from the Java
/// tests `RunningAnnotationMdcTest`, `RunningAnnotationLayoutTest` and
/// `RunningAnnotationDrawingTest`. The view trees are also compared with Java's by `layout.test.ts`
/// (cases of `ParserCases`).

import { beforeAll, describe, expect, it } from "vitest";
import { parseMdc } from "../src/mdcreader/builder.js";
import { RunningAnnotation, Zone } from "../src/model/model.js";
import { parseMdcAst } from "../src/parser/parser.js";
import { MdcLexer } from "../src/parser/lexer.js";
import { MdcSymbolCode } from "../src/parser/symbols.js";
import { signCodes } from "../src/render/codes.js";
import { HieroglyphDrawer } from "../src/render/glyphs.js";
import { buildView } from "../src/render/layout.js";
import { ApproximateTextMeasurer } from "../src/render/measurers.js";
import { renderToSvg } from "../src/render/render.js";
import { DEFAULT_STYLE, withStyle, type JSeshStyle } from "../src/render/style.js";
import type { MDCView } from "../src/render/view.js";
import { toMdc } from "../src/writer/writer.js";
import { glyphDataAvailable, nodeGlyphProvider, ReplayMeasurer } from "./layout-support.js";

const EPSILON = 1e-2;
const QUADRAT = DEFAULT_STYLE.geometry.maxCadratHeight;
const GAP = DEFAULT_STYLE.geometry.smallSkip;
const COLUMN_GAP = DEFAULT_STYLE.geometry.columnQuadratSkip;
/// The width of a zone of `width=1000`.
const TEN = 10 * QUADRAT;

/// The symbols of a text, as `CODE` or `CODE:value`.
function symbols(mdc: string): string[] {
  const lexer = new MdcLexer(mdc);
  const result: string[] = [];
  for (;;) {
    const s = lexer.nextSymbol();
    if (s === undefined || s.code === MdcSymbolCode.EOF) return result;
    const hasNumber = typeof s.value === "number";
    result.push(hasNumber ? `${s.code}:${String(s.value)}` : s.code);
  }
}

function annotation(mdc: string): RunningAnnotation {
  const item = parseMdc(mdc).topItems.find((i) => i instanceof RunningAnnotation);
  expect(item).toBeInstanceOf(RunningAnnotation);
  return item as RunningAnnotation;
}

describe("running annotations, reading", () => {
  it("reads the keywords, with or without options", () => {
    expect(symbols("%haplography")).toEqual(["HAPLOGRAPHY"]);
    expect(symbols("%lacuna")).toEqual(["LACUNA"]);
    // "@" is the prefix of user signs: it doesn't start the keywords.
    expect(symbols("@haplography")).toEqual(["HIEROGLYPH"]);
    const bare = annotation("A1-%haplography-B1");
    expect(bare.kind).toBe("HAPLOGRAPHY");
    expect(bare.fill).toBe(false);
    expect(bare.percent).toBe(100);
  });

  it("reads the legacy forms as one symbol with their size, each dot being half a quadrat", () => {
    expect(symbols("^-..-..-^")).toEqual(["HAPLOGRAPHY:200"]);
    expect(symbols("?-..-..-..-?")).toEqual(["LACUNA:300"]);
    expect(symbols("^-.-^")).toEqual(["HAPLOGRAPHY:50"]);
    expect(symbols("?-.-..-?")).toEqual(["LACUNA:150"]);
    expect(symbols("^-..-.-^")).toEqual(["HAPLOGRAPHY:150"]);
    expect(symbols("A1-^-..-..-^-B1")).toEqual([
      "HIEROGLYPH",
      "SEPARATOR",
      "HAPLOGRAPHY:200",
      "SEPARATOR",
      "HIEROGLYPH",
    ]);
  });

  it("leaves the other uses of the marks alone", () => {
    expect(symbols("^")).toEqual(["TOGGLE"]);
    expect(symbols("?10")).toEqual(["ABSOLUTE_TAB:10"]);
    // The opening and the closing marks must be the same, and the form complete.
    expect(symbols("^-..-?")[0]).toBe("TOGGLE");
    expect(symbols("^-..-")[0]).toBe("TOGGLE");
    expect(symbols("^-A1-^")[0]).toBe("TOGGLE");
    expect(symbols("^--^")[0]).toBe("TOGGLE");
    // The philology marks win.
    const codes = symbols("[?-..-..-?]");
    expect(codes[0]).toBe("BEGIN_PHIL");
    expect(codes.at(-1)).toBe("END_PHIL");
    expect(codes.some((c) => c.startsWith("LACUNA"))).toBe(false);
  });

  it("keeps the options in the AST, and gives the same AST for the legacy form", () => {
    const ast = parseMdcAst("A1-%lacuna[size=fill]-^-..-..-^").topItems.items;
    expect(ast[1]).toEqual({
      nodeType: "RunningAnnotation",
      kind: "LACUNA",
      options: { nodeType: "OptionList", options: [{ nodeType: "Option", name: "size", value: { nodeType: "Text", value: "fill" } }] },
    });
    const legacy = parseMdcAst("^-..-..-^").topItems.items[0];
    const keyword = parseMdcAst("%haplography[size=200]").topItems.items[0];
    expect(legacy).toEqual(keyword);
  });

  it("reads the size in percents of a quadrat, fill, or one quadrat if missing or invalid", () => {
    expect(annotation("%haplography[size=250]").percent).toBe(250);
    expect(annotation("%haplography[size=fill]").fill).toBe(true);
    expect(annotation("%lacuna[size=0]").percent).toBe(100);
    expect(annotation("%lacuna[size=foo]").percent).toBe(100);
    expect(annotation("%lacuna[size=foo]").fill).toBe(false);
    expect(annotation("A1-^-..-..-^-B1").percent).toBe(200);
  });

  it("is a top item between two quadrats", () => {
    const items = parseMdc("A1-%haplography-B1").topItems;
    expect(items.map((i) => i.constructor.name)).toEqual(["Cadrat", "RunningAnnotation", "Cadrat"]);
  });

  it("copies equal", () => {
    const a = annotation("%lacuna[size=fill]");
    expect(a.deepCopy().fill).toBe(true);
    expect(a.deepCopy().kind).toBe("LACUNA");
  });
});

describe("running annotations, writing", () => {
  it("always writes the size", () => {
    expect(toMdc(parseMdc("A1-%haplography-B1"))).toBe("A1-%haplography[size=100]-B1");
    expect(toMdc(parseMdc("%lacuna[size=fill]"))).toBe("%lacuna[size=fill]");
  });

  it("writes the legacy forms as keywords", () => {
    expect(toMdc(parseMdc("A1-^-..-..-^-B1"))).toBe("A1-%haplography[size=200]-B1");
    expect(toMdc(parseMdc("?-.-?"))).toBe("%lacuna[size=50]");
  });

  it.each(["A1-%haplography[size=250]-B1", "%lacuna[size=fill]-A1", "%zone(A1-%tab-%haplography[size=fill])"])(
    "round-trips %s",
    (mdc) => {
      expect(toMdc(parseMdc(mdc))).toBe(mdc);
    },
  );
});

async function layOut(mdc: string, style: JSeshStyle = DEFAULT_STYLE): Promise<MDCView> {
  const provider = nodeGlyphProvider();
  const text = parseMdc(mdc);
  await provider.loadShapes(signCodes(text, provider));
  return buildView(text, { style, drawer: new HieroglyphDrawer(provider), measurer: new ReplayMeasurer([]) });
}

/// The content of the first item of the text, which is a zone.
async function zone(mdc: string): Promise<MDCView> {
  const view = (await layOut(mdc)).getSubView(0);
  expect(view.model).toBeInstanceOf(Zone);
  return view.firstSubView;
}

const maxX = (v: MDCView) => v.position.x + v.width;
const maxY = (v: MDCView) => v.position.y + v.height;

describe.skipIf(!glyphDataAvailable)("running annotations, layout", () => {
  beforeAll(() => {
    nodeGlyphProvider();
  });

  describe("fixed size", () => {
    it("is a percent of a quadrat long, and one quadrat across", async () => {
      const content = await zone("%zone[width=1000](A1-%haplography[size=250]-A1)");
      const a = content.getSubView(1);
      expect(a.model).toBeInstanceOf(RunningAnnotation);
      expect(a.width).toBeCloseTo(2.5 * QUADRAT, 1);
      expect(a.height).toBeCloseTo(QUADRAT, 1);
      expect(a.isFilling).toBe(false);
      // It takes room in the line.
      expect(content.getSubView(2).position.x).toBeCloseTo(maxX(a) + GAP, 1);
    });

    it("is a height in columns", async () => {
      const content = await zone("%zone[layout=columns](A1-%lacuna[size=200]-A1)");
      const a = content.getSubView(1);
      expect(a.height).toBeCloseTo(2 * QUADRAT, 1);
      expect(a.width).toBeCloseTo(DEFAULT_STYLE.geometry.maxCadratWidth, 1);
      expect(content.getSubView(2).position.y).toBeCloseTo(maxY(a) + COLUMN_GAP, 1);
    });

    it("has the same layout in the legacy form", async () => {
      const legacy = await layOut("A1-^-..-..-^-A1");
      const modern = await layOut("A1-%haplography[size=200]-A1");
      expect(legacy.getSubView(1).width).toBeCloseTo(modern.getSubView(1).width, 3);
      expect(legacy.getSubView(2).position.x).toBeCloseTo(modern.getSubView(2).position.x, 3);
    });
  });

  describe("fill, without tabs", () => {
    it("takes the rest of its line", async () => {
      const content = await zone("%zone[width=1000](A1-%haplography[size=fill]-A1)");
      const a = content.getSubView(1);
      const last = content.getSubView(2);
      expect(a.isFilling).toBe(true);
      expect(a.width).toBeGreaterThan(5 * QUADRAT);
      expect(maxX(last)).toBeCloseTo(TEN, 1);
      // Only the usual gaps are left around it.
      expect(a.position.x).toBeCloseTo(maxX(content.getSubView(0)) + GAP, 1);
      expect(last.position.x).toBeCloseTo(maxX(a) + GAP, 1);
    });

    it("ends at the end of the line when it is last", async () => {
      const content = await zone("%zone[width=1000](A1-%lacuna[size=fill])");
      expect(maxX(content.getSubView(1))).toBeCloseTo(TEN, 1);
    });

    it("shares the free space with the other ones", async () => {
      const content = await zone("%zone[width=1000](%haplography[size=fill]-A1-%lacuna[size=fill])");
      expect(content.getSubView(0).width).toBeCloseTo(content.getSubView(2).width, 1);
      expect(maxX(content.getSubView(2))).toBeCloseTo(TEN, 1);
    });

    it("takes the width of the longest line without a width", async () => {
      const text = await layOut("A1-%haplography[size=fill]-A1!A1-A1-A1-A1-A1");
      const lastOfFirstLine = text.getSubView(2);
      const lastOfSecondLine = text.getSubView(text.numberOfSubviews - 1);
      expect(maxX(lastOfFirstLine)).toBeCloseTo(maxX(lastOfSecondLine), 1);
    });

    it("takes the rest of the column in columns", async () => {
      const content = await zone("%zone[layout=columns,height=1000](A1-%haplography[size=fill]-A1)");
      const a = content.getSubView(1);
      expect(a.height).toBeGreaterThan(5 * QUADRAT);
      expect(maxY(content.getSubView(2))).toBeCloseTo(TEN, 1);
      expect(content.getSubView(2).position.y).toBeCloseTo(maxY(a) + COLUMN_GAP, 1);
    });

    it("fills right-to-left text the same way", async () => {
      const content = await zone("%zone[dir=rl,width=1000](A1-%haplography[size=fill]-A1)");
      expect(maxX(content.getSubView(2))).toBeCloseTo(TEN, 1);
    });

    it("keeps its natural length when the alignment of the lines is ignored", async () => {
      const wrapped = withStyle(DEFAULT_STYLE, {
        geometry: { wrapWidth: 20 * QUADRAT },
        options: { ignoreLineAlignment: true },
      });
      const text = await layOut("A1-%haplography[size=fill]-A1", wrapped);
      expect(text.getSubView(1).width).toBeCloseTo(0, 3);
    });
  });

  describe("fill, with tabs", () => {
    it("fills up to the next stop", async () => {
      // The second line pushes the first stop. Views: 0 A1, 1 annotation, 2 %tab, 3 B1, 4 !, 5-7 G17, 8 %tab, 9 C1.
      const content = await zone("%zone[width=1000](A1-%haplography[size=fill]-%tab-B1-!G17-G17-G17-%tab-C1)");
      const a = content.getSubView(1);
      const stop = content.getSubView(3).position.x;
      expect(stop).toBeGreaterThan(3 * QUADRAT);
      expect(maxX(a)).toBeCloseTo(stop - GAP, 1);
      expect(a.position.x).toBeCloseTo(maxX(content.getSubView(0)) + GAP, 1);
    });

    it("does not push the stop", async () => {
      const withAnnotation = await zone("%zone[width=1000](A1-%haplography[size=fill]-%tab-B1)");
      const without = await zone("%zone[width=1000](A1-%tab-B1)");
      expect(withAnnotation.getSubView(3).position.x).toBeCloseTo(without.getSubView(2).position.x, 1);
    });

    it("fills a cell which holds only an annotation", async () => {
      // Views: 0 A1, 1 %tab, 2 annotation, 3 %tab, 4 B1.
      const content = await zone(
        "%zone[width=1000](A1-%tab-%lacuna[size=fill]-%tab-B1-!G17-%tab-G17-G17-G17-%tab-G17)",
      );
      const a = content.getSubView(2);
      const stop1 = content.getSubView(1).position.x;
      const stop2 = content.getSubView(4).position.x;
      expect(a.width).toBeGreaterThan(QUADRAT);
      // The cell goes from stop 1 to stop 2 minus the gap.
      expect(a.position.x).toBeCloseTo(stop1, 1);
      expect(maxX(a)).toBeCloseTo(stop2 - GAP, 1);
    });

    it("goes to the end of the table in the last cell", async () => {
      const content = await zone("%zone[width=1000](A1-%tab-B1-%haplography[size=fill])");
      expect(maxX(content.getSubView(3))).toBeCloseTo(TEN, 1);
    });

    it("fills the cell in columns", async () => {
      const content = await zone(
        "%zone[layout=columns,height=1000](A1-%haplography[size=fill]-%tab-B1-!G17-G17-G17-%tab-C1)",
      );
      const a = content.getSubView(1);
      const stop = content.getSubView(3).position.y;
      expect(stop).toBeGreaterThan(3 * QUADRAT);
      expect(maxY(a)).toBeCloseTo(stop - COLUMN_GAP, 1);
    });

    it("still ignores the zones which fill in lines with tabs", async () => {
      const natural = await zone("%zone[width=1000](A1-%tab-%zone(A1))");
      const content = await zone("%zone[width=1000](A1-%tab-%zone[fill=yes](A1))");
      expect(content.getSubView(2).width).toBeCloseTo(natural.getSubView(2).width, 3);
    });
  });
});

describe.skipIf(!glyphDataAvailable)("running annotations, drawing", () => {
  const options = () => ({ glyphs: nodeGlyphProvider(), measurer: new ApproximateTextMeasurer() });

  /// The `d` attribute of the paths of the picture which are not shapes of signs.
  function lines(svg: string): string[] {
    return [...svg.matchAll(/<path d="([^"]*)" fill="none"[^>]*>/g)].map((m) => m[0]);
  }

  it("draws an haplography as a solid line with an arrow head at each end", async () => {
    const svg = await renderToSvg("%haplography[size=300]", options());
    const [path] = lines(svg);
    expect(path).toBeDefined();
    expect(path).not.toContain("stroke-dasharray");
    // The line, and two arrow heads (a head is a polyline of two segments).
    expect((path!.match(/M/g) ?? []).length).toBe(3);
    expect(svg).not.toContain("NaN");
  });

  it("draws a lacuna dashed, without arrows", async () => {
    const svg = await renderToSvg("%lacuna[size=300]", options());
    const [path] = lines(svg);
    expect(path).toContain("stroke-dasharray");
    expect((path!.match(/M/g) ?? []).length).toBe(1);
  });

  it("draws a vertical line in columns", async () => {
    const svg = await renderToSvg("%lacuna[size=300]", {
      ...options(),
      style: withStyle(DEFAULT_STYLE, { options: { textOrientation: "VERTICAL" } }),
    });
    const [path] = lines(svg);
    const m = /d="M([\d.-]+) ([\d.-]+)L([\d.-]+) ([\d.-]+)"/.exec(path!);
    expect(m).not.toBeNull();
    // Same x at both ends: vertical.
    expect(m![1]).toBe(m![3]);
    expect(m![2]).not.toBe(m![4]);
  });

  it("draws the line of a lacuna in right-to-left text too", async () => {
    const svg = await renderToSvg("A1-%lacuna[size=300]-A1", {
      ...options(),
      style: withStyle(DEFAULT_STYLE, { options: { textDirection: "RIGHT_TO_LEFT" } }),
    });
    expect(lines(svg).some((l) => l.includes("stroke-dasharray"))).toBe(true);
  });

  it("draws the marks of the tabs only when asked to", async () => {
    const mdc = "%zone(A1-%tab-B1-%tab-C1)";
    const without = await renderToSvg(mdc, options());
    const withTabs = await renderToSvg(mdc, { ...options(), showTabs: true });
    expect(without).not.toContain("#1e64e6");
    expect(withTabs).toContain("#1e64e6");
    // The indexes of the stops, 1 and 2.
    expect([...withTabs.matchAll(/text-anchor="start"[^>]*>(\d+)</g)].map((m) => m[1])).toEqual(["1", "2"]);
    expect(withTabs).not.toContain("NaN");
  });
});
