/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Entry point of the self-contained bundle given to web sites
/// (`npm run zip`): everything `@jsesh/core`, `@jsesh/glyphs`,
/// `@jsesh/elements`, `@jsesh/editor` and `@jsesh/palette` export, in a single
/// file. Importing it defines `<jsesh-text>`, `<jsesh-editor>` and
/// `<jsesh-palette>`.

export * from "@jsesh/core";
export * from "@jsesh/glyphs";
export * from "@jsesh/elements";
export * from "@jsesh/editor";
export * from "@jsesh/palette";
