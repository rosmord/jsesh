/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Every text of the palette, to translate or rephrase.
export interface PaletteLabels {
  // The "families" (the Gardiner families come from the sign database).
  selectFamily: string;
  lastUsed: string;
  userPalette: string;
  allSigns: string;
  tallNarrowSigns: string;
  lowBroadSigns: string;
  lowNarrowSigns: string;
  /// The tag which stands for "no filter", and the one for "signs without tags".
  allTags: string;
  otherTags: string;

  // Variants, by type (`FULL`, `PARTIAL`, `OTHER`, `NO`, `UNSPECIFIED`).
  variantTypes: Record<string, string>;
  part: string;

  // The controls.
  showAllVariants: string;
  searchPlaceholder: string;
  containingPlaceholder: string;
  signCount: (count: number) => string;
  choose: string;
  variantsButton: string;
  variantsHint: string;
  containingButton: string;
  containingHint: string;
  back: string;
  backHint: string;
  inUserPalette: string;
  values: string;
  tags: string;
  variants: string;
  contains: string;
  loading: string;
}

export const DEFAULT_PALETTE_LABELS: PaletteLabels = {
  selectFamily: "Select a sign family",
  lastUsed: "Latest signs",
  userPalette: "User palette",
  allSigns: "All signs",
  tallNarrowSigns: "Tall narrow signs",
  lowBroadSigns: "Low broad signs",
  lowNarrowSigns: "Low narrow signs",
  allTags: "All",
  otherTags: "Others",
  variantTypes: {
    FULL: "full variant",
    PARTIAL: "partial variant",
    OTHER: "other variant",
    NO: "graphical variant only",
    UNSPECIFIED: "variant",
  },
  part: "part",
  showAllVariants: "show all variants",
  searchPlaceholder: "value or code, then Enter",
  containingPlaceholder: "containing…",
  signCount: (n) => `${n} sign${n === 1 ? "" : "s"}`,
  choose: "Choose",
  variantsButton: "Variants",
  variantsHint: "Show the variants of the sign (again: variants of variants)",
  containingButton: "Containing",
  containingHint: "Show the signs containing this one (again: one level further)",
  back: "Back",
  backHint: "Previous sign",
  inUserPalette: "in my palette",
  values: "Values",
  tags: "Tags",
  variants: "Variants",
  contains: "Contains",
  loading: "Loading the sign database…",
};
