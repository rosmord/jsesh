/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// The editing state machine: no DOM, no drawing. It follows the behaviour of
/// JSesh's editor (`JMDCEditorWorkflow`) for typing, but works at the level of
/// quadrats only: the cursor stands between top items.
///
/// A view (see `EditorView`, or your own) shows the session's text and
/// forwards keys and mouse positions to it. Groups (stacks, ligatures,
/// cartouches) are built by writing the items in MdC and reading them back.

import {
  Cadrat,
  DEFAULT_DOCUMENT_PREFERENCES,
  HieroglyphicText,
  parseMdc,
  toMdc,
  writeMdcDocument,
  type StyleChanges,
  type TopItem,
} from "@jsesh/core";

/// What the session needs to know about a key press. A DOM `KeyboardEvent` fits.
export interface KeyInput {
  key: string;
  ctrlKey?: boolean;
  metaKey?: boolean;
  shiftKey?: boolean;
  altKey?: boolean;
}

/// A snapshot of the session, for what is shown around the editor.
export interface EditorState {
  /// The text, as MdC.
  readonly mdc: string;
  /// The code being typed.
  readonly code: string;
  /// The separator typed after the previous sign (`" "` for none).
  readonly separator: string;
  /// The cursor: the number of top items before it.
  readonly caret: number;
  /// The number of top items.
  readonly length: number;
  /// The number of selected top items.
  readonly selection: number;
  /// The selected range of top items: `[selectionStart, selectionEnd[`. Both are `caret` if nothing is selected.
  readonly selectionStart: number;
  readonly selectionEnd: number;
  /// The last error (an unreadable code...), if any.
  readonly error: string | undefined;
  readonly canUndo: boolean;
  readonly canRedo: boolean;
}

/// Tells what a change affected.
export interface EditorChange {
  /// True if the text must be laid out again. If false, only the cursor, the selection or the typed code changed.
  readonly layout: boolean;
}

/// Proposes the texts (in MdC) that can be inserted for a code typed by the
/// user, the preferred one first: see `EditorSession.possibilities`.
export type PossibilityProvider = (code: string) => readonly string[];

/// What is needed to replace the last inserted text with the next possibility.
interface Retry {
  readonly candidates: readonly string[];
  readonly index: number;
  /// The separator which grouped the sign with the previous quadrat.
  readonly separator: string;
  /// The range of top items the insertion replaced.
  readonly start: number;
  readonly end: number;
}

/// Style options which concern the editing (`textDirection`, `textOrientation`...).
export type EditorOptions = NonNullable<StyleChanges["options"]>;

interface Snapshot {
  text: HieroglyphicText;
  caret: number;
}

const MAX_UNDO = 200;

export class EditorSession {
  private _text = new HieroglyphicText();
  private _caret = 0;
  private _mark: number | null = null;
  private _code = "";
  private _separator = " ";
  private _error: string | undefined;
  private _options: EditorOptions = {};
  private readonly undoStack: Snapshot[] = [];
  private readonly redoStack: Snapshot[] = [];
  private readonly listeners = new Set<(change: EditorChange) => void>();
  private cachedState: EditorState | undefined;
  private retry: Retry | undefined;

  /// Tells which signs a code may stand for (`PossibilityProvider`). By
  /// default, only the code itself. With `SignDatabase.getPossibilityListFor`,
  /// typing `i`, then a space, inserts the first sign with the value `i`; a
  /// second space replaces it with the next one, and so on, as in JSesh.
  possibilities: PossibilityProvider = (code) => [code];

  // ---- Observing

  /// Calls `listener` after each change. Returns a function which removes it.
  /// Together with `state`, it fits `useSyncExternalStore` (React) and similar APIs.
  subscribe(listener: (change: EditorChange) => void): () => void {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  /// The current state. The same object is returned until the next change.
  get state(): EditorState {
    if (this.cachedState === undefined) {
      const [start, end] = this.selectionRange();
      this.cachedState = {
        mdc: toMdc(this._text),
        code: this._code,
        separator: this._separator,
        caret: this._caret,
        length: this._text.topItems.length,
        selection: end - start,
        selectionStart: start,
        selectionEnd: end,
        error: this._error,
        canUndo: this.undoStack.length > 0,
        canRedo: this.redoStack.length > 0,
      };
    }
    return this.cachedState;
  }

  /// The text being edited. Don't change it directly: use the session's commands, so that views are told.
  get text(): HieroglyphicText {
    return this._text;
  }

  get options(): Readonly<EditorOptions> {
    return this._options;
  }

  get isVertical(): boolean {
    return this._options.textOrientation === "VERTICAL";
  }

  get isRightToLeft(): boolean {
    return this._options.textDirection === "RIGHT_TO_LEFT";
  }

  /// Changes the style options (`textDirection`, `textOrientation`...).
  setOptions(options: EditorOptions): void {
    this._options = { ...this._options, ...options };
    this.changed();
  }

  /// Replaces the whole text. Returns false if `mdc` can't be read.
  setMdc(mdc: string, { undoable = true } = {}): boolean {
    let text: HieroglyphicText;
    try {
      text = parseMdc(mdc);
    } catch {
      return false;
    }
    this.setText(text, { undoable });
    return true;
  }

  /// Replaces the whole text with a model (which the session then owns).
  setText(text: HieroglyphicText, { undoable = true } = {}): void {
    if (undoable) this.saveForUndo();
    this._text = text;
    this._caret = text.topItems.length;
    this._mark = null;
    this.clearCode();
    this.changed();
  }

  /// The text as a JSesh document (`.gly` file).
  document(): string {
    return writeMdcDocument({ preferences: DEFAULT_DOCUMENT_PREFERENCES, text: this._text });
  }

  // ---- Commands

  /// Inserts a sign, as if its code had been typed and followed by a space.
  insertSign(code: string): void {
    if (this._code !== "") this.acceptSeparator(this._separator);
    this._code = code;
    this.accept(" ", { propose: false, cycle: false });
  }

  /// Inserts MdC text at the cursor (replacing the selection).
  insertMdc(mdc: string): void {
    if (this._code !== "") this.acceptSeparator(" ");
    const items = this.read(mdc);
    if (items === undefined) return;
    this.saveForUndo();
    this.replaceSelection(items);
    this.clearCode();
    this.changed();
  }

  /// Groups the selection with `separator` (`:`, `*` or `&`); without a
  /// selection, the two quadrats before the cursor.
  group(separator: string): void {
    if (this._code !== "") this.acceptSeparator(" ");
    const [start, end] = this._mark === null ? [Math.max(0, this._caret - 2), this._caret] : this.selectionRange();
    const group = this.buildGroup(this._text.topItems.slice(start, end), separator);
    if (group === undefined) return;
    this.saveForUndo();
    this.replaceRange(start, end, group);
    this.changed();
  }

  /// Puts the selection (or the quadrat before the cursor) in a cartouche.
  cartouche(): void {
    if (this._code !== "") this.acceptSeparator(" ");
    const [start, end] = this.selectionOrPrevious();
    if (start === end) return;
    const items = this.read("<-" + this._text.topItems.slice(start, end).map(itemMdc).join("-") + "->");
    if (items === undefined) return;
    this.saveForUndo();
    this.replaceRange(start, end, items);
    this.changed();
  }

  /// Writes the selection (or the quadrat before the cursor) in red, or back
  /// in black if its first item is red.
  toggleRed(): void {
    const [start, end] = this.selectionOrPrevious();
    if (start === end) return;
    this.saveForUndo();
    const items = this._text.topItems.slice(start, end);
    const red = !items[0].isRed;
    for (const item of items) item.state.isRed = red;
    this.changed();
  }

  undo(): void {
    this.swapWith(this.undoStack, this.redoStack);
  }

  redo(): void {
    this.swapWith(this.redoStack, this.undoStack);
  }

  /// Notifies the listeners again (to show the state in an MdC field, for instance).
  refresh(): void {
    this.changed({ layout: false, keepRetry: true });
  }

  // ---- Typing, as in JSesh

  /// A separator completes the code being typed: the sign is inserted, and
  /// grouped with the previous quadrat according to the *previous*
  /// separator. Without a code, the previous separator groups the two
  /// quadrats before the cursor. Then `separator` becomes the current one.
  acceptSeparator(separator: string): void {
    this.accept(separator, { propose: true, cycle: separator === " " });
  }

  /// `acceptSeparator`. With `propose`, the sign is the first possibility for the code (otherwise, the code itself);
  /// with `cycle`, it can then be replaced by the next ones.
  private accept(separator: string, { propose, cycle }: { propose: boolean; cycle: boolean }): void {
    let retry: Retry | undefined;
    if (this._code !== "") {
      const candidates = propose ? this.possibilities(this._code) : [this._code];
      const items = this.read(candidates[0] ?? this._code);
      if (items === undefined) return;
      this.saveForUndo();
      const [start, end] = this.selectionRange();
      this.insertAndGroup(start, end, items, this._separator);
      if (cycle && candidates.length > 1) {
        retry = { candidates, index: 0, separator: this._separator, start, end };
      }
      this._code = "";
    } else if (this._separator !== " ") {
      this.saveForUndo();
      this.groupBeforeCaret(this._separator);
    }
    this._separator = separator;
    this.changed();
    this.retry = retry;
  }

  /// Replaces the sign inserted by the last separator with the next
  /// possibility for the code which was typed (`i` can be A1, A26, M2...), and
  /// goes back to the first one after the last. Returns false if there is
  /// nothing to change: the last action wasn't the insertion of a sign.
  nextPossibility(): boolean {
    const retry = this.retry;
    const previous = this.undoStack.at(-1);
    if (retry === undefined || previous === undefined) return false;
    const index = (retry.index + 1) % retry.candidates.length;
    const items = this.read(retry.candidates[index]);
    if (items === undefined) return true;
    // Go back to the text before the insertion: it is still on top of the undo stack.
    this._text = previous.text.deepCopy();
    this.insertAndGroup(retry.start, retry.end, items, retry.separator);
    this.changed();
    this.retry = { ...retry, index, end: retry.start + items.length };
    return true;
  }

  /// Types one character of a sign code.
  typeCharacter(character: string): void {
    this._code += character;
    this._error = undefined;
    this.changed({ layout: false });
  }

  /// Forgets the code being typed, and the selection.
  escape(): void {
    this.clearCode();
    this._mark = null;
    this.changed({ layout: false });
  }

  /// Erases, according to the context: the last letter of the code, the
  /// separator, the selection, or the quadrat before the cursor.
  backspace(): void {
    if (this._code !== "") {
      this._code = this._code.slice(0, -1);
      this.changed({ layout: false });
      return;
    }
    if (this._separator !== " ") {
      this._separator = " ";
      this.changed({ layout: false });
      return;
    }
    if (this._mark !== null) {
      this.saveForUndo();
      this.replaceSelection([]);
    } else if (this._caret > 0) {
      this.saveForUndo();
      this.replaceRange(this._caret - 1, this._caret, []);
    } else {
      return;
    }
    this.changed();
  }

  /// Erases the selection, or the quadrat after the cursor.
  delete(): void {
    if (this._code !== "" || this._separator !== " ") return;
    if (this._mark !== null) {
      this.saveForUndo();
      this.replaceSelection([]);
    } else if (this._caret < this._text.topItems.length) {
      this.saveForUndo();
      this._text.removeItems(this._caret, this._caret + 1);
    } else {
      return;
    }
    this.changed();
  }

  /// Handles a key press, as the editor would. Returns true if the key was used (the caller should then
  /// prevent the browser's default action).
  handleKey(e: KeyInput): boolean {
    const key = e.key;
    if (e.ctrlKey || e.metaKey) {
      const lower = key.toLowerCase();
      if (lower === "z") {
        if (e.shiftKey) this.redo();
        else this.undo();
      } else if (lower === "y") this.redo();
      else if (lower === "a") this.selectAll();
      else return false; // Leave copy, reload... to the browser.
      return true;
    }
    if (this.moveKey(key, e.shiftKey === true)) return true;
    switch (key) {
      case "Backspace":
        this.backspace();
        return true;
      case "Delete":
        this.delete();
        return true;
      case "Escape":
        this.escape();
        return true;
      case " ":
        if (!(this._code === "" && this._separator === " " && this.nextPossibility())) this.acceptSeparator(" ");
        return true;
      case "-":
      case "Enter":
        // As a space, but a following space won't change the sign.
        this.accept(" ", { propose: true, cycle: false });
        return true;
      case ":":
      case "*":
      case "&":
        this.acceptSeparator(key);
        return true;
      case "!":
        this.insertMdc("!");
        return true;
    }
    if (key.length === 1 && !e.altKey) {
      this.typeCharacter(key);
      return true;
    }
    return false;
  }

  // ---- Moving

  /// Handles the arrows, Home and End. Returns false for other keys.
  moveKey(key: string, extend: boolean): boolean {
    const rtl = this.isRightToLeft;
    const horizontal = { ArrowRight: rtl ? -1 : 1, ArrowLeft: rtl ? 1 : -1 };
    const steps: Record<string, number> = this.isVertical ? { ArrowDown: 1, ArrowUp: -1, ...horizontal } : horizontal;
    let target: number;
    if (key in steps) target = this._caret + steps[key];
    else if (key === "Home") target = 0;
    else if (key === "End") target = this._text.topItems.length;
    else return false;
    this.moveCaret(target, extend);
    return true;
  }

  /// Moves the cursor between top items; with `extend`, the selection grows.
  moveCaret(position: number, extend = false): void {
    if (this._code !== "") this.acceptSeparator(" ");
    this._separator = " ";
    if (extend && this._mark === null) this._mark = this._caret;
    if (!extend) this._mark = null;
    this._caret = Math.max(0, Math.min(position, this._text.topItems.length));
    if (this._mark === this._caret) this._mark = null;
    this.changed({ layout: false });
  }

  selectAll(): void {
    this._mark = 0;
    this._caret = this._text.topItems.length;
    this.changed({ layout: false });
  }

  // ---- Internals

  /// Reads MdC text as a list of top items; records an error if it can't.
  private read(mdc: string): TopItem[] | undefined {
    try {
      const items = [...parseMdc(mdc).topItems];
      this._error = undefined;
      return items;
    } catch (error) {
      this._error = `Can't read “${mdc}”: ${error instanceof Error ? error.message : String(error)}`;
      this.changed({ layout: false });
      return undefined;
    }
  }

  /// A group of quadrats, built by writing them in MdC with the separator.
  /// Returns undefined if they can't be grouped.
  private buildGroup(items: readonly TopItem[], separator: string): TopItem[] | undefined {
    if (items.length < 2 || !items.every((i) => i instanceof Cadrat)) return undefined;
    let parts = items.map(itemMdc);
    if (separator === "*") parts = parts.map((p) => (p.includes(":") ? `(${p})` : p));
    // Only simple signs make ligatures.
    if (separator === "&" && !parts.every((p) => /^[A-Za-z0-9]+$/.test(p))) return undefined;
    const group = this.read(parts.join(separator));
    if (group !== undefined && items[0].isRed) for (const g of group) g.state.isRed = true;
    return group;
  }

  /// Replaces the range with `items`, grouped with the previous quadrat according to `separator`.
  private insertAndGroup(start: number, end: number, items: readonly TopItem[], separator: string): void {
    this.replaceRange(start, end, items);
    if (separator !== " " && items.length === 1) this.groupBeforeCaret(separator);
  }

  private groupBeforeCaret(separator: string): void {
    const start = this._caret - 2;
    if (start < 0) return;
    const group = this.buildGroup(this._text.topItems.slice(start, this._caret), separator);
    if (group !== undefined) this.replaceRange(start, this._caret, group);
  }

  private replaceRange(start: number, end: number, items: readonly TopItem[]): void {
    this._text.removeItems(start, end);
    this._text.insertItemsAt(start, items);
    this._caret = start + items.length;
    this._mark = null;
  }

  private replaceSelection(items: readonly TopItem[]): void {
    const [start, end] = this.selectionRange();
    this.replaceRange(start, end, items);
  }

  private selectionRange(): [number, number] {
    if (this._mark === null) return [this._caret, this._caret];
    return [Math.min(this._caret, this._mark), Math.max(this._caret, this._mark)];
  }

  private selectionOrPrevious(): [number, number] {
    return this._mark === null ? [Math.max(0, this._caret - 1), this._caret] : this.selectionRange();
  }

  private clearCode(): void {
    this._code = "";
    this._separator = " ";
  }

  private saveForUndo(): void {
    this.undoStack.push(this.snapshot());
    if (this.undoStack.length > MAX_UNDO) this.undoStack.shift();
    this.redoStack.length = 0;
  }

  private snapshot(): Snapshot {
    return { text: this._text.deepCopy(), caret: this._caret };
  }

  private swapWith(from: Snapshot[], to: Snapshot[]): void {
    const previous = from.pop();
    if (previous === undefined) return;
    to.push(this.snapshot());
    this._text = previous.text;
    this._caret = previous.caret;
    this._mark = null;
    this.clearCode();
    this.changed();
  }

  /// Tells the listeners. Any change but a `keepRetry` one ends the possibility of cycling through the signs.
  private changed({ layout = true, keepRetry = false } = {}): void {
    this.cachedState = undefined;
    if (!keepRetry) this.retry = undefined;
    for (const listener of [...this.listeners]) listener({ layout });
  }
}

/// The MdC of one top item, without its colour (kept apart, in its state).
function itemMdc(item: TopItem): string {
  return toMdc(new HieroglyphicText([item.deepCopy()]))
    .replace(/\$[rb]-?/g, "")
    .replace(/-+$/, "")
    .trim();
}
