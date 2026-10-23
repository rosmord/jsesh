/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

// Builds the zip files to give to people who want JSesh on their web site
// (`npm run zip`, which first builds `web-dist/bundle/jsesh.js`):
//
//   web-dist/jsesh-web-<version>.zip             the library, the glyph data, and examples;
//   web-dist/jsesh-web-<version>-code-only.zip   the same without the glyph data (for sites using a shared copy).
//
// Needs the `zip` command, and the glyph data (`../gradlew -p .. :jseshJsExport:exportAll`).

import { execFileSync } from "node:child_process";
import { cpSync, existsSync, mkdirSync, readFileSync, rmSync, writeFileSync } from "node:fs";
import { join, resolve } from "node:path";

const root = resolve(import.meta.dirname, "..");
const out = join(root, "web-dist");
const version = JSON.parse(readFileSync(join(root, "packages/elements/package.json"), "utf-8")).version;
const bundle = join(out, "bundle/jsesh.js");
const data = join(root, "packages/glyphs/data");

if (!existsSync(bundle)) throw new Error(`${bundle} not found: run "npm run zip", not this script alone.`);
if (!existsSync(join(data, "glyph-index.json")))
  throw new Error(`No glyph data in ${data}: run "../gradlew -p .. :jseshJsExport:exportAll" first.`);

const NAME = "jsesh-web";
const stage = join(out, "stage");
const site = join(stage, NAME);
rmSync(stage, { recursive: true, force: true });
mkdirSync(join(site, "jsesh"), { recursive: true });

cpSync(bundle, join(site, "jsesh/jsesh.js"));
cpSync(join(root, "web-template/index.html"), join(site, "index.html"));
cpSync(join(root, "web-template/README.md"), join(site, "README.md"));
cpSync(join(root, "examples/examples.css"), join(site, "examples.css"));
for (const dir of ["editor", "palette"]) cpSync(join(root, "examples", dir), join(site, dir), { recursive: true });

/// The examples are written for the development server: bare imports (`@jsesh/...`) and `/glyph-data/`.
/// Here, one import map sends the three packages to the bundle, and the data is next to it.
const IMPORT_MAP = `<script type="importmap">
      {
        "imports": {
          "@jsesh/core": "../jsesh/jsesh.js",
          "@jsesh/glyphs": "../jsesh/jsesh.js",
          "@jsesh/elements": "../jsesh/jsesh.js"
        }
      }
    </script>
    `;

function adaptExample(file) {
  let html = readFileSync(file, "utf-8");
  if (!html.includes("/glyph-data/") || !html.includes('<script type="module"'))
    throw new Error(`${file}: not what this script expects; update scripts/make-web-zip.mjs.`);
  html = html.replaceAll('"/glyph-data/"', '"../jsesh/data/"');
  html = html.replace('<script type="module"', IMPORT_MAP + '<script type="module"');
  if (html.includes("/glyph-data/")) throw new Error(`${file}: a /glyph-data/ URL is left.`);
  writeFileSync(file, html);
}
adaptExample(join(site, "editor/index.html"));
adaptExample(join(site, "palette/index.html"));

function zip(fileName) {
  const target = join(out, fileName);
  rmSync(target, { force: true });
  execFileSync("zip", ["-r", "-q", "-X", target, NAME], { cwd: stage });
  console.log("written", target);
}

zip(`${NAME}-${version}-code-only.zip`);
cpSync(data, join(site, "jsesh/data"), { recursive: true });
zip(`${NAME}-${version}.zip`);

rmSync(stage, { recursive: true, force: true });
