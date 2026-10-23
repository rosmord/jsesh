/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

import { Hieroglyph, type ModelElement } from "../model/model.js";
import type { GlyphProvider } from "./glyphs.js";

/// The canonical codes of all the signs in an element, to load their shapes
/// before drawing it.
export function signCodes(element: ModelElement, provider: GlyphProvider): Set<string> {
  const result = new Set<string>();
  const visit = (e: ModelElement) => {
    if (e instanceof Hieroglyph) result.add(provider.canonicalCode(e.code));
    else for (const c of e.children) visit(c);
  };
  visit(element);
  return result;
}
