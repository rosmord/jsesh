/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// The sign database, for palettes: port of the read-only part of
/// `jsesh.glyphs.signdata.HieroglyphDatabase`, reading the data exported by
/// `./gradlew :jseshJsExport:exportSignInfo` (`sign-info.json`).

/// The data of `sign-info.json` (see `jsesh.jsexport.ExportSignInfo`).
export interface SignInfoData {
  format: number;
  families: [string, string][];
  signs: SignEntry[];
  values: Record<string, string[]>;
  tallNarrow: string[];
  lowBroad: string[];
  lowNarrow: string[];
}

export interface SignEntry {
  /// Code.
  c: string;
  /// 1 if the sign is listed when variants are hidden.
  b?: 1;
  /// Values.
  v?: string[];
  /// Tags.
  t?: string[];
  /// Variants: code and type.
  var?: [string, SignVariantType][];
  /// The signs this one contains.
  in?: string[];
  /// Description, in MdC.
  d?: string;
}

export interface HieroglyphFamily {
  /// The Gardiner family: `A`, `B`... `Aa`, `Ff`, `NU`, `NL`.
  code: string;
  description: string;
}

/// How much a variant shares the uses of its sign (`SignVariantType`).
export type SignVariantType = "FULL" | "PARTIAL" | "OTHER" | "NO" | "UNSPECIFIED";

export interface SignVariant {
  code: string;
  type: SignVariantType;
}

const NO_CODES: readonly string[] = [];

export class SignDatabase {
  private readonly signs = new Map<string, SignEntry>();
  /// Position of each code in Manuel de Codage order.
  private readonly order = new Map<string, number>();
  private readonly containing = new Map<string, string[]>();
  private readonly families: readonly HieroglyphFamily[];

  constructor(private readonly data: SignInfoData) {
    data.signs.forEach((sign, i) => {
      this.signs.set(sign.c, sign);
      this.order.set(sign.c, i);
      for (const part of sign.in ?? []) {
        let list = this.containing.get(part);
        if (list === undefined) this.containing.set(part, (list = []));
        list.push(sign.c);
      }
    });
    this.families = data.families.map(([code, description]) => ({ code, description }));
  }

  /// Compares codes in Manuel de Codage order (`ManuelDeCodage.compareCodes`
  /// for the known signs; unknown codes come last).
  readonly compareCodes = (a: string, b: string): number => {
    const ia = this.order.get(a) ?? Number.MAX_SAFE_INTEGER;
    const ib = this.order.get(b) ?? Number.MAX_SAFE_INTEGER;
    return ia !== ib ? ia - ib : a < b ? -1 : a > b ? 1 : 0;
  };

  /// All the codes, in Manuel de Codage order.
  getCodesSet(): string[] {
    return this.data.signs.map((s) => s.c);
  }

  getFamilies(): readonly HieroglyphFamily[] {
    return this.families;
  }

  /// The codes of a family (all the codes if `family` is empty), with or
  /// without the variants (signs listed as variants of others, unless
  /// [isAlwaysDisplayed]).
  getCodesForFamily(family: string, includeVariants: boolean): string[] {
    const pattern = family === "" ? undefined : new RegExp(`^(US[0-9]+)?${family}[0-9]+[a-zA-Z]*$`);
    return this.data.signs
      .filter((s) => (pattern === undefined || pattern.test(s.c)) && (includeVariants || s.b === 1))
      .map((s) => s.c);
  }

  /// Should the sign be listed when the variants are hidden?
  isAlwaysDisplayed(code: string): boolean {
    return this.signs.get(code)?.b === 1;
  }

  getValuesFor(code: string): readonly string[] {
    return this.signs.get(code)?.v ?? NO_CODES;
  }

  getTagsForSign(code: string): readonly string[] {
    return this.signs.get(code)?.t ?? NO_CODES;
  }

  /// The variants directly recorded for a sign (not transitive).
  getVariants(code: string): SignVariant[] {
    return (this.signs.get(code)?.var ?? []).map(([c, type]) => ({ code: c, type }));
  }

  /// The signs which contain this one in their drawing.
  getSignsContaining(code: string): readonly string[] {
    return this.containing.get(code) ?? NO_CODES;
  }

  /// The signs this one contains.
  getSignsIn(code: string): readonly string[] {
    return this.signs.get(code)?.in ?? NO_CODES;
  }

  /// The description of a sign, in MdC (possibly empty).
  getDescriptionFor(code: string): string {
    return this.signs.get(code)?.d ?? "";
  }

  /// The signs a palette proposes for a value (`getPossibilityFor(value,
  /// PALETTE)`, keeping single signs only).
  getPossibilityFor(value: string): readonly string[] {
    return this.data.values[value.replaceAll("-", "")] ?? NO_CODES;
  }

  /// The codes starting with `code` (possibly after a `USn` prefix).
  getCodesStartingWith(code: string): string[] {
    const pattern = new RegExp(`^(US[0-9]+)?${escapeRegExp(code)}`);
    return this.getCodesSet().filter((c) => pattern.test(c));
  }

  /// The codes which might match a Gardiner code typed by the user, case
  /// insensitive: the code itself first, then its variants by name (`A1A`,
  /// `US1A1`...).
  getSuitableSignsForCode(code: string): string[] {
    const pattern = new RegExp(`^(US[0-9]+)?${escapeRegExp(code)}[a-zA-Z]*$`, "i");
    const lower = code.toLowerCase();
    const exact = this.getCodesSet().filter((c) => c.toLowerCase() === lower);
    return [...exact, ...this.getCodesSet().filter((c) => c.toLowerCase() !== lower && pattern.test(c))];
  }

  getTallNarrowSigns(): readonly string[] {
    return this.data.tallNarrow;
  }

  getLowBroadSigns(): readonly string[] {
    return this.data.lowBroad;
  }

  getLowNarrowSigns(): readonly string[] {
    return this.data.lowNarrow;
  }
}

function escapeRegExp(s: string): string {
  return s.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}
