/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Development server for the examples (`npm run dev`). It serves the
/// workspace packages from their sources, the glyph data under
/// `/glyph-data/`, and the JSesh text corpus under `/corpus/`.

import { createReadStream, existsSync, readdirSync, statSync } from "node:fs";
import { extname, join, resolve } from "node:path";
import { defaultClientConditions, defineConfig, type Plugin } from "vite";

const here = import.meta.dirname;

const TYPES: Record<string, string> = {
  ".json": "application/json",
  ".ttf": "font/ttf",
  ".gly": "text/plain; charset=utf-8",
  ".png": "image/png",
};

/// Serves the files of `dir` under `prefix`; `prefix` itself lists them.
function staticFolder(prefix: string, dir: string): Plugin {
  return {
    name: "static-" + prefix,
    configureServer(server) {
      server.middlewares.use(prefix, (req, res, next) => {
        const path = decodeURIComponent((req.url ?? "/").split("?")[0]!);
        if (path === "/" || path === "") {
          res.setHeader("Content-Type", "application/json");
          res.end(JSON.stringify(readdirSync(dir).sort()));
          return;
        }
        const file = resolve(dir, "." + path);
        if (!file.startsWith(dir) || !existsSync(file) || !statSync(file).isFile()) return next();
        res.setHeader("Content-Type", TYPES[extname(file)] ?? "application/octet-stream");
        createReadStream(file).pipe(res);
      });
    },
  };
}

export default defineConfig({
  root: here,
  resolve: { conditions: ["source", ...defaultClientConditions] },
  plugins: [
    staticFolder("/glyph-data", resolve(here, "../packages/glyphs/data")),
    staticFolder("/corpus", resolve(here, "../../jsesh-texts/src/main/assets/jsesh-texts")),
    staticFolder("/pictures", resolve(here, "../fixtures/generated/pictures")),
  ],
  build: {
    rollupOptions: {
      input: {
        index: join(here, "index.html"),
        vanilla: join(here, "vanilla/index.html"),
        compare: join(here, "compare/index.html"),
        editor: join(here, "editor/index.html"),
        palette: join(here, "palette/index.html"),
        custom: join(here, "custom/index.html"),
      },
    },
  },
});
