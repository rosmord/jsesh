/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Builds `web-dist/bundle/jsesh.js`: the three packages in one ES module, with
/// no dependency (`npm run zip` uses it). It is built from the packages'
/// sources, so the packages need not be built first.

import { resolve } from "node:path";
import { defaultClientConditions, defineConfig } from "vite";

const here = import.meta.dirname;

export default defineConfig({
  root: here,
  resolve: { conditions: ["source", ...defaultClientConditions] },
  build: {
    outDir: resolve(here, "../web-dist/bundle"),
    emptyOutDir: true,
    lib: { entry: resolve(here, "index.ts"), formats: ["es"], fileName: () => "jsesh.js" },
  },
});
