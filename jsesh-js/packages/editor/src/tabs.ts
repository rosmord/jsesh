/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// The dynamic-tab commands of the editor, as functions on a list of top items: port of the
/// "dynamic tabs" section of `JMDCEditorWorkflow`.
///
/// They don't know where the items are. The session applies them to the top-level text; when the
/// editor can put its cursor inside a zone (where `%tab` is laid out), the same functions will do
/// for the items of that zone.

import { Tab, TabLine, type TopItem } from "@jsesh/core";

/// How a command changes the items of a line: from the items of the line and the position of the
/// tab in them, the new items of the line, or `undefined` for "no change".
export type TabLineChange = (line: readonly TopItem[], position: number) => TopItem[] | undefined;

/// The index of the tab a tab command works on: the first tab in the selection, else the item
/// just before the cursor, else the item just after it. `mark` is the other end of the
/// selection, if any. Returns -1 if there is none.
export function tabNearCaret(items: readonly TopItem[], caret: number, mark: number | null): number {
  if (mark !== null) {
    for (let i = Math.min(caret, mark); i < Math.max(caret, mark); i++) {
      if (items[i] instanceof Tab) return i;
    }
  }
  if (caret > 0 && items[caret - 1] instanceof Tab) return caret - 1;
  if (caret < items.length && items[caret] instanceof Tab) return caret;
  return -1;
}

/// The range `[start, end[` of the line which contains the item at `index`: the items between two
/// line or page breaks (the break which ends the line is not included).
export function lineBounds(items: readonly TopItem[], index: number): [number, number] {
  let start = index;
  while (start > 0 && !items[start - 1]!.isBreak()) start--;
  let end = index;
  while (end < items.length && !items[end]!.isBreak()) end++;
  return [start, end];
}

/// What a tab command changes: the items `[start, end[` are replaced by `items`.
export interface TabLineEdit {
  readonly start: number;
  readonly end: number;
  readonly items: readonly TopItem[];
}

/// Applies a change to the line of the tab near the cursor. Returns the edit, or `undefined` if
/// there is no tab near the cursor or if the change leaves the line as it is.
export function editTabLine(
  items: readonly TopItem[],
  caret: number,
  mark: number | null,
  change: TabLineChange,
): TabLineEdit | undefined {
  const tabIndex = tabNearCaret(items, caret, mark);
  if (tabIndex < 0) return undefined;
  const [start, end] = lineBounds(items, tabIndex);
  const changed = change(items.slice(start, end), tabIndex - start);
  return changed === undefined ? undefined : { start, end, items: changed };
}

/// Moves the tab to the next stop (`delta` 1) or the previous one (-1); see [TabLine.moveTab].
export function moveTabChange(delta: number): TabLineChange {
  return (line, position) => TabLine.moveTab(line, position, delta);
}
