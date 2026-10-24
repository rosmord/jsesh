/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Where the palette keeps the *latest signs* and the *user palette*.
export interface PaletteStorage {
  /// The codes stored under `key` (`jsesh-palette-last` or `jsesh-palette-user`), or `[]`.
  load(key: string): string[];
  /// Stores `codes` under `key`.
  save(key: string, codes: readonly string[]): void;
}

/// The browser's `localStorage`: the lists belong to one browser on one site. If it is not available (private
/// mode...), the palette still works but forgets.
export const localStoragePaletteStorage: PaletteStorage = {
  load(key) {
    try {
      const value: unknown = JSON.parse(localStorage.getItem(key) ?? "[]");
      return Array.isArray(value) ? value.filter((c): c is string => typeof c === "string") : [];
    } catch {
      return [];
    }
  },
  save(key, codes) {
    try {
      localStorage.setItem(key, JSON.stringify(codes));
    } catch {
      // Not kept, then.
    }
  },
};

/// A storage which keeps the lists in memory only (for tests, or for pages which keep the lists themselves).
export function memoryPaletteStorage(initial: Record<string, string[]> = {}): PaletteStorage {
  const data = new Map(Object.entries(initial));
  return {
    load: (key) => [...(data.get(key) ?? [])],
    save: (key, codes) => void data.set(key, [...codes]),
  };
}
