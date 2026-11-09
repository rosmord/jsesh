/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// The dynamic tabs of one line of text: port of `jsesh.model.TabLine`.

import { Tab, TAB_AUTO, type ModelElement, type TopItem } from "./model.js";

/// The dynamic tabs ([Tab]) of one line of text: the items between two line (or page) breaks of
/// a text or of a zone.
///
/// The *stop index* of a tab is the index it actually jumps to: its explicit index if it is
/// greater than the one of the previous tab of the line, else the previous one plus one (see
/// [Tab.resolveIndex]). The layout uses the same rule (it only differs for tabs after a soft
/// wrap, which it ignores).
export class TabLine {
  private constructor() {}

  /// The stop index of each item of a line: its stop index if it is a tab, else 0.
  static stopIndexes(line: readonly ModelElement[]): number[] {
    const result: number[] = new Array<number>(line.length).fill(0);
    let previous = 0;
    line.forEach((item, i) => {
      if (item instanceof Tab) {
        previous = Tab.resolveIndex(item.index, previous);
        result[i] = previous;
      }
    });
    return result;
  }

  /// The stop index of a tab in the text, from the tabs before it on its line. The tab can be
  /// outside a text (it is then alone on its line).
  static stopIndexOf(tab: Tab): number {
    const before: ModelElement[] = [];
    let e = tab.previousSibling;
    while (e !== null && !e.isBreak()) {
      before.unshift(e);
      e = e.previousSibling;
    }
    before.push(tab);
    return TabLine.stopIndexes(before)[before.length - 1]!;
  }

  /// Moves a tab to the next (or previous) tab stop.
  ///
  /// - the other tabs keep their stops when possible. When a tab moves forward onto the stop of
  ///   the next tab, the next one moves forward too, and so on;
  /// - a tab can't move back onto (or before) the stop of the previous tab, nor before stop 1.
  ///
  /// A tab is written without an index (`%tab`) when it follows the previous one and was already
  /// written so, or is the moved tab; otherwise, it gets an explicit index.
  ///
  /// `line` is the items of the line (not modified), `position` the index of the tab to move in
  /// it, `delta` +1 to move it to the next stop, -1 to the previous one. Returns the items of the
  /// line after the move (the unchanged items are the same objects, the changed tabs are new
  /// ones), or `undefined` if the tab can't move.
  static moveTab(line: readonly TopItem[], position: number, delta: number): TopItem[] | undefined {
    if (!(line[position] instanceof Tab)) throw new Error("not a tab: " + String(line[position]));
    const old = TabLine.stopIndexes(line);
    let previous = 0;
    for (let i = 0; i < position; i++) if (line[i] instanceof Tab) previous = old[i]!;
    const target = old[position]! + delta;
    if (target <= previous) return undefined;
    const result = [...line];
    for (let i = position; i < line.length; i++) {
      const tab = line[i];
      if (!(tab instanceof Tab)) continue;
      const index = i === position ? target : Math.max(old[i]!, previous + 1);
      const auto = index === previous + 1 && (i === position || !tab.hasExplicitIndex);
      const written = auto ? TAB_AUTO : index;
      if (written !== tab.index) result[i] = tab.withIndex(written);
      previous = index;
    }
    return result;
  }
}
