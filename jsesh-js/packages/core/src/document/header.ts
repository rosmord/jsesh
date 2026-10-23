/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// The `++JSesh_xxx value +s` header of JSesh files: port of
/// `jsesh.io.document.JSeshInfoReader` and of the header part of
/// `MDCDocumentWriter`.

const JSESH_INFO_PREFIX = "++JSesh_";

/// Splits a JSesh file into its header properties and its MdC body.
///
/// Properties are kept as strings, keyed by their full name (e.g.
/// `JSesh_page_direction`); see `documentPreferencesFromMap` to interpret
/// them. A leading byte-order mark is ignored.
export function stripJSeshHeader(source: string): { properties: Record<string, string>; body: string } {
  let pos = source.startsWith("﻿") ? 1 : 0;
  const properties: Record<string, string> = {};
  while (source.startsWith(JSESH_INFO_PREFIX, pos)) {
    let end = pos;
    while (end < source.length && source[end] !== "\n" && source[end] !== "\r") end++;
    const line = source.substring(pos, end);
    if (source[end] === "\r" && source[end + 1] === "\n") pos = end + 2;
    else if (end < source.length) pos = end + 1;
    else pos = end;
    const tab = line.split(" ");
    properties[tab[0]!.substring(2)] = tab.length > 1 ? tab[1]! : "";
  }
  return { properties, body: source.substring(pos) };
}

/// Writes a JSesh header for `properties` (the `JSesh_Info` line first).
export function writeJSeshHeader(properties: Record<string, string>): string {
  let result = "++JSesh_Info 1.0 +s\n";
  for (const [key, value] of Object.entries(properties)) {
    if (key !== "JSesh_Info") result += `++${key} ${value} +s\n`;
  }
  return result;
}
