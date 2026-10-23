/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// The file name (without extension) of a sign's shape, in the `glyphs`
/// folder of the data.
///
/// Codes are case-sensitive, but file systems often aren't: lowercase
/// letters and digits are kept, an uppercase letter becomes `_` and the
/// letter in lowercase, any other character `-`, its hexadecimal code and
/// `-`. Mirrors `jsesh.jsexport.ExportGlyphData.fileNameForCode`.
export function fileNameForCode(code: string): string {
  let result = "";
  for (let i = 0; i < code.length; i++) {
    const c = code[i]!;
    if ((c >= "a" && c <= "z") || (c >= "0" && c <= "9")) result += c;
    else if (c >= "A" && c <= "Z") result += "_" + c.toLowerCase();
    else result += "-" + code.charCodeAt(i).toString(16) + "-";
  }
  return result;
}
