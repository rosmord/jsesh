/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// The logic of a sign palette, after JSesh's (`jsesh.ui.palette.PalettePresenter`),
/// with no DOM: it is built on `SignDatabase` (`@jsesh/glyphs`), and a view
/// (`<jsesh-palette>`, or your own) shows its `state` and calls its methods.
///
/// The signs are chosen by family (with or without their variants), then by
/// tags; they can also be searched by value or code, or by the signs they
/// contain. A selected sign shows its values, tags, variants and description.
/// Choosing a sign (a double click in the view, say) calls the `onSignChosen`
/// listeners.

import type { SignDatabase } from "@jsesh/glyphs";
import { DEFAULT_PALETTE_LABELS, type PaletteLabels } from "./labels.js";
import { localStoragePaletteStorage, type PaletteStorage } from "./storage.js";

/// The values of `family` which are not Gardiner families.
export const PseudoFamily = {
  LAST_USED: "*last",
  USER_PALETTE: "*user",
  ALL: "*all",
  TALL_NARROW: "*tallNarrow",
  LOW_BROAD: "*lowBroad",
  LOW_NARROW: "*lowNarrow",
} as const;

/// Storage keys of the two lists.
export const LAST_USED_KEY = "jsesh-palette-last";
export const USER_PALETTE_KEY = "jsesh-palette-user";

/// Number of codes kept by the "back" button, as in JSesh.
const HISTORY_SIZE = 6;

export interface FamilyOption {
  /// A Gardiner family code (`A`, `Aa`...) or one of `PseudoFamily`.
  value: string;
  label: string;
}

export interface VariantInfo {
  code: string;
  /// `FULL`, `PARTIAL`, `OTHER`, `NO` or `UNSPECIFIED`.
  type: string;
  /// The type, in words.
  typeLabel: string;
}

/// What is known about the selected sign.
export interface SignInfo {
  code: string;
  values: readonly string[];
  tags: readonly string[];
  variants: readonly VariantInfo[];
  /// The signs this one contains.
  parts: readonly string[];
  /// The description, in MdC (it may contain signs).
  description: string;
  inUserPalette: boolean;
}

/// A snapshot of the palette.
export interface PaletteState {
  /// The family selected, or `""` when the signs come from a search or a navigation.
  readonly family: string;
  readonly showAllVariants: boolean;
  /// The tags of the family (the first is "all"), and the tag selected. Empty if there is no family.
  readonly tags: readonly string[];
  readonly tag: string;
  /// The other tags of the signs which remain, and the one selected (`""` for none).
  readonly secondaryTags: readonly string[];
  readonly secondaryTag: string;
  /// The signs which can be asked for as parts of the displayed signs (`""`, then codes).
  readonly containingOptions: readonly string[];
  /// The codes displayed. A new array whenever the list changes.
  readonly displayed: readonly string[];
  readonly selected: string | undefined;
  readonly info: SignInfo | undefined;
  readonly canGoBack: boolean;
}

export interface PaletteControllerOptions {
  /// Where the latest signs and the user palette are kept (`localStorage` by default).
  storage?: PaletteStorage;
  labels?: Partial<PaletteLabels>;
  /// The family shown at the start (`A` by default).
  initialFamily?: string;
}

export class PaletteController {
  private readonly storage: PaletteStorage;
  private readonly labels: PaletteLabels;
  private readonly lastUsed: Set<string>;
  private readonly userPalette: Set<string>;
  private readonly listeners = new Set<() => void>();
  private readonly chosenListeners = new Set<(code: string) => void>();
  private history: string[] = [];
  /// Closures computed by the Variants and Containing buttons, as in JSesh:
  /// each click adds one more level.
  private variantSet: Set<string> | undefined;
  private partSet: Set<string> | undefined;
  private cachedState: PaletteState | undefined;

  private family = "";
  private showAllVariants = false;
  private tags: readonly string[] = [];
  private tag = "";
  private secondaryTags: readonly string[] = [];
  private secondaryTag = "";
  private containingOptions: readonly string[] = [];
  private displayed: readonly string[] = [];
  private selected: string | undefined;

  constructor(
    readonly database: SignDatabase,
    options: PaletteControllerOptions = {},
  ) {
    this.storage = options.storage ?? localStoragePaletteStorage;
    this.labels = { ...DEFAULT_PALETTE_LABELS, ...options.labels };
    this.lastUsed = new Set(this.storage.load(LAST_USED_KEY));
    this.userPalette = new Set(this.storage.load(USER_PALETTE_KEY));
    this.family = options.initialFamily ?? "A";
    this.selectCategory();
  }

  // ---- Observing

  /// Calls `listener` after each change. Returns a function which removes it.
  /// Together with `state`, it fits `useSyncExternalStore` (React) and similar APIs.
  subscribe(listener: () => void): () => void {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  /// Calls `listener` with the code of each sign chosen. Returns a function which removes it.
  onSignChosen(listener: (code: string) => void): () => void {
    this.chosenListeners.add(listener);
    return () => this.chosenListeners.delete(listener);
  }

  /// The current state. The same object is returned until the next change.
  get state(): PaletteState {
    this.cachedState ??= {
      family: this.family,
      showAllVariants: this.showAllVariants,
      tags: this.tags,
      tag: this.tag,
      secondaryTags: this.secondaryTags,
      secondaryTag: this.secondaryTag,
      containingOptions: this.containingOptions,
      displayed: this.displayed,
      selected: this.selected,
      info: this.selected === undefined ? undefined : this.signInfo(this.selected),
      canGoBack: this.history.length >= 2,
    };
    return this.cachedState;
  }

  /// The families to offer: the latest signs, the user palette, the Gardiner families and the shape-based lists.
  get familyOptions(): FamilyOption[] {
    const l = this.labels;
    return [
      { value: PseudoFamily.LAST_USED, label: l.lastUsed },
      { value: PseudoFamily.USER_PALETTE, label: l.userPalette },
      ...this.database.getFamilies().map((f) => ({ value: f.code, label: `${f.code}. ${f.description}` })),
      { value: PseudoFamily.ALL, label: l.allSigns },
      { value: PseudoFamily.TALL_NARROW, label: l.tallNarrowSigns },
      { value: PseudoFamily.LOW_BROAD, label: l.lowBroadSigns },
      { value: PseudoFamily.LOW_NARROW, label: l.lowNarrowSigns },
    ];
  }

  // ---- Choosing what to display

  /// Shows a family (`""` shows nothing).
  setFamily(family: string): void {
    this.family = family;
    this.selectCategory();
    this.changed();
  }

  /// Shows, or hides, the variants in the families.
  setShowAllVariants(show: boolean): void {
    this.showAllVariants = show;
    this.selectCategory();
    this.changed();
  }

  /// Narrows the family to the signs with `tag`.
  setTag(tag: string): void {
    this.tag = tag;
    const signs = this.signsForTag(tag);
    this.setDisplayedSigns(signs);
    // The secondary tags: the other tags of these signs.
    const tags = new Set(signs.flatMap((c) => this.database.getTagsForSign(c)));
    tags.delete(tag);
    this.secondaryTags = tags.size === 0 ? [] : ["", ...[...tags].sort()];
    this.secondaryTag = "";
    this.changed();
  }

  setSecondaryTag(secondary: string): void {
    this.secondaryTag = secondary;
    let signs = this.signsForTag(this.tag);
    if (secondary !== "") signs = signs.filter((c) => this.database.getTagsForSign(c).includes(secondary));
    this.setDisplayedSigns(signs);
    this.changed();
  }

  /// Shows the signs with a given value, or (if `text` looks like a code) a given code.
  search(text: string): void {
    text = text.trim();
    if (text === "") return;
    let codes: string[];
    if (/^[a-zA-Z].*[0-9]/.test(text)) {
      codes = [...new Set([...this.database.getSuitableSignsForCode(text), ...this.database.getCodesStartingWith(text)])];
    } else {
      codes = [...this.database.getPossibilityFor(text)];
    }
    this.selectNoFamily();
    this.setDisplayedSigns(codes);
    if (codes.length > 0) this.select(codes[0]);
    else this.changed();
  }

  /// The variants of the selected sign; each call adds the variants of the variants.
  showVariants(): void {
    if (this.selected === undefined) return;
    if (this.variantSet === undefined) {
      this.variantSet = new Set([this.selected]);
      this.partSet = undefined;
    }
    for (const code of [...this.variantSet]) {
      for (const v of this.database.getVariants(code)) this.variantSet.add(v.code);
    }
    this.selectNoFamily();
    this.setDisplayedSigns([...this.variantSet].sort(this.database.compareCodes), { keepClosures: true });
    this.changed();
  }

  /// The signs which contain the selected sign; each call goes one level further.
  showContaining(): void {
    if (this.selected === undefined) return;
    if (this.partSet === undefined) {
      this.partSet = new Set([this.selected]);
      this.variantSet = undefined;
    }
    for (const code of [...this.partSet]) {
      for (const c of this.database.getSignsContaining(code)) this.partSet.add(c);
    }
    this.selectNoFamily();
    this.setDisplayedSigns([...this.partSet].sort(this.database.compareCodes), { keepClosures: true });
    this.changed();
  }

  /// Keeps, among the displayed signs, those which contain `code`, directly or not.
  filterContaining(code: string): void {
    if (code === "") return;
    const closure = new Set<string>();
    const toDo = [code];
    while (toDo.length > 0) {
      const sign = toDo.pop()!;
      if (!closure.has(sign)) {
        closure.add(sign);
        toDo.push(...this.database.getSignsContaining(sign));
      }
    }
    this.selectNoFamily();
    this.setDisplayedSigns(this.displayed.filter((c) => closure.has(c)));
    this.changed();
  }

  /// Shows `code`, and selects it, even if it is not in the displayed signs (it is then added to them). This is
  /// what a click on a variant or a part does.
  reveal(code: string): void {
    if (!this.displayed.includes(code)) {
      this.selectNoFamily();
      this.setDisplayedSigns([code, ...this.displayed]);
    }
    this.select(code);
  }

  // ---- The selected sign

  /// Selects a sign (and remembers it for `back`).
  select(code: string): void {
    if (code !== this.history[this.history.length - 1]) {
      this.history.push(code);
      if (this.history.length > HISTORY_SIZE) this.history.shift();
    }
    this.selected = code;
    this.changed();
  }

  /// Moves the selection by `offset` signs in the displayed list (a grid's arrow keys: ±1, ± the number of columns).
  move(offset: number): void {
    if (this.displayed.length === 0) return;
    const index = this.selected === undefined ? -1 : this.displayed.indexOf(this.selected);
    const target = Math.max(0, Math.min(this.displayed.length - 1, index + offset));
    this.select(this.displayed[target]);
  }

  /// Goes back to the previous sign (the last six are remembered).
  back(): void {
    if (this.history.length < 2) return;
    this.history.pop();
    const code = this.history[this.history.length - 1];
    if (!this.displayed.includes(code)) {
      this.selectNoFamily();
      this.setDisplayedSigns([code, ...this.displayed]);
    }
    this.selected = code;
    this.changed();
  }

  /// Puts a sign in the user palette, or takes it out. By default, the selected sign.
  setInUserPalette(inPalette: boolean, code = this.selected): void {
    if (code === undefined) return;
    if (inPalette) this.userPalette.add(code);
    else this.userPalette.delete(code);
    this.storage.save(USER_PALETTE_KEY, [...this.userPalette]);
    // If the user palette is displayed, it changes.
    if (this.family === PseudoFamily.USER_PALETTE) this.selectCategory();
    this.changed();
  }

  /// Chooses a sign (by default the selected one): it goes in the latest signs, and the `onSignChosen` listeners are called.
  choose(code = this.selected): void {
    if (code === undefined) return;
    this.lastUsed.add(code);
    this.storage.save(LAST_USED_KEY, [...this.lastUsed]);
    for (const listener of [...this.chosenListeners]) listener(code);
  }

  // ---- Internals

  /// The signs of the selected family, or pseudo-family.
  private signsInFamily(): string[] {
    const db = this.database;
    const sort = (codes: Iterable<string>) => [...codes].sort(db.compareCodes);
    switch (this.family) {
      case "":
        return [];
      case PseudoFamily.LAST_USED:
        return sort(this.lastUsed);
      case PseudoFamily.USER_PALETTE:
        return sort(this.userPalette);
      case PseudoFamily.ALL:
        return db.getCodesForFamily("", true);
      case PseudoFamily.TALL_NARROW:
        return [...db.getTallNarrowSigns()];
      case PseudoFamily.LOW_BROAD:
        return [...db.getLowBroadSigns()];
      case PseudoFamily.LOW_NARROW:
        return [...db.getLowNarrowSigns()];
      default:
        return db.getCodesForFamily(this.family, this.showAllVariants);
    }
  }

  /// The signs of the family which have a tag (or no tag, for "Others").
  private signsForTag(tag: string): string[] {
    const signs = this.signsInFamily();
    if (tag === this.labels.allTags) return signs;
    if (tag === this.labels.otherTags) return signs.filter((c) => this.database.getTagsForSign(c).length === 0);
    return signs.filter((c) => this.database.getTagsForSign(c).includes(tag));
  }

  private selectCategory(): void {
    const signs = this.signsInFamily();
    this.setDisplayedSigns(signs);
    const tags = new Set(signs.flatMap((c) => this.database.getTagsForSign(c)));
    this.tags = this.family === "" ? [] : [this.labels.allTags, ...[...tags].sort(), this.labels.otherTags];
    this.tag = this.family === "" ? "" : this.labels.allTags;
    this.secondaryTags = [];
    this.secondaryTag = "";
  }

  private selectNoFamily(): void {
    this.family = "";
    this.tags = [];
    this.tag = "";
    this.secondaryTags = [];
    this.secondaryTag = "";
  }

  private setDisplayedSigns(codes: readonly string[], { keepClosures = false } = {}): void {
    if (!keepClosures) {
      this.variantSet = undefined;
      this.partSet = undefined;
    }
    this.displayed = codes;
    // Keep the selection if it is still displayed, else select the first sign (without recording it in the history).
    if (this.selected === undefined || !codes.includes(this.selected)) this.selected = codes[0];
    // The "containing" filter proposes the parts of the displayed signs.
    const parts = new Set(codes.flatMap((c) => this.database.getSignsIn(c)));
    this.containingOptions = parts.size === 0 ? [] : ["", ...[...parts].sort(this.database.compareCodes)];
  }

  private signInfo(code: string): SignInfo {
    const db = this.database;
    return {
      code,
      values: db.getValuesFor(code),
      tags: db.getTagsForSign(code),
      variants: db.getVariants(code).map((v) => ({
        code: v.code,
        type: v.type,
        typeLabel: this.labels.variantTypes[v.type] ?? v.type,
      })),
      parts: db.getSignsIn(code),
      description: db.getDescriptionFor(code),
      inUserPalette: this.userPalette.has(code),
    };
  }

  private changed(): void {
    this.cachedState = undefined;
    for (const listener of [...this.listeners]) listener();
  }
}
