/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// The signs drawn by the user and stored in a document (`SIGN1`,
/// `SIGN2`...): port of `jsesh.document.embeddedsigns.EmbeddedSignList` and
/// of `jsesh.io.document.EmbeddedSignsHeaderCodec`.
///
/// In the header, each sign is a property
/// `JSesh_embedded_sign_SIGN1 <variantOf|-> <base64(description)|-> <base64(svg)>`,
/// and `JSesh_max_embedded_sign_id` is the highest number ever used.

/// One embedded sign.
export interface EmbeddedSign {
  /// `SIGN1`, `SIGN2`...
  code: string;
  /// The normalized SVG written by JSesh (one path, plus ligature zones).
  svg: string;
  /// The Gardiner code the sign is a variant of, if any.
  variantOf?: string;
  /// A description, possibly empty.
  description: string;
}

/// The embedded signs of a document.
export interface EmbeddedSigns {
  /// The highest number ever given to a sign (numbers are not reused).
  maxId: number;
  /// The signs, by increasing number.
  signs: EmbeddedSign[];
}

const CODE_PREFIX = "SIGN";
const MAX_ID = "JSesh_max_embedded_sign_id";
const SIGN_PREFIX = "JSesh_embedded_sign_";
const NONE = "-";

/// The number of an embedded sign code (12 for `SIGN12`), if it is one.
export function embeddedSignId(code: string): number | undefined {
  const m = /^SIGN([1-9][0-9]{0,8})$/.exec(code);
  return m === null ? undefined : Number(m[1]);
}

/// Is `code` of the form `SIGN<n>`?
export function isEmbeddedSignCode(code: string): boolean {
  return embeddedSignId(code) !== undefined;
}

/// Reads the embedded signs from header properties. Malformed signs are
/// skipped (with a warning).
export function embeddedSignsFromMap(properties: Record<string, string>): EmbeddedSigns {
  const byId = new Map<number, EmbeddedSign>();
  let maxId = 0;
  for (const [key, value] of Object.entries(properties)) {
    if (!key.startsWith(SIGN_PREFIX)) continue;
    const code = key.substring(SIGN_PREFIX.length);
    const id = embeddedSignId(code);
    const parts = value.trim().split(/ +/);
    try {
      if (id === undefined || parts.length !== 3) throw new Error("malformed");
      const sign: EmbeddedSign = {
        code,
        svg: decodeBase64(parts[2]!),
        description: parts[1] === NONE ? "" : decodeBase64(parts[1]!),
      };
      if (parts[0] !== NONE) sign.variantOf = parts[0]!;
      byId.set(id, sign);
      maxId = Math.max(maxId, id);
    } catch {
      console.warn(`Malformed embedded sign ${code} in document header`);
    }
  }
  const declared = Number.parseInt(properties[MAX_ID] ?? "", 10);
  if (Number.isFinite(declared)) maxId = Math.max(maxId, declared);
  const signs = [...byId.entries()].sort(([a], [b]) => a - b).map(([, s]) => s);
  return { maxId, signs };
}

/// The header properties of embedded signs (empty if there has never been one).
export function embeddedSignsToMap(signs: EmbeddedSigns): Record<string, string> {
  const result: Record<string, string> = {};
  if (signs.maxId > 0) result[MAX_ID] = String(signs.maxId);
  for (const s of signs.signs) {
    const description = s.description === "" ? NONE : encodeBase64(s.description);
    result[SIGN_PREFIX + s.code] = `${s.variantOf ?? NONE} ${description} ${encodeBase64(s.svg)}`;
  }
  return result;
}

/// The code the next sign added to `signs` would get.
export function nextEmbeddedSignCode(signs: EmbeddedSigns): string {
  return CODE_PREFIX + (signs.maxId + 1);
}

function decodeBase64(text: string): string {
  const binary = atob(text);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
  return new TextDecoder("utf-8", { fatal: true }).decode(bytes);
}

function encodeBase64(text: string): string {
  let binary = "";
  for (const b of new TextEncoder().encode(text)) binary += String.fromCharCode(b);
  return btoa(binary);
}
