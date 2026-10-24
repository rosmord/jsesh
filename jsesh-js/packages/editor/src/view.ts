/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Shows an `EditorSession` in a DOM element, and sends it the keys and the
/// mouse clicks. `<jsesh-editor>` is made of one `EditorSession` and one
/// `EditorView`; use these directly to put the editor in your own markup (a
/// Vue or React component, for instance, can create the view in its "mounted"
/// hook).

import { drawLaidOutText, type LaidOutText, type StyleChanges } from "@jsesh/core";
import { layOutInPage } from "@jsesh/elements";
import { caretArea, itemAreas, positionAt, type Area } from "./geometry.js";
import type { EditorSession } from "./session.js";

/// The editor's own margins around the text, in points.
const MARGIN = 6;

let views = 0;

export interface EditorViewOptions {
  /// Height of a quadrat, in pixels (45 by default).
  cadratHeight?: number;
  /// Changes to the page's style, for this editor.
  style?: StyleChanges;
  /// URL of the glyph data, for this editor only.
  glyphsUrl?: string;
  /// Called when the text can't be drawn (the glyph data can't be loaded...). By default, the error is logged.
  onError?: (error: unknown) => void;
}

/// Styles for the cursor and the selection, which the view adds to its
/// surface as `div`s of class `caret` and `selection-box`. The surface
/// should be focusable (`tabindex="0"`); the view makes it `position: relative`
/// if it is not positioned.
export const EDITOR_VIEW_CSS = `
.caret, .selection-box { position: absolute; pointer-events: none; }
.caret { background: var(--jsesh-caret-color, #c00); }
.selection-box { background: var(--jsesh-selection-color, rgb(74 127 212 / 25%)); }
.surface:focus .caret { animation: jsesh-blink 1s step-end infinite; }
.surface:not(:focus) .caret { background: var(--jsesh-inactive-caret-color, #999); }
@keyframes jsesh-blink { 50% { opacity: 0; } }
`;

export class EditorView {
  private _cadratHeight: number;
  private style: StyleChanges | undefined;
  private readonly glyphsUrl: string | undefined;
  private readonly onError: (error: unknown) => void;
  private readonly idPrefix = `jsesh-editor${views++}`;
  private laidOut: LaidOutText | undefined;
  private _areas: Area[] = [];
  private generation = 0;
  private readonly unsubscribe: () => void;
  private readonly onKeyDown = (e: KeyboardEvent): void => {
    if (this.session.handleKey(e)) e.preventDefault();
  };
  private readonly onMouseDown = (e: MouseEvent): void => {
    // Let the browser give the focus to the surface, but no text selection.
    e.preventDefault();
    this.surface.focus();
    const box = this.surface.getBoundingClientRect();
    const x = e.clientX - box.left + this.surface.scrollLeft;
    const y = e.clientY - box.top + this.surface.scrollTop;
    this.session.moveCaret(positionAt(this._areas, x, y, this.flow()), e.shiftKey);
  };

  /// @param session what is edited.
  /// @param surface a focusable element (`tabindex="0"`), where the text is shown. Its content is replaced.
  constructor(
    readonly session: EditorSession,
    readonly surface: HTMLElement,
    options: EditorViewOptions = {},
  ) {
    this._cadratHeight = options.cadratHeight ?? 45;
    this.style = options.style;
    this.glyphsUrl = options.glyphsUrl;
    this.onError = options.onError ?? ((error) => console.error("jsesh editor:", error));
    if (getComputedStyle(surface).position === "static") surface.style.position = "relative";
    surface.addEventListener("keydown", this.onKeyDown);
    surface.addEventListener("mousedown", this.onMouseDown);
    this.unsubscribe = session.subscribe(({ layout }) => {
      if (layout) this.renderSafely();
      else this.drawCaret();
    });
    // Not at once: the page may call `configureJSesh` right after importing this module, as for <jsesh-text>.
    queueMicrotask(() => this.renderSafely());
  }

  /// Stops listening to the surface and to the session.
  destroy(): void {
    this.generation++;
    this.unsubscribe();
    this.surface.removeEventListener("keydown", this.onKeyDown);
    this.surface.removeEventListener("mousedown", this.onMouseDown);
  }

  get cadratHeight(): number {
    return this._cadratHeight;
  }

  set cadratHeight(height: number) {
    this._cadratHeight = height;
    this.renderSafely();
  }

  /// Changes to the page's style, for this editor (`session.setOptions` is for direction and orientation).
  setStyle(style: StyleChanges | undefined): void {
    this.style = style;
    this.renderSafely();
  }

  /// The area of each top item, in pixels, after the last layout.
  get areas(): readonly Area[] {
    return this._areas;
  }

  /// Gives the keyboard to the editor.
  focus(): void {
    this.surface.focus();
  }

  private renderSafely(): void {
    this.render().catch((error: unknown) => this.onError(error));
  }

  /// Lays out the text, draws it, and finds the area of each top item.
  async render(): Promise<void> {
    const generation = ++this.generation;
    const options = this.session.options;
    const laidOut = await layOutInPage(this.session.text, {
      style: {
        ...this.style,
        geometry: { leftMargin: MARGIN, topMargin: MARGIN, ...this.style?.geometry },
        options: { ...this.style?.options, ...options },
      },
      glyphsUrl: this.glyphsUrl,
    });
    if (generation !== this.generation) return;
    this.laidOut = laidOut;
    this.surface.innerHTML = drawLaidOutText(laidOut, { cadratHeight: this._cadratHeight, idPrefix: this.idPrefix });
    this._areas = itemAreas(laidOut, this._cadratHeight);
    this.drawCaret();
  }

  private flow() {
    return { vertical: this.session.isVertical, rightToLeft: this.session.isRightToLeft };
  }

  /// Draws the cursor and the selection over the picture.
  drawCaret(): void {
    if (this.laidOut === undefined) return;
    for (const e of this.surface.querySelectorAll(".caret, .selection-box")) e.remove();
    const state = this.session.state;
    for (let i = state.selectionStart; i < state.selectionEnd && i < this._areas.length; i++) {
      this.addBox("selection-box", this._areas[i]);
    }
    const scale = this._cadratHeight / this.laidOut.style.geometry.standardSignHeight;
    const caret = this.addBox("caret", caretArea(this._areas, state.caret, this.flow(), this._cadratHeight, MARGIN * scale));
    caret.scrollIntoView?.({ block: "nearest", inline: "nearest" });
  }

  private addBox(className: string, { x, y, width, height }: Area): HTMLElement {
    const box = document.createElement("div");
    box.className = className;
    Object.assign(box.style, { left: `${x}px`, top: `${y}px`, width: `${width}px`, height: `${height}px` });
    this.surface.append(box);
    return box;
  }
}
