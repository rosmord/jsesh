/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// `<jsesh-editor>`: a hieroglyphic editor.
///
/// ```html
/// <jsesh-editor cadrat-height="45">i-mn:n-Htp:t*p</jsesh-editor>
/// ```
///
/// The initial text is the `mdc` attribute (or property), or else the
/// element's content. Click in the editor and type as in JSesh: a sign code,
/// then a separator (`-`, space or Enter for a new quadrat; `:`, `*`, `&` to
/// group the sign with the next one).
///
/// Attributes: `mdc`, `cadrat-height` (height of a quadrat in pixels, 45 by
/// default), `direction` (`ltr` or `rtl`), `orientation` (`horizontal` or
/// `vertical`), `glyphs-url`. The `jseshStyle` property takes any style change.
///
/// The `mdc` property reads and sets the text; setting it to what it already
/// is does nothing, so it can be bound to a framework's model (`v-model`...).
///
/// Events: `jsesh-change`, after each change of the text, the cursor or the
/// code being typed (its `detail` is the `EditorState`); `jsesh-error`, if the
/// text can't be drawn (glyph data not found...), with the error as `detail`.
///
/// Style: `--jsesh-caret-color`, `--jsesh-selection-color`,
/// `--jsesh-inactive-caret-color`, `--jsesh-editor-border`,
/// `--jsesh-editor-background`, `--jsesh-editor-min-height`; the part
/// `surface` (`jsesh-editor::part(surface)`) for everything else.
///
/// As in JSesh, typing a code then a space inserts the first sign for it (`i`:
/// A1), and typing another space replaces it with the next sign with the same
/// phonetic value (A26, M2...) or the same basic code (`A1`: A1, A1A...),
/// going back to the first after the last. This needs the sign database
/// (`sign-info.json`, loaded from the same place as the glyphs).
///
/// Commands (`insertSign`, `group`, `undo`...) are those of `EditorSession`,
/// which is available as the `session` property: use it to build your own
/// toolbar.

import type { StyleChanges } from "@jsesh/core";
import { resolveGlyphsUrl } from "@jsesh/elements";
import { loadSignDatabase } from "@jsesh/glyphs";
import { EDITOR_VIEW_CSS, EditorView } from "./view.js";
import { EditorSession, type EditorOptions, type EditorState } from "./session.js";

const STYLE = `
:host { display: block; }
.surface {
  position: relative;
  box-sizing: border-box;
  min-height: var(--jsesh-editor-min-height, 6rem);
  overflow: auto;
  cursor: text;
  line-height: 0;
  border: var(--jsesh-editor-border, 1px solid #aaa);
  border-radius: 4px;
  background: var(--jsesh-editor-background, white);
}
.surface:focus { outline: 2px solid #4a7fd4; outline-offset: 1px; }
.surface svg { display: block; }
${EDITOR_VIEW_CSS}
`;

export class JSeshEditorElement extends HTMLElement {
  static readonly observedAttributes = ["mdc", "cadrat-height", "direction", "orientation", "glyphs-url"];

  /// The editing state machine of this element.
  readonly session = new EditorSession();

  private readonly surface: HTMLElement;
  private view: EditorView | undefined;
  private _cadratHeight = 45;
  private _style: StyleChanges | undefined;
  private initialized = false;
  private unsubscribe: (() => void) | undefined;

  constructor() {
    super();
    const root = this.attachShadow({ mode: "open", delegatesFocus: true });
    root.innerHTML = `<style>${STYLE}</style>`;
    this.surface = document.createElement("div");
    this.surface.className = "surface";
    this.surface.setAttribute("part", "surface");
    this.surface.tabIndex = 0;
    this.surface.setAttribute("role", "textbox");
    this.surface.setAttribute("aria-multiline", "true");
    this.surface.setAttribute("aria-label", "Hieroglyphic text");
    root.append(this.surface);
  }

  // ---- Properties

  /// The MdC text being edited.
  get mdc(): string {
    return this.session.state.mdc;
  }

  set mdc(value: string) {
    if (value !== this.session.state.mdc) this.session.setMdc(value, { undoable: this.initialized });
  }

  /// The current state: text, cursor, selection, code being typed...
  get state(): EditorState {
    return this.session.state;
  }

  /// Height of a quadrat, in pixels.
  get cadratHeight(): number {
    return this._cadratHeight;
  }

  set cadratHeight(height: number) {
    this._cadratHeight = height;
    if (this.view !== undefined) this.view.cadratHeight = height;
  }

  /// Changes to the style, on top of the page's style and the attributes.
  get jseshStyle(): StyleChanges | undefined {
    return this._style;
  }

  set jseshStyle(style: StyleChanges | undefined) {
    this._style = style;
    this.view?.setStyle(style);
  }

  // ---- Commands (see `EditorSession`)

  override focus(): void {
    this.surface.focus();
  }

  insertSign(code: string): void {
    this.session.insertSign(code);
  }

  insertMdc(mdc: string): void {
    this.session.insertMdc(mdc);
  }

  group(separator: string): void {
    this.session.group(separator);
  }

  cartouche(): void {
    this.session.cartouche();
  }

  toggleRed(): void {
    this.session.toggleRed();
  }

  undo(): void {
    this.session.undo();
  }

  redo(): void {
    this.session.redo();
  }

  /// The text as a JSesh document (the content of a `.gly` file, with header).
  document(): string {
    return this.session.document();
  }

  // ---- Custom element life cycle

  connectedCallback(): void {
    if (!this.initialized) {
      this.initialized = true;
      // An attribute wins over the content, as for <jsesh-text>.
      const text = this.getAttribute("mdc") ?? this.textContent?.trim() ?? "";
      if (text !== "" && this.session.state.length === 0) this.session.setMdc(text, { undoable: false });
    }
    this.view = new EditorView(this.session, this.surface, {
      cadratHeight: this._cadratHeight,
      style: this._style,
      glyphsUrl: this.getAttribute("glyphs-url") ?? undefined,
      onError: (error) => this.dispatchEvent(new CustomEvent("jsesh-error", { detail: error })),
    });
    this.unsubscribe = this.session.subscribe(() =>
      this.dispatchEvent(new CustomEvent("jsesh-change", { detail: this.session.state, bubbles: true })),
    );
    // Not at once: the page may call `configureJSesh` right after importing this module.
    Promise.resolve()
      .then(() => loadSignDatabase(resolveGlyphsUrl(this.getAttribute("glyphs-url") ?? undefined)))
      .then(
        (database) => {
          this.session.possibilities = (code) => database.getPossibilityListFor(code);
        },
        // The sign database is optional: without it, a code stands for itself only.
        () => undefined,
      );
  }

  disconnectedCallback(): void {
    this.view?.destroy();
    this.view = undefined;
    this.unsubscribe?.();
  }

  attributeChangedCallback(name: string, _old: string | null, value: string | null): void {
    switch (name) {
      case "mdc":
        // Before the element is connected, connectedCallback takes care of it.
        if (this.initialized && value !== null) this.mdc = value;
        break;
      case "cadrat-height": {
        const height = Number.parseFloat(value ?? "");
        if (Number.isFinite(height) && height > 0) this.cadratHeight = height;
        break;
      }
      case "direction":
      case "orientation":
        this.session.setOptions(this.optionsFromAttributes());
        break;
      case "glyphs-url":
        // Read when the view is created.
        break;
    }
  }

  private optionsFromAttributes(): EditorOptions {
    const options: EditorOptions = {};
    const direction = this.getAttribute("direction");
    if (direction !== null) options.textDirection = direction.toLowerCase() === "rtl" ? "RIGHT_TO_LEFT" : "LEFT_TO_RIGHT";
    const orientation = this.getAttribute("orientation");
    if (orientation !== null) options.textOrientation = orientation.toLowerCase() === "vertical" ? "VERTICAL" : "HORIZONTAL";
    return options;
  }
}
