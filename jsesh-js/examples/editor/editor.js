/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// A small hieroglyphic editor, built on the public API of `@jsesh/core` and
/// `@jsesh/elements`. It follows the behaviour of JSesh's editor
/// (`JMDCEditorWorkflow`) for typing, but works at the level of quadrats only:
/// the cursor stands between top items.
///
/// The text is a `HieroglyphicText`. After each change, it is laid out with
/// `layOutInPage`; the subviews of the root view are the views of the top
/// items, in order, which gives the place of the cursor and of the selection,
/// and lets clicks be located in the text. Groups (stacks, ligatures,
/// cartouches) are built by writing the items in MdC and reading them back.

import { Cadrat, drawLaidOutText, HieroglyphicText, parseMdc, toMdc, writeMdcDocument, DEFAULT_DOCUMENT_PREFERENCES } from "@jsesh/core";
import { layOutInPage } from "@jsesh/elements";

/// The editor's own margins around the text, in points.
const MARGIN = 6;

export class HieroglyphicEditor {
  /// @param {HTMLElement} surface a focusable element (`tabindex="0"`), where the text is shown.
  /// @param {{ onChange?: (state: object) => void }} options `onChange` is called after each change of
  ///        the text, the cursor or the code being typed.
  constructor(surface, options = {}) {
    this.surface = surface;
    this.onChange = options.onChange ?? (() => {});
    this.text = new HieroglyphicText();
    /// The cursor: the number of top items before it.
    this.caret = 0;
    /// The other end of the selection, or null.
    this.mark = null;
    /// The code being typed, and the separator which will group it with the
    /// previous quadrat (' ' for none), as in JSesh.
    this.code = "";
    this.separator = " ";
    this.error = undefined;
    this.undoStack = [];
    this.redoStack = [];
    this._cadratHeight = 45;
    this.styleOptions = {};
    /// The last layout, and the area of each top item, in pixels.
    this.laidOut = undefined;
    this.areas = [];
    this.generation = 0;

    surface.addEventListener("keydown", (e) => this.keyDown(e));
    surface.addEventListener("mousedown", (e) => this.mouseDown(e));
  }

  // ---- Public API

  /// The state shown around the editor.
  get state() {
    const [start, end] = this.selectionRange();
    return {
      mdc: toMdc(this.text),
      code: this.code,
      separator: this.separator,
      caret: this.caret,
      length: this.text.topItems.length,
      selection: end - start,
      error: this.error,
    };
  }

  get cadratHeight() {
    return this._cadratHeight;
  }

  set cadratHeight(h) {
    this._cadratHeight = h;
    this.render();
  }

  /// Changes the style options (`textDirection`, `textOrientation`...).
  setOptions(options) {
    this.styleOptions = { ...this.styleOptions, ...options };
    this.render();
  }

  focus() {
    this.surface.focus();
  }

  /// Replaces the whole text. Returns false if `mdc` can't be read.
  setMdc(mdc, { undoable = true } = {}) {
    let text;
    try {
      text = parseMdc(mdc);
    } catch {
      return false;
    }
    if (undoable) this.saveForUndo();
    this.text = text;
    this.caret = text.topItems.length;
    this.mark = null;
    this.clearCode();
    this.changed();
    return true;
  }

  /// The text as a JSesh document (`.gly` file).
  document() {
    return writeMdcDocument({ preferences: DEFAULT_DOCUMENT_PREFERENCES, text: this.text });
  }

  /// Inserts a sign, as if its code had been typed and followed by a space.
  insertSign(code) {
    if (this.code !== "") this.acceptSeparator(this.separator);
    this.code = code;
    this.acceptSeparator(" ");
  }

  /// Inserts MdC text at the cursor (replacing the selection).
  insertMdc(mdc) {
    if (this.code !== "") this.acceptSeparator(" ");
    const items = this.read(mdc);
    if (items === undefined) return;
    this.saveForUndo();
    this.replaceSelection(items);
    this.clearCode();
    this.changed();
  }

  /// Groups the selection with `separator` (`:`, `*` or `&`); without a
  /// selection, the two quadrats before the cursor.
  group(separator) {
    if (this.code !== "") this.acceptSeparator(" ");
    const [start, end] = this.mark === null ? [Math.max(0, this.caret - 2), this.caret] : this.selectionRange();
    const group = this.buildGroup(this.text.topItems.slice(start, end), separator);
    if (group === undefined) return;
    this.saveForUndo();
    this.replaceRange(start, end, group);
    this.changed();
  }

  /// Puts the selection (or the quadrat before the cursor) in a cartouche.
  cartouche() {
    if (this.code !== "") this.acceptSeparator(" ");
    const [start, end] = this.selectionOrPrevious();
    if (start === end) return;
    const items = this.read("<-" + this.text.topItems.slice(start, end).map(itemMdc).join("-") + "->");
    if (items === undefined) return;
    this.saveForUndo();
    this.replaceRange(start, end, items);
    this.changed();
  }

  /// Writes the selection (or the quadrat before the cursor) in red, or back
  /// in black if its first item is red.
  toggleRed() {
    const [start, end] = this.selectionOrPrevious();
    if (start === end) return;
    this.saveForUndo();
    const items = this.text.topItems.slice(start, end);
    const red = !items[0].isRed;
    for (const item of items) item.state.isRed = red;
    this.changed();
  }

  undo() {
    this.swapWith(this.undoStack, this.redoStack);
  }

  redo() {
    this.swapWith(this.redoStack, this.undoStack);
  }

  /// Shows the state again (the MdC field, for instance).
  refresh() {
    this.onChange(this.state);
  }

  // ---- Typing, as in JSesh

  /// A separator completes the code being typed: the sign is inserted, and
  /// grouped with the previous quadrat according to the *previous*
  /// separator. Without a code, the previous separator groups the two
  /// quadrats before the cursor. Then `separator` becomes the current one.
  acceptSeparator(separator) {
    if (this.code !== "") {
      const items = this.read(this.code);
      if (items === undefined) return;
      this.saveForUndo();
      this.replaceSelection(items);
      if (this.separator !== " " && items.length === 1) this.groupBeforeCaret(this.separator);
      this.code = "";
    } else if (this.separator !== " ") {
      this.saveForUndo();
      this.groupBeforeCaret(this.separator);
    }
    this.separator = separator;
    this.changed();
  }

  groupBeforeCaret(separator) {
    const start = this.caret - 2;
    if (start < 0) return;
    const group = this.buildGroup(this.text.topItems.slice(start, this.caret), separator);
    if (group !== undefined) this.replaceRange(start, this.caret, group);
  }

  /// Erases, according to the context: the last letter of the code, the
  /// separator, the selection, or the quadrat before the cursor.
  backspace() {
    if (this.code !== "") {
      this.code = this.code.slice(0, -1);
    } else if (this.separator !== " ") {
      this.separator = " ";
    } else if (this.mark !== null) {
      this.saveForUndo();
      this.replaceSelection([]);
    } else if (this.caret > 0) {
      this.saveForUndo();
      this.replaceRange(this.caret - 1, this.caret, []);
    }
    this.changed();
  }

  /// Erases the selection, or the quadrat after the cursor.
  delete() {
    if (this.code !== "" || this.separator !== " ") return;
    if (this.mark !== null) {
      this.saveForUndo();
      this.replaceSelection([]);
    } else if (this.caret < this.text.topItems.length) {
      this.saveForUndo();
      this.text.removeItems(this.caret, this.caret + 1);
    }
    this.changed();
  }

  keyDown(e) {
    const command = e.ctrlKey || e.metaKey;
    const key = e.key;
    if (command) {
      const lower = key.toLowerCase();
      if (lower === "z") this[e.shiftKey ? "redo" : "undo"]();
      else if (lower === "y") this.redo();
      else if (lower === "a") this.selectAll();
      else return; // Leave copy, reload... to the browser.
    } else if (this.moveKey(key, e.shiftKey)) {
      // Done.
    } else if (key === "Backspace") {
      this.backspace();
    } else if (key === "Delete") {
      this.delete();
    } else if (key === "Escape") {
      this.clearCode();
      this.mark = null;
      this.changed();
    } else if (key === "-" || key === " " || key === "Enter") {
      this.acceptSeparator(" ");
    } else if (key === ":" || key === "*" || key === "&") {
      this.acceptSeparator(key);
    } else if (key === "!") {
      this.insertMdc("!");
    } else if (key.length === 1 && !e.altKey) {
      this.code += key;
      this.error = undefined;
      this.changed();
    } else {
      return;
    }
    e.preventDefault();
  }

  /// Handles the arrows, Home and End. Returns false for other keys.
  moveKey(key, extend) {
    const vertical = this.styleOptions.textOrientation === "VERTICAL";
    const rtl = this.styleOptions.textDirection === "RIGHT_TO_LEFT";
    const steps = vertical
      ? { ArrowDown: 1, ArrowUp: -1, ArrowRight: rtl ? -1 : 1, ArrowLeft: rtl ? 1 : -1 }
      : { ArrowRight: rtl ? -1 : 1, ArrowLeft: rtl ? 1 : -1 };
    let target;
    if (key in steps) target = this.caret + steps[key];
    else if (key === "Home") target = 0;
    else if (key === "End") target = this.text.topItems.length;
    else return false;
    this.moveCaret(target, extend);
    return true;
  }

  moveCaret(position, extend) {
    if (this.code !== "") this.acceptSeparator(" ");
    this.separator = " ";
    if (extend && this.mark === null) this.mark = this.caret;
    if (!extend) this.mark = null;
    this.caret = Math.max(0, Math.min(position, this.text.topItems.length));
    if (this.mark === this.caret) this.mark = null;
    this.changed({ layout: false });
  }

  selectAll() {
    this.mark = 0;
    this.caret = this.text.topItems.length;
    this.changed({ layout: false });
  }

  mouseDown(e) {
    // Let the browser give the focus to the surface, but no text selection.
    e.preventDefault();
    this.surface.focus();
    const box = this.surface.getBoundingClientRect();
    const x = e.clientX - box.left + this.surface.scrollLeft;
    const y = e.clientY - box.top + this.surface.scrollTop;
    this.moveCaret(this.positionAt(x, y), e.shiftKey);
  }

  /// The cursor position closest to a point of the surface: before or after
  /// the nearest quadrat.
  positionAt(x, y) {
    if (this.areas.length === 0) return 0;
    let best = 0;
    let bestDistance = Infinity;
    this.areas.forEach((a, i) => {
      const dx = Math.max(a.x - x, 0, x - (a.x + a.width));
      const dy = Math.max(a.y - y, 0, y - (a.y + a.height));
      // Being on the right line matters more than being near the quadrat.
      const distance = this.isVertical() ? dx * 4 + dy : dy * 4 + dx;
      if (distance < bestDistance) {
        bestDistance = distance;
        best = i;
      }
    });
    const a = this.areas[best];
    let after;
    if (this.isVertical()) after = y > a.y + a.height / 2;
    else if (this.isRightToLeft()) after = x < a.x + a.width / 2;
    else after = x > a.x + a.width / 2;
    return after ? best + 1 : best;
  }

  // ---- Model changes

  /// Reads MdC text as a list of top items; shows an error if it can't.
  read(mdc) {
    try {
      const items = [...parseMdc(mdc).topItems];
      this.error = undefined;
      return items;
    } catch (error) {
      this.error = `Can't read “${mdc}”: ${error.message ?? error}`;
      this.changed({ layout: false });
      return undefined;
    }
  }

  /// A group of quadrats, built by writing them in MdC with the separator.
  /// Returns undefined if they can't be grouped.
  buildGroup(items, separator) {
    if (items.length < 2 || !items.every((i) => i instanceof Cadrat)) return undefined;
    let parts = items.map(itemMdc);
    if (separator === "*") parts = parts.map((p) => (p.includes(":") ? `(${p})` : p));
    // Only simple signs make ligatures.
    if (separator === "&" && !parts.every((p) => /^[A-Za-z0-9]+$/.test(p))) return undefined;
    const group = this.read(parts.join(separator));
    if (group !== undefined && items[0].isRed) for (const g of group) g.state.isRed = true;
    return group;
  }

  replaceRange(start, end, items) {
    this.text.removeItems(start, end);
    this.text.insertItemsAt(start, items);
    this.caret = start + items.length;
    this.mark = null;
  }

  replaceSelection(items) {
    const [start, end] = this.selectionRange();
    this.replaceRange(start, end, items);
  }

  selectionRange() {
    if (this.mark === null) return [this.caret, this.caret];
    return [Math.min(this.caret, this.mark), Math.max(this.caret, this.mark)];
  }

  selectionOrPrevious() {
    return this.mark === null ? [Math.max(0, this.caret - 1), this.caret] : this.selectionRange();
  }

  clearCode() {
    this.code = "";
    this.separator = " ";
  }

  saveForUndo() {
    this.undoStack.push(this.snapshot());
    if (this.undoStack.length > 200) this.undoStack.shift();
    this.redoStack.length = 0;
  }

  snapshot() {
    return { text: this.text.deepCopy(), caret: this.caret };
  }

  swapWith(from, to) {
    const previous = from.pop();
    if (previous === undefined) return;
    to.push(this.snapshot());
    this.text = previous.text;
    this.caret = previous.caret;
    this.mark = null;
    this.clearCode();
    this.changed();
  }

  // ---- Display

  isVertical() {
    return this.styleOptions.textOrientation === "VERTICAL";
  }

  isRightToLeft() {
    return this.styleOptions.textDirection === "RIGHT_TO_LEFT";
  }

  changed({ layout = true } = {}) {
    if (layout) this.render();
    else this.drawCaret();
    this.onChange(this.state);
  }

  /// Lays out the text, draws it, and finds the area of each top item.
  async render() {
    const generation = ++this.generation;
    const laidOut = await layOutInPage(this.text, {
      style: { geometry: { leftMargin: MARGIN, topMargin: MARGIN }, options: this.styleOptions },
    });
    if (generation !== this.generation) return;
    this.laidOut = laidOut;
    const scale = this._cadratHeight / laidOut.style.geometry.standardSignHeight;
    this.surface.innerHTML = drawLaidOutText(laidOut, { cadratHeight: this._cadratHeight, idPrefix: "editor" });
    const root = laidOut.view;
    this.areas = root.subviews.map((_, i) => {
      const b = root.getSubViewBounds(i);
      return {
        x: b.x * root.xScale * scale,
        y: b.y * root.yScale * scale,
        width: b.width * root.xScale * scale,
        height: b.height * root.yScale * scale,
      };
    });
    this.drawCaret();
  }

  /// Draws the cursor and the selection over the picture.
  drawCaret() {
    if (this.laidOut === undefined) return;
    for (const e of this.surface.querySelectorAll(".caret, .selection-box")) e.remove();
    const [start, end] = this.selectionRange();
    for (let i = start; i < end && i < this.areas.length; i++) this.addBox("selection-box", this.areas[i]);

    const scale = this._cadratHeight / this.laidOut.style.geometry.standardSignHeight;
    const size = this._cadratHeight;
    const thickness = 2;
    // The cursor is drawn before the item which follows it, or after the last one.
    const next = this.areas[this.caret];
    const last = this.areas[this.caret - 1];
    let box;
    if (next === undefined && last === undefined) {
      const m = MARGIN * scale;
      box = this.isVertical() ? { x: m, y: m, width: size, height: thickness } : { x: m, y: m, width: thickness, height: size };
    } else if (this.isVertical()) {
      const a = next ?? last;
      box = { x: a.x, y: next !== undefined ? a.y - thickness : a.y + a.height, width: Math.max(a.width, size / 2), height: thickness };
    } else {
      const a = next ?? last;
      const startSide = next !== undefined;
      // In right-to-left lines, the start of an item is its right side.
      const x = startSide !== this.isRightToLeft() ? a.x - thickness : a.x + a.width;
      box = { x, y: a.y, width: thickness, height: Math.max(a.height, size / 2) };
    }
    const caret = this.addBox("caret", box);
    caret.scrollIntoView?.({ block: "nearest", inline: "nearest" });
  }

  addBox(className, { x, y, width, height }) {
    const box = document.createElement("div");
    box.className = className;
    Object.assign(box.style, { left: `${x}px`, top: `${y}px`, width: `${width}px`, height: `${height}px` });
    this.surface.append(box);
    return box;
  }
}

/// The MdC of one top item, without its colour (kept apart, in its state).
function itemMdc(item) {
  return toMdc(new HieroglyphicText([item.deepCopy()]))
    .replace(/\$[rb]-?/g, "")
    .replace(/-+$/, "")
    .trim();
}
