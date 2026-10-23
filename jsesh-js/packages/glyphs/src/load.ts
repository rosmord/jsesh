/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Loading the glyph data over HTTP.

import { GlyphIndexProvider, type GlyphIndexData } from "./provider.js";
import { SignDatabase, type SignInfoData } from "./sign-database.js";
import type { SignShape } from "@jsesh/core";

/// The URL of the data shipped with this package (its `data` folder). It
/// works when the package is served as is (from a CDN, for instance); a
/// site which hosts the data elsewhere passes its own URL.
export function defaultGlyphDataUrl(): string {
  return new URL("../data/", import.meta.url).href;
}

const cache = new Map<string, Promise<GlyphIndexProvider>>();

/// Loads the glyph index from `baseUrl` (a folder with `glyph-index.json`
/// and `glyphs/`). Shapes are then loaded when needed. The provider is
/// shared by all the callers with the same URL.
export function loadGlyphProvider(baseUrl: string = defaultGlyphDataUrl()): Promise<GlyphIndexProvider> {
  const base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
  let result = cache.get(base);
  if (result === undefined) {
    result = fetchJson<GlyphIndexData>(base + "glyph-index.json").then(
      (index) => new GlyphIndexProvider(index, (fileName) => fetchJson<SignShape>(`${base}glyphs/${fileName}.json`)),
    );
    result.catch(() => cache.delete(base));
    cache.set(base, result);
  }
  return result;
}

const databases = new Map<string, Promise<SignDatabase>>();

/// Loads the sign database (`sign-info.json`, 700 kB, 130 kB compressed)
/// from `baseUrl`. Only palettes need it.
export function loadSignDatabase(baseUrl: string = defaultGlyphDataUrl()): Promise<SignDatabase> {
  const base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
  let result = databases.get(base);
  if (result === undefined) {
    result = fetchJson<SignInfoData>(base + "sign-info.json").then((data) => new SignDatabase(data));
    result.catch(() => databases.delete(base));
    databases.set(base, result);
  }
  return result;
}

async function fetchJson<T>(url: string): Promise<T> {
  const response = await fetch(url);
  if (!response.ok) throw new Error(`${url}: ${response.status} ${response.statusText}`);
  return (await response.json()) as T;
}
