/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// `@jsesh/glyphs`: JSesh's hieroglyphic font and sign database, exported
/// from the Java version, and their loaders.

export { fileNameForCode } from "./glyph-files.js";
export { defaultGlyphDataUrl, loadGlyphProvider, loadSignDatabase } from "./load.js";
export {
  SignDatabase,
  type HieroglyphFamily,
  type SignEntry,
  type SignInfoData,
  type SignVariant,
  type SignVariantType,
} from "./sign-database.js";
export {
  GlyphIndexProvider,
  type GlyphEntry,
  type GlyphIndexData,
  type ShapeLoader,
  type ZoneEntry,
} from "./provider.js";
