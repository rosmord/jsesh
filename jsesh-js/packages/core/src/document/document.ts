/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Reading and writing whole JSesh documents (header + text): port of
/// `jsesh.io.document.MDCDocumentReader` and `MDCDocumentWriter`.

import { parseMdc } from "../mdcreader/builder.js";
import { AlphabeticCharacter, HieroglyphicText, LineBreak } from "../model/model.js";
import { toMdc } from "../writer/writer.js";
import { embeddedSignsFromMap, embeddedSignsToMap, type EmbeddedSigns } from "./embedded-signs.js";
import { stripJSeshHeader, writeJSeshHeader } from "./header.js";
import {
  DEFAULT_DOCUMENT_PREFERENCES,
  documentPreferencesFromMap,
  documentPreferencesToMap,
  type DocumentPreferences,
} from "./preferences.js";

export interface MdcDocument {
  preferences: DocumentPreferences;
  text: HieroglyphicText;
  /// The signs stored in the document (`sign1`...), if it has ever had any.
  embeddedSigns?: EmbeddedSigns;
}

export interface ReadDocumentOptions {
  /// If true, a syntax error is thrown. Otherwise (the default, as in JSesh)
  /// the text is re-read line by line, and lines in error are replaced by an
  /// error message followed by their source as latin text.
  failFast?: boolean;
}

/// Reads a JSesh document (`.gly` content), with its optional header.
export function readMdcDocument(source: string, options: ReadDocumentOptions = {}): MdcDocument {
  const { properties, body } = stripJSeshHeader(source);
  const preferences =
    Object.keys(properties).length > 0 ? documentPreferencesFromMap(properties) : { ...DEFAULT_DOCUMENT_PREFERENCES };
  let text: HieroglyphicText;
  try {
    text = parseMdc(body);
  } catch (e) {
    if (options.failFast) throw e;
    text = new HieroglyphicText();
    for (const line of body.split(/\r\n|\n|\r/)) {
      try {
        text.addAll(parseMdc(line).topItems.slice());
      } catch (lineError) {
        text.addAll(AlphabeticCharacter.fromMdcText("b", "Error in file: " + (lineError as Error).message));
        text.addTopItem(new LineBreak());
        text.addAll(AlphabeticCharacter.fromMdcText("l", line));
        text.addTopItem(new LineBreak());
      }
    }
  }
  const embeddedSigns = embeddedSignsFromMap(properties);
  return embeddedSigns.maxId > 0 ? { preferences, text, embeddedSigns } : { preferences, text };
}

/// Writes a JSesh document, with its header.
export function writeMdcDocument(document: MdcDocument): string {
  const properties = documentPreferencesToMap(document.preferences);
  if (document.embeddedSigns !== undefined) {
    // Written after the preferences, sorted, as Java does.
    const signs = embeddedSignsToMap(document.embeddedSigns);
    for (const key of Object.keys(signs).sort()) properties[key] = signs[key]!;
  }
  return writeJSeshHeader(properties) + toMdc(document.text);
}
