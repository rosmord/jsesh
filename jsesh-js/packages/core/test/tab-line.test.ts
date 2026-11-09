/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// `TabLine`: stop indexes and moving a tab (port of `jsesh.model.TabLine`).

import { describe, expect, it } from "vitest";
import { parseMdc } from "../src/mdcreader/builder.js";
import { Tab, TAB_AUTO, type TopItem } from "../src/model/model.js";
import { TabLine } from "../src/model/tab-line.js";
import { toMdc } from "../src/writer/writer.js";
import { HieroglyphicText } from "../src/model/model.js";

const items = (mdc: string): TopItem[] => [...parseMdc(mdc).topItems];

/// The MdC of a line of items.
const written = (line: readonly TopItem[]): string => toMdc(new HieroglyphicText(line.map((i) => i.deepCopy())));

describe("TabLine.stopIndexes", () => {
  it("gives 0 to the items which are not tabs, and the stop to the tabs", () => {
    expect(TabLine.stopIndexes(items("A1-%tab-B1-%tab-C1"))).toEqual([0, 1, 0, 2, 0]);
  });

  it("takes an explicit index if it is after the previous stop, else the next one", () => {
    expect(TabLine.stopIndexes(items("%tab[n=3]-%tab-%tab[n=2]-%tab[n=9]"))).toEqual([3, 4, 5, 9]);
  });
});

describe("TabLine.stopIndexOf", () => {
  it("counts the tabs before it on its line only", () => {
    const text = parseMdc("%tab-%tab-!%tab-%tab[n=5]");
    const tabs = text.topItems.filter((i): i is Tab => i instanceof Tab);
    expect(tabs.map((t) => TabLine.stopIndexOf(t))).toEqual([1, 2, 1, 5]);
  });

  it("is 1 for a tab outside a text", () => {
    expect(TabLine.stopIndexOf(new Tab())).toBe(1);
  });
});

describe("TabLine.moveTab", () => {
  const move = (mdc: string, position: number, delta: number): string | undefined => {
    const result = TabLine.moveTab(items(mdc), position, delta);
    return result === undefined ? undefined : written(result);
  };

  it("moves a tab to the next stop, giving it an explicit index if it no longer follows the previous one", () => {
    expect(move("A1-%tab-B1", 1, 1)).toBe("A1-%tab[n=2]-B1");
  });

  it("pushes the following tabs which would be caught up", () => {
    expect(move("%tab-A1-%tab-B1", 0, 1)).toBe("%tab[n=2]-A1-%tab-B1");
    // The tab moved onto the stop of the next one: the next one moves forward too.
    expect(move("%tab[n=2]-A1-%tab[n=3]-B1", 0, 1)).toBe("%tab[n=3]-A1-%tab[n=4]-B1");
  });

  it("moves back, and writes a tab without index when it follows the previous one again", () => {
    expect(move("%tab[n=2]-A1", 0, -1)).toBe("%tab-A1");
    expect(move("%tab-A1-%tab[n=3]-B1", 2, -1)).toBe("%tab-A1-%tab-B1");
  });

  it("can't move back onto the previous stop, nor before stop 1", () => {
    expect(move("%tab-A1", 0, -1)).toBeUndefined();
    expect(move("%tab-A1-%tab-B1", 2, -1)).toBeUndefined();
  });

  it("keeps the unchanged items, and the alignment of the moved tab", () => {
    const line = items("A1-%tab[align=end]-B1");
    const result = TabLine.moveTab(line, 1, 1)!;
    expect(result[0]).toBe(line[0]);
    expect(result[2]).toBe(line[2]);
    expect((result[1] as Tab).alignment).toBe("END");
    expect((line[1] as Tab).index).toBe(TAB_AUTO);
  });

  it("refuses an item which is not a tab", () => {
    expect(() => TabLine.moveTab(items("A1-%tab"), 0, 1)).toThrow();
  });
});
