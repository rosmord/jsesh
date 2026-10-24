/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// `@jsesh/palette`: JSesh's sign palette, at two levels.
///
/// 1. `<jsesh-palette>`: a ready-made Web Component (importing this module
///    defines it).
/// 2. `PaletteController`: the logic of the palette (families, tags,
///    search, variants, history, user palette...) with no DOM. Give it a
///    `SignDatabase`, show its `state`, call its methods: you can draw the
///    palette as you want, in Vue, React or anything else.

import { JSeshPaletteElement } from "./jsesh-palette.js";

export { JSeshPaletteElement };
export {
  LAST_USED_KEY,
  PaletteController,
  PseudoFamily,
  USER_PALETTE_KEY,
  type FamilyOption,
  type PaletteControllerOptions,
  type PaletteState,
  type SignInfo,
  type VariantInfo,
} from "./controller.js";
export { DEFAULT_PALETTE_LABELS, type PaletteLabels } from "./labels.js";
export { localStoragePaletteStorage, memoryPaletteStorage, type PaletteStorage } from "./storage.js";

/// Defines `<jsesh-palette>` (done when this module is imported).
export function defineJSeshPalette(): void {
  if (typeof customElements === "undefined") return;
  if (customElements.get("jsesh-palette") === undefined) customElements.define("jsesh-palette", JSeshPaletteElement);
}

defineJSeshPalette();

declare global {
  interface HTMLElementTagNameMap {
    "jsesh-palette": JSeshPaletteElement;
  }
}
