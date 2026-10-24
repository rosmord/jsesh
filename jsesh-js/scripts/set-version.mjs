/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

// One version number for the whole JavaScript port. The "version" of the root package.json is the
// reference; every workspace package (and every "@jsesh/*" dependency between them) must carry it.
//
//   node scripts/set-version.mjs 0.2.0    sets the root version, then propagates it (`npm run version:set -- 0.2.0`)
//   node scripts/set-version.mjs          propagates the current root version
//   node scripts/set-version.mjs --check  changes nothing; exits with an error if something differs (`npm run version:check`)
//
// After a change, run `npm install` once to refresh package-lock.json.

import { existsSync, readdirSync, readFileSync, writeFileSync } from "node:fs";
import { join, resolve } from "node:path";

const root = resolve(import.meta.dirname, "..");
const DEPENDENCY_FIELDS = ["dependencies", "devDependencies", "peerDependencies", "optionalDependencies"];

const readJson = (file) => JSON.parse(readFileSync(file, "utf-8"));

const escapeRegExp = (s) => s.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");

/// Replaces the string value of `"key": "..."` (first occurrence) in the text of a JSON file,
/// leaving the rest of the file's formatting alone.
function replaceValue(text, key, value) {
  return text.replace(new RegExp(`("${escapeRegExp(key)}"\\s*:\\s*")[^"]*(")`), `$1${value}$2`);
}

/// Directories of the workspaces declared in the root package.json ("dir" or "dir/*" patterns).
function workspaceDirs(rootJson) {
  const dirs = [];
  for (const pattern of rootJson.workspaces) {
    if (pattern.endsWith("/*")) {
      const parent = join(root, pattern.slice(0, -2));
      for (const entry of readdirSync(parent, { withFileTypes: true })) {
        if (entry.isDirectory() && existsSync(join(parent, entry.name, "package.json")))
          dirs.push(join(parent, entry.name));
      }
    } else {
      dirs.push(join(root, pattern));
    }
  }
  return dirs;
}

const args = process.argv.slice(2);
const check = args.includes("--check");
const requested = args.find((a) => !a.startsWith("--"));

const rootFile = join(root, "package.json");
const rootJson = readJson(rootFile);

if (requested) {
  if (check) throw new Error("--check cannot be combined with a new version.");
  if (!/^\d+\.\d+\.\d+(-[0-9A-Za-z.-]+)?$/.test(requested)) throw new Error(`Not a valid version: ${requested}`);
  writeFileSync(rootFile, replaceValue(readFileSync(rootFile, "utf-8"), "version", requested));
  rootJson.version = requested;
}

const version = rootJson.version;
if (!version) throw new Error("The root package.json has no \"version\".");

const packages = workspaceDirs(rootJson).map((dir) => {
  const file = join(dir, "package.json");
  return { file, json: readJson(file) };
});
const names = new Set(packages.map((p) => p.json.name));

let problems = 0;
for (const { file, json } of packages) {
  const rel = file.slice(root.length + 1);
  let text = readFileSync(file, "utf-8");
  let changed = false;
  const update = (holder, key) => {
    if (holder[key] === version) return;
    console.log(`${check ? "MISMATCH" : "update"}  ${rel}: ${key} ${holder[key]} -> ${version}`);
    problems++;
    text = replaceValue(text, key, version);
    changed = true;
  };
  update(json, "version");
  for (const field of DEPENDENCY_FIELDS) {
    for (const dep of Object.keys(json[field] ?? {})) {
      if (names.has(dep)) update(json[field], dep);
    }
  }
  if (changed && !check) writeFileSync(file, text);
}

if (check && problems > 0) {
  console.error(`${problems} difference(s) with the root version ${version}: run "node scripts/set-version.mjs".`);
  process.exit(1);
}
console.log(problems === 0 ? `All ${packages.length} packages are at ${version}.` : `Done: ${packages.length} packages at ${version}. Run "npm install" to refresh package-lock.json.`);
