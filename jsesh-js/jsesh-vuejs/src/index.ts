/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// `@jsesh/vue`: JSesh's Web Components as Vue 3 components.
///
/// - `JSeshText`: displays MdC.
/// - `JSeshEditor`: the hieroglyphic editor, with `v-model:mdc`.
/// - `JSeshEditorToolbar`: undo, redo, grouping, cartouche, red.
/// - `JSeshPalette`: the sign palette.
///
/// `JSeshPlugin` configures JSesh and registers all four. `useExternalState` makes a `ref` of the headless
/// `EditorSession` and `PaletteController` of `@jsesh/editor` and `@jsesh/palette`, to build your own components.

export { configureJSesh, type JSeshConfiguration } from "@jsesh/elements";
export { default as JSeshEditor } from "./components/JSeshEditor.vue";
export { default as JSeshEditorToolbar } from "./components/JSeshEditorToolbar.vue";
export { DEFAULT_TOOLBAR_LABELS, type ToolbarLabels } from "./components/toolbar-labels.js";
export { default as JSeshPalette } from "./components/JSeshPalette.vue";
export { default as JSeshText } from "./components/JSeshText.vue";
export { JSeshPlugin } from "./plugin.js";
export { useExternalState, type ExternalStateSource } from "./useExternalState.js";
