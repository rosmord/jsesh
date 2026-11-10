/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// The signs drawn by the user and stored in a document (`sign1`,
/// `sign2`...): port of `jsesh.document.embeddedsigns.EmbeddedSignList` and
/// of `jsesh.io.document.EmbeddedSignsHeaderCodec`.
///
/// In the header, each sign is a property
/// `JSesh_embedded_sign_sign1 <variantOf|-> v0 <base64(description)|-> <svg> <rendering>`,
/// where `v0` is the version of the storage format of the sign (a sign in
/// another version is skipped), and `JSesh_max_embedded_sign_id` is the
/// highest number ever used. `svg` (the user's file) and `rendering` (its
/// size, outline and ligature zones, computed by JSesh) are base64-encoded,
/// not compressed. They are kept as they are, and decoded only when needed
/// ([embeddedSignSvg], [embeddedSignRendering]).

/// One embedded sign.
export interface EmbeddedSign {
  /// `sign1`, `sign2`...
  code: string;
  /// The Gardiner code the sign is a variant of, if any.
  variantOf?: string;
  /// A description, possibly empty.
  description: string;
  /// The user's SVG file, base64-encoded, as in the header.
  svg: string;
  /// The drawing as JSesh reads it ([EmbeddedSignRendering] as JSON),
  /// base64-encoded, as in the header.
  rendering: string;
}

/// The embedded signs of a document.
export interface EmbeddedSigns {
  /// The highest number ever given to a sign (numbers are not reused).
  maxId: number;
  /// The signs, by increasing number.
  signs: EmbeddedSign[];
}

/// The drawing of an embedded sign, as written by the Java
/// `EmbeddedSignShapes.renderingData`: size, outline (starting at 0,0) and
/// the three ligature zones.
export interface EmbeddedSignRendering {
  w: number;
  h: number;
  d: string;
  /// x, y, width, height and gravity (`s`, `c` or `e`, then `t`, `c` or `b`),
  /// as in the glyph data of `@jsesh/glyphs`.
  zones: ([number, number, number, number, string] | null)[];
}

const CODE_PREFIX = "sign";
const MAX_ID = "JSesh_max_embedded_sign_id";
const SIGN_PREFIX = "JSesh_embedded_sign_";
const NONE = "-";
/// The version of the storage format of a sign, after `variantOf`.
export const EMBEDDED_SIGN_FORMAT_VERSION = "v0";

/// The number of an embedded sign code (12 for `sign12`), if it is one.
export function embeddedSignId(code: string): number | undefined {
  const m = /^sign([1-9][0-9]{0,8})$/.exec(code);
  return m === null ? undefined : Number(m[1]);
}

/// Is `code` of the form `sign<n>`?
export function isEmbeddedSignCode(code: string): boolean {
  return embeddedSignId(code) !== undefined;
}

/// Reads the embedded signs from header properties. Malformed signs, and
/// signs in an unknown format version, are skipped (with a warning). The
/// encoded data is not checked.
export function embeddedSignsFromMap(properties: Record<string, string>): EmbeddedSigns {
  const byId = new Map<number, EmbeddedSign>();
  let maxId = 0;
  for (const [key, value] of Object.entries(properties)) {
    if (!key.startsWith(SIGN_PREFIX)) continue;
    const code = key.substring(SIGN_PREFIX.length);
    const id = embeddedSignId(code);
    const parts = value.trim().split(/ +/);
    try {
      if (id === undefined || parts[1] !== EMBEDDED_SIGN_FORMAT_VERSION || parts.length !== 5) throw new Error("malformed");
      const sign: EmbeddedSign = {
        code,
        description: parts[2] === NONE ? "" : decodeText(parts[2]!),
        svg: parts[3]!,
        rendering: parts[4]!,
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
    const description = s.description === "" ? NONE : encodeText(s.description);
    result[SIGN_PREFIX + s.code] = `${s.variantOf ?? NONE} ${EMBEDDED_SIGN_FORMAT_VERSION} ${description} ${s.svg} ${s.rendering}`;
  }
  return result;
}

/// The code the next sign added to `signs` would get.
export function nextEmbeddedSignCode(signs: EmbeddedSigns): string {
  return CODE_PREFIX + (signs.maxId + 1);
}

/// The user's SVG file of a sign, byte for byte.
export function embeddedSignSvg(sign: EmbeddedSign): Uint8Array {
  return base64ToBytes(sign.svg);
}

/// The drawing of a sign, as JSesh reads it from its SVG.
export function embeddedSignRendering(sign: EmbeddedSign): EmbeddedSignRendering {
  return JSON.parse(decodeText(sign.rendering)) as EmbeddedSignRendering;
}

function base64ToBytes(text: string): Uint8Array {
  const binary = atob(text);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
  return bytes;
}

function decodeText(text: string): string {
  return new TextDecoder("utf-8", { fatal: true }).decode(base64ToBytes(text));
}

function encodeText(text: string): string {
  let binary = "";
  for (const b of new TextEncoder().encode(text)) binary += String.fromCharCode(b);
  return btoa(binary);
}
