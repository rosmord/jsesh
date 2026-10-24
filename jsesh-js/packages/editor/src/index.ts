/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// `@jsesh/editor`: a hieroglyphic editor, at three levels.
///
/// 1. `<jsesh-editor>`: a ready-made Web Component (importing this module
///    defines it).
/// 2. `EditorSession` (the editing state machine, with no DOM) and
///    `EditorView` (shows a session in an element, and sends it the keys and
///    clicks): to build your own markup around the editor.
/// 3. `itemAreas`, `positionAt` and `caretArea` (the geometry of a laid-out
///    text) with `EditorSession`: to draw the editor yourself.

import { JSeshEditorElement } from "./jsesh-editor.js";

export { JSeshEditorElement };
export {
  EditorSession,
  type EditorChange,
  type EditorOptions,
  type EditorState,
  type KeyInput,
  type PossibilityProvider,
} from "./session.js";
export { EDITOR_VIEW_CSS, EditorView, type EditorViewOptions } from "./view.js";
export { CARET_THICKNESS, caretArea, itemAreas, positionAt, type Area, type TextFlow } from "./geometry.js";

/// Defines `<jsesh-editor>` (done when this module is imported).
export function defineJSeshEditor(): void {
  if (typeof customElements === "undefined") return;
  if (customElements.get("jsesh-editor") === undefined) customElements.define("jsesh-editor", JSeshEditorElement);
}

defineJSeshEditor();

declare global {
  interface HTMLElementTagNameMap {
    "jsesh-editor": JSeshEditorElement;
  }
}
