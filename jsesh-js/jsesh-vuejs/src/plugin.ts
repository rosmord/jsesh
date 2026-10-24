/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

import { configureJSesh, type JSeshConfiguration } from "@jsesh/elements";
import type { Plugin } from "vue";
import JSeshEditor from "./components/JSeshEditor.vue";
import JSeshEditorToolbar from "./components/JSeshEditorToolbar.vue";
import JSeshPalette from "./components/JSeshPalette.vue";
import JSeshText from "./components/JSeshText.vue";

/// Installs JSesh in a Vue application: sets the page-wide options (`configureJSesh`; set `glyphsUrl`, always,
/// when using a bundler) and registers the components globally.
///
/// ```ts
/// createApp(App).use(JSeshPlugin, { glyphsUrl: "/jsesh-glyphs/" }).mount("#app");
/// ```
///
/// You can also skip the plugin, call `configureJSesh` yourself, and import the components where you use them.
export const JSeshPlugin: Plugin<[JSeshConfiguration?]> = {
  install(app, options = {}) {
    configureJSesh(options);
    app.component("JSeshText", JSeshText);
    app.component("JSeshEditor", JSeshEditor);
    app.component("JSeshEditorToolbar", JSeshEditorToolbar);
    app.component("JSeshPalette", JSeshPalette);
  },
};

// Types for the global registration (`app.use(JSeshPlugin)`): the components are known in all templates.
declare module "vue" {
  interface GlobalComponents {
    JSeshText: typeof JSeshText;
    JSeshEditor: typeof JSeshEditor;
    JSeshEditorToolbar: typeof JSeshEditorToolbar;
    JSeshPalette: typeof JSeshPalette;
  }
}
