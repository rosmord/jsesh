/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// A sign palette, after JSesh's (`jsesh.ui.palette.PalettePresenter`), built
/// on `SignDatabase` (`@jsesh/glyphs`) and `renderMdc` (`@jsesh/elements`).
///
/// The signs are chosen by family (with or without their variants), then by
/// tags; they can also be searched by value or code, or by the signs they
/// contain. A selected sign shows its values, tags, variants and description;
/// a double click (or Enter) chooses it, which sends a `sign-chosen` event.
/// The latest signs and the user palette are kept in the browser.

import { renderMdc } from "@jsesh/elements";

const ALL = "All";
const OTHERS = "Others";
const LAST_USED = "*last";
const USER_PALETTE = "*user";
const ALL_FAMILIES = "*all";
const TALL_NARROW_SIGNS = "*tallNarrow";
const LOW_BROAD_SIGNS = "*lowBroad";
const LOW_NARROW_SIGNS = "*lowNarrow";

/// Number of codes kept by the "back" button, as in JSesh.
const HISTORY_SIZE = 6;

/// The palette's controls; their roles are given by `data-palette`. The
/// styles are in `palette.css`.
const PALETTE_HTML = `
<div class="row">
  <select data-palette="family" aria-label="Sign family">
    <option value="">Select a sign family</option>
  </select>
  <label><input type="checkbox" data-palette="showAll" /> show all variants</label>
  <select data-palette="tag" aria-label="Tag"></select>
  <select data-palette="secondaryTag" aria-label="Secondary tag"></select>
</div>
<div class="row">
  <input data-palette="search" type="search" placeholder="value or code, then Enter" size="22" />
  <select data-palette="contains" aria-label="Signs containing"></select>
  <span class="spacer"></span>
  <span data-palette="count"></span>
</div>
<div class="body">
  <div data-palette="signs" role="listbox" aria-label="Signs"></div>
  <div data-palette="info">
    <div class="big">
      <jsesh-text data-palette="picture" cadrat-height="70"></jsesh-text>
      <span data-palette="code"></span>
    </div>
    <div class="row">
      <button data-palette="choose">Choose</button>
      <button data-palette="variants" title="Show the variants of the sign (again: variants of variants)">Variants</button>
      <button data-palette="containing" title="Show the signs containing this one (again: one level further)">Containing</button>
      <button data-palette="back" title="Previous sign">Back</button>
    </div>
    <label><input type="checkbox" data-palette="inUserPalette" /> in my palette</label>
    <dl>
      <dt>Values</dt>
      <dd data-palette="values"></dd>
      <dt>Tags</dt>
      <dd data-palette="tags"></dd>
      <dt>Variants</dt>
      <dd data-palette="variantList"></dd>
      <dt>Contains</dt>
      <dd data-palette="parts"></dd>
    </dl>
    <div data-palette="description">
      <jsesh-text data-palette="descriptionText" cadrat-height="18"></jsesh-text>
    </div>
  </div>
</div>
`;

const VARIANT_TYPES = {
  FULL: "full variant",
  PARTIAL: "partial variant",
  OTHER: "other variant",
  NO: "graphical variant only",
  UNSPECIFIED: "variant",
};

export class HieroglyphPalette extends EventTarget {
  /// @param {HTMLElement} root the element where the palette is built (its content is replaced).
  /// @param {import("@jsesh/glyphs").SignDatabase} database
  constructor(root, database) {
    super();
    root.classList.add("palette");
    root.innerHTML = PALETTE_HTML;
    this.db = database;
    this.$ = (name) => root.querySelector(`[data-palette="${name}"]`);
    this.displayed = [];
    this.selected = undefined;
    this.history = [];
    /// Closures computed by the Variants and Containing buttons, as in JSesh:
    /// each click adds one more level.
    this.variantSet = undefined;
    this.partSet = undefined;
    this.lastUsed = new Set(load("jsesh-palette-last"));
    this.userPalette = new Set(load("jsesh-palette-user"));

    this.observer = new IntersectionObserver((entries) => this.drawVisibleCells(entries), {
      root: this.$("signs"),
      rootMargin: "200px",
    });

    this.fillFamilies();
    this.$("family").addEventListener("change", () => this.selectCategory());
    this.$("showAll").addEventListener("change", () => this.selectCategory());
    this.$("tag").addEventListener("change", () => this.selectTag());
    this.$("secondaryTag").addEventListener("change", () => this.selectSecondaryTag());
    this.$("search").addEventListener("keydown", (e) => {
      if (e.key === "Enter") this.selectFromTransliterationOrCode();
    });
    this.$("contains").addEventListener("change", () => this.selectFilteredContaining(this.$("contains").value));
    this.$("variants").addEventListener("click", () => this.selectVariants());
    this.$("containing").addEventListener("click", () => this.selectContaining());
    this.$("back").addEventListener("click", () => this.selectPreviousSign());
    this.$("inUserPalette").addEventListener("change", () => this.toggleSignInUserPalette());
    this.$("choose").addEventListener("click", () => this.choose(this.selected));
    this.$("signs").addEventListener("keydown", (e) => this.gridKey(e));

    this.$("family").value = "A";
    this.selectCategory();
  }

  // ---- Choosing what to display

  fillFamilies() {
    const select = this.$("family");
    const add = (value, label) => select.add(new Option(label, value));
    add(LAST_USED, "Latest signs");
    add(USER_PALETTE, "User palette");
    for (const f of this.db.getFamilies()) add(f.code, `${f.code}. ${f.description}`);
    add(ALL_FAMILIES, "All signs");
    add(TALL_NARROW_SIGNS, "Tall narrow signs");
    add(LOW_BROAD_SIGNS, "Low broad signs");
    add(LOW_NARROW_SIGNS, "Low narrow signs");
  }

  /// The signs of the selected family, or pseudo-family.
  signsInFamily() {
    const sort = (codes) => [...codes].sort(this.db.compareCodes);
    switch (this.$("family").value) {
      case "":
        return [];
      case LAST_USED:
        return sort(this.lastUsed);
      case USER_PALETTE:
        return sort(this.userPalette);
      case ALL_FAMILIES:
        return this.db.getCodesForFamily("", true);
      case TALL_NARROW_SIGNS:
        return [...this.db.getTallNarrowSigns()];
      case LOW_BROAD_SIGNS:
        return [...this.db.getLowBroadSigns()];
      case LOW_NARROW_SIGNS:
        return [...this.db.getLowNarrowSigns()];
      default:
        return this.db.getCodesForFamily(this.$("family").value, this.$("showAll").checked);
    }
  }

  /// The signs of the family which have a tag (or no tag, for "Others").
  signsForTag(tag) {
    const signs = this.signsInFamily();
    if (tag === ALL) return signs;
    if (tag === OTHERS) return signs.filter((c) => this.db.getTagsForSign(c).length === 0);
    return signs.filter((c) => this.db.getTagsForSign(c).includes(tag));
  }

  selectCategory() {
    const signs = this.signsInFamily();
    this.setDisplayedSigns(signs);
    const tags = new Set(signs.flatMap((c) => this.db.getTagsForSign(c)));
    setOptions(this.$("tag"), [ALL, ...[...tags].sort(), OTHERS]);
    setOptions(this.$("secondaryTag"), []);
  }

  selectTag() {
    const tag = this.$("tag").value;
    const signs = this.signsForTag(tag);
    this.setDisplayedSigns(signs);
    // The secondary tags: the other tags of these signs.
    const tags = new Set(signs.flatMap((c) => this.db.getTagsForSign(c)));
    tags.delete(tag);
    setOptions(this.$("secondaryTag"), tags.size === 0 ? [] : ["", ...[...tags].sort()]);
  }

  selectSecondaryTag() {
    const secondary = this.$("secondaryTag").value;
    let signs = this.signsForTag(this.$("tag").value);
    if (secondary !== "") signs = signs.filter((c) => this.db.getTagsForSign(c).includes(secondary));
    this.setDisplayedSigns(signs);
  }

  /// Signs with a given value, or (if it looks like a code) a given code.
  selectFromTransliterationOrCode() {
    const text = this.$("search").value.trim();
    if (text === "") return;
    let codes;
    if (/^[a-zA-Z].*[0-9]/.test(text)) {
      codes = [...new Set([...this.db.getSuitableSignsForCode(text), ...this.db.getCodesStartingWith(text)])];
    } else {
      codes = [...this.db.getPossibilityFor(text)];
    }
    this.selectNoFamily();
    this.setDisplayedSigns(codes);
    if (codes.length > 0) this.setSelectedCode(codes[0]);
  }

  /// The variants of the selected sign; each click adds the variants of the
  /// variants.
  selectVariants() {
    if (this.selected === undefined) return;
    if (this.variantSet === undefined) {
      this.variantSet = new Set([this.selected]);
      this.partSet = undefined;
    }
    for (const code of [...this.variantSet]) {
      for (const v of this.db.getVariants(code)) this.variantSet.add(v.code);
    }
    this.selectNoFamily();
    this.setDisplayedSigns([...this.variantSet].sort(this.db.compareCodes), { keepClosures: true });
  }

  /// The signs which contain the selected sign; each click goes one level
  /// further.
  selectContaining() {
    if (this.selected === undefined) return;
    if (this.partSet === undefined) {
      this.partSet = new Set([this.selected]);
      this.variantSet = undefined;
    }
    for (const code of [...this.partSet]) {
      for (const c of this.db.getSignsContaining(code)) this.partSet.add(c);
    }
    this.selectNoFamily();
    this.setDisplayedSigns([...this.partSet].sort(this.db.compareCodes), { keepClosures: true });
  }

  /// Keeps, among the displayed signs, those which contain `code`, directly
  /// or not.
  selectFilteredContaining(code) {
    if (code === "") return;
    const closure = new Set();
    const toDo = [code];
    while (toDo.length > 0) {
      const sign = toDo.pop();
      if (!closure.has(sign)) {
        closure.add(sign);
        toDo.push(...this.db.getSignsContaining(sign));
      }
    }
    this.selectNoFamily();
    this.setDisplayedSigns(this.displayed.filter((c) => closure.has(c)));
  }

  selectNoFamily() {
    this.$("family").value = "";
    setOptions(this.$("tag"), []);
    setOptions(this.$("secondaryTag"), []);
  }

  // ---- The sign list

  setDisplayedSigns(codes, { keepClosures = false } = {}) {
    if (!keepClosures) {
      this.variantSet = undefined;
      this.partSet = undefined;
    }
    this.displayed = codes;
    const grid = this.$("signs");
    this.observer.disconnect();
    grid.replaceChildren();
    grid.scrollTop = 0;
    for (const code of codes) {
      const cell = document.createElement("button");
      cell.className = "sign";
      cell.dataset.code = code;
      cell.title = code;
      cell.tabIndex = -1;
      cell.innerHTML = `<span class="picture"></span><span class="code">${code}</span>`;
      cell.addEventListener("click", () => this.setSelectedCode(code));
      cell.addEventListener("dblclick", () => this.choose(code));
      grid.append(cell);
      this.observer.observe(cell);
    }
    this.$("count").textContent = `${codes.length} sign${codes.length === 1 ? "" : "s"}`;
    if (codes.includes(this.selected)) this.markSelected();
    else this.markSelected(codes[0]);
    this.updateSubSignChooser();
  }

  /// Signs are drawn when they become visible.
  drawVisibleCells(entries) {
    for (const entry of entries) {
      if (!entry.isIntersecting) continue;
      const cell = entry.target;
      this.observer.unobserve(cell);
      renderMdc(cell.dataset.code, { cadratHeight: 34 }).then(
        (svg) => (cell.firstElementChild.innerHTML = svg),
        () => (cell.firstElementChild.textContent = "?"),
      );
    }
  }

  /// The "containing" filter proposes the parts of the displayed signs.
  updateSubSignChooser() {
    const parts = new Set(this.displayed.flatMap((c) => this.db.getSignsIn(c)));
    setOptions(this.$("contains"), parts.size === 0 ? [] : ["", ...[...parts].sort(this.db.compareCodes)], "containing…");
  }

  gridKey(e) {
    const cells = [...this.$("signs").children];
    const i = cells.findIndex((c) => c.dataset.code === this.selected);
    const perRow = Math.max(1, Math.round(this.$("signs").clientWidth / (cells[0]?.offsetWidth || 1)));
    const moves = { ArrowRight: 1, ArrowLeft: -1, ArrowDown: perRow, ArrowUp: -perRow };
    if (e.key in moves) {
      const next = cells[Math.max(0, Math.min(cells.length - 1, i + moves[e.key]))];
      if (next !== undefined) this.setSelectedCode(next.dataset.code);
    } else if (e.key === "Enter" || e.key === " ") {
      this.choose(this.selected);
    } else {
      return;
    }
    e.preventDefault();
  }

  // ---- The selected sign

  setSelectedCode(code) {
    if (code !== this.history[this.history.length - 1]) {
      this.history.push(code);
      if (this.history.length > HISTORY_SIZE) this.history.shift();
    }
    this.markSelected(code);
  }

  selectPreviousSign() {
    if (this.history.length < 2) return;
    this.history.pop();
    const code = this.history[this.history.length - 1];
    if (!this.displayed.includes(code)) {
      this.selectNoFamily();
      this.setDisplayedSigns([code, ...this.displayed]);
    }
    this.markSelected(code);
  }

  markSelected(code = this.selected) {
    this.selected = code;
    for (const cell of this.$("signs").querySelectorAll(".sign")) {
      const selected = cell.dataset.code === code;
      cell.classList.toggle("selected", selected);
      cell.tabIndex = selected ? 0 : -1;
      if (selected) {
        cell.scrollIntoView({ block: "nearest" });
        if (this.$("signs").contains(document.activeElement)) cell.focus();
      }
    }
    this.showSignInfo();
  }

  showSignInfo() {
    const code = this.selected;
    const info = this.$("info");
    info.hidden = code === undefined;
    if (code === undefined) return;
    this.$("picture").mdc = code;
    this.$("code").textContent = code;
    this.$("values").textContent = this.db.getValuesFor(code).join(", ") || "—";
    this.$("tags").textContent = this.db.getTagsForSign(code).join(", ") || "—";
    this.$("variantList").replaceChildren(
      ...this.db.getVariants(code).map((v) => this.codeLink(v.code, VARIANT_TYPES[v.type] ?? v.type)),
    );
    this.$("parts").replaceChildren(...this.db.getSignsIn(code).map((c) => this.codeLink(c, "part")));
    const description = this.db.getDescriptionFor(code);
    this.$("description").hidden = description === "";
    this.$("descriptionText").mdc = description;
    this.$("inUserPalette").checked = this.userPalette.has(code);
  }

  /// A small button showing a sign, which selects it.
  codeLink(code, title) {
    const link = document.createElement("button");
    link.className = "code-link";
    link.title = `${code} (${title})`;
    link.innerHTML = `<jsesh-text cadrat-height="22">${code}</jsesh-text>${code}`;
    link.addEventListener("click", () => {
      if (!this.displayed.includes(code)) {
        this.selectNoFamily();
        this.setDisplayedSigns([code, ...this.displayed]);
      }
      this.setSelectedCode(code);
    });
    return link;
  }

  toggleSignInUserPalette() {
    if (this.selected === undefined) return;
    if (this.$("inUserPalette").checked) this.userPalette.add(this.selected);
    else this.userPalette.delete(this.selected);
    save("jsesh-palette-user", this.userPalette);
  }

  /// The sign is chosen: it goes in the latest signs, and the palette
  /// sends `sign-chosen`, with the code as `detail`.
  choose(code) {
    if (code === undefined) return;
    this.lastUsed.add(code);
    save("jsesh-palette-last", this.lastUsed);
    this.dispatchEvent(new CustomEvent("sign-chosen", { detail: code }));
  }
}

function setOptions(select, values, emptyLabel = "") {
  select.replaceChildren(...values.map((v) => new Option(v === "" ? emptyLabel : v, v)));
  select.disabled = values.length === 0;
}

function load(key) {
  try {
    return JSON.parse(localStorage.getItem(key) ?? "[]");
  } catch {
    return [];
  }
}

function save(key, set) {
  try {
    localStorage.setItem(key, JSON.stringify([...set]));
  } catch {
    // Not kept, then.
  }
}
