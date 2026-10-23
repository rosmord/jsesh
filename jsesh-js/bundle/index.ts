/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Entry point of the self-contained bundle given to web sites
/// (`npm run zip`): everything `@jsesh/core`, `@jsesh/glyphs` and
/// `@jsesh/elements` export, in a single file. Importing it defines
/// `<jsesh-text>`.

export * from "@jsesh/core";
export * from "@jsesh/glyphs";
export * from "@jsesh/elements";
