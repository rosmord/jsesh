/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// `@jsesh/elements`: JSesh in web pages. Importing this module defines the
/// custom elements (`<jsesh-text>`).

import { JSeshTextElement } from "./jsesh-text.js";

export { autoRender, DEFAULT_CLASS, PROCESSED_CLASS, type AutoRenderOptions } from "./auto-render.js";
export {
  configureJSesh,
  glyphsUrl as resolveGlyphsUrl,
  layOutInPage,
  renderMdc,
  type BrowserRenderOptions,
  type JSeshConfiguration,
} from "./config.js";
export { JSeshTextElement };

/// Defines the custom elements (done when this module is imported).
export function defineJSeshElements(): void {
  if (typeof customElements === "undefined") return;
  if (customElements.get("jsesh-text") === undefined) customElements.define("jsesh-text", JSeshTextElement);
}

defineJSeshElements();

declare global {
  interface HTMLElementTagNameMap {
    "jsesh-text": JSeshTextElement;
  }
}
