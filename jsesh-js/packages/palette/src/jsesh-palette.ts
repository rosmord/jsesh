/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// `<jsesh-palette>`: JSesh's sign palette.
///
/// ```html
/// <jsesh-palette></jsesh-palette>
/// <script type="module">
///   document.querySelector("jsesh-palette").addEventListener("sign-chosen", (e) => console.log(e.detail));
/// </script>
/// ```
///
/// A grid of signs, each drawn with the JSesh font, and a panel describing the
/// selected sign. Double-click a sign, press Enter on it, or click *Choose*:
/// the element sends `sign-chosen`, with the code in `detail` (the event
/// bubbles and crosses the shadow DOM).
///
/// The sign database is loaded from `glyphs-url` (or, by default, from the
/// URL given to `configureJSesh`), unless the `database` property gives one.
/// Set `labels` to translate the texts, and `storage` to keep the latest signs
/// and the user palette elsewhere than in `localStorage`.
///
/// Style: `--jsesh-palette-border`, `--jsesh-palette-background`,
/// `--jsesh-palette-muted-background`, `--jsesh-palette-selected-background`,
/// `--jsesh-palette-selected-border`, `--jsesh-palette-grid-height`,
/// `--jsesh-palette-info-width`. Every control and zone has a `part` (`family`,
/// `show-all`, `tag`, `secondary-tag`, `search`, `contains`, `count`, `signs`,
/// `sign`, `info`, `picture`, `code`, `choose`, `variants`, `containing`,
/// `back`, `in-user-palette`...), so `jsesh-palette::part(tag) { display: none }`
/// removes the tag lists. The palette adapts to its width: in a narrow
/// container (a side panel), the information goes under the signs.
///
/// For a palette with a different look, use `PaletteController`, which has no
/// DOM, and draw it as you want.

import { loadSignDatabase, type SignDatabase } from "@jsesh/glyphs";
import { renderMdc, resolveGlyphsUrl } from "@jsesh/elements";
import { PaletteController, type PaletteState } from "./controller.js";
import { DEFAULT_PALETTE_LABELS, type PaletteLabels } from "./labels.js";
import type { PaletteStorage } from "./storage.js";

const STYLE = `
:host { display: block; container-type: inline-size; line-height: 1.4; }
[hidden] { display: none !important; }
.palette {
  border: 1px solid var(--jsesh-palette-border, #aaa);
  border-radius: 4px;
  padding: 0.5rem;
  background: var(--jsesh-palette-background, white);
}
.row { display: flex; flex-wrap: wrap; gap: 0.4rem; align-items: center; margin-bottom: 0.4rem; }
[data-palette="family"] { max-width: 22rem; }
.spacer { flex: 1; }
.body { display: grid; grid-template-columns: minmax(0, 1fr) var(--jsesh-palette-info-width, 18rem); gap: 0.5rem; }
@container (max-width: 40rem) {
  .body { grid-template-columns: 1fr; }
  [data-palette="info"] { border-left: none !important; padding-left: 0 !important; max-height: none !important; }
}
[data-palette="signs"] {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(4.2rem, 1fr));
  gap: 2px;
  height: var(--jsesh-palette-grid-height, 24rem);
  overflow-y: auto;
  align-content: start;
  background: var(--jsesh-palette-muted-background, #f4f4f4);
  padding: 2px;
}
.sign {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  height: 4.2rem;
  padding: 2px;
  background: var(--jsesh-palette-background, white);
  border: 1px solid transparent;
  cursor: pointer;
  font-size: 0.7rem;
}
.sign .picture { flex: 1; display: flex; align-items: center; justify-content: center; min-height: 0; width: 100%; }
.sign svg { max-width: 100%; max-height: 100%; height: auto; }
.sign:hover { border-color: #9bb5e0; }
.sign.selected {
  background: var(--jsesh-palette-selected-background, #dbe6f8);
  border-color: var(--jsesh-palette-selected-border, #4a7fd4);
}
[data-palette="info"] {
  border-left: 1px solid #ddd;
  padding-left: 0.5rem;
  font-size: 0.9rem;
  overflow: auto;
  max-height: var(--jsesh-palette-grid-height, 24rem);
}
.big { display: flex; align-items: center; gap: 0.75rem; }
.big [data-palette="code"] { font-size: 1.4rem; font-weight: 600; }
dl { display: grid; grid-template-columns: auto 1fr; gap: 0.2rem 0.5rem; margin: 0.5rem 0; }
dt { color: #666; }
dd { margin: 0; }
.code-link {
  display: inline-flex;
  align-items: center;
  gap: 0.2rem;
  margin: 0 0.2rem 0.2rem 0;
  padding: 0.1rem 0.3rem;
  font-size: 0.75rem;
  background: var(--jsesh-palette-background, white);
  border: 1px solid #ccc;
  border-radius: 3px;
  cursor: pointer;
}
[data-palette="description"] { overflow-x: auto; border-top: 1px solid #ddd; padding-top: 0.4rem; }
.message { padding: 0.5rem; }
`;

function template(l: PaletteLabels): string {
  return `
<div class="row" data-palette="filters">
  <select data-palette="family" aria-label="${esc(l.selectFamily)}"></select>
  <label><input type="checkbox" data-palette="show-all" /> ${esc(l.showAllVariants)}</label>
  <select data-palette="tag" aria-label="${esc(l.tags)}"></select>
  <select data-palette="secondary-tag" aria-label="${esc(l.tags)}"></select>
</div>
<div class="row" data-palette="search-row">
  <input data-palette="search" type="search" placeholder="${esc(l.searchPlaceholder)}" size="22" />
  <select data-palette="contains" aria-label="${esc(l.containingPlaceholder)}"></select>
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
      <button data-palette="choose">${esc(l.choose)}</button>
      <button data-palette="variants" title="${esc(l.variantsHint)}">${esc(l.variantsButton)}</button>
      <button data-palette="containing" title="${esc(l.containingHint)}">${esc(l.containingButton)}</button>
      <button data-palette="back" title="${esc(l.backHint)}">${esc(l.back)}</button>
    </div>
    <label><input type="checkbox" data-palette="in-user-palette" /> ${esc(l.inUserPalette)}</label>
    <dl>
      <dt>${esc(l.values)}</dt>
      <dd data-palette="values"></dd>
      <dt>${esc(l.tags)}</dt>
      <dd data-palette="tags"></dd>
      <dt>${esc(l.variants)}</dt>
      <dd data-palette="variant-list"></dd>
      <dt>${esc(l.contains)}</dt>
      <dd data-palette="parts"></dd>
    </dl>
    <div data-palette="description">
      <jsesh-text data-palette="description-text" cadrat-height="18"></jsesh-text>
    </div>
  </div>
</div>`;
}

function esc(text: string): string {
  return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/"/g, "&quot;");
}

/// Replaces the options of a select, unless they are the same.
function setOptions(select: HTMLSelectElement, options: readonly { value: string; label: string }[]): void {
  const key = options.map((o) => o.value + "\u0000" + o.label).join("\u0001");
  if (select.dataset.options !== key) {
    select.dataset.options = key;
    select.replaceChildren(...options.map((o) => new Option(o.label, o.value)));
  }
  select.disabled = options.length === 0;
}

export class JSeshPaletteElement extends HTMLElement {
  static readonly observedAttributes = ["glyphs-url"];

  private readonly root: ShadowRoot;
  private _database: SignDatabase | undefined;
  private _controller: PaletteController | undefined;
  private _labels: PaletteLabels = DEFAULT_PALETTE_LABELS;
  private _storage: PaletteStorage | undefined;
  private loading = 0;

  // The view.
  private ui = new Map<string, HTMLElement>();
  private observer: IntersectionObserver | undefined;
  private unsubscribe: (() => void) | undefined;
  private shownSigns: readonly string[] | undefined;
  private cells = new Map<string, HTMLElement>();
  private shownSelected: string | undefined;
  private shownInfoCode: string | undefined;

  constructor() {
    super();
    this.root = this.attachShadow({ mode: "open" });
  }

  // ---- Properties

  /// The sign database. If you don't set it, it is loaded from `glyphs-url`.
  get database(): SignDatabase | undefined {
    return this._database;
  }

  set database(database: SignDatabase | undefined) {
    this._database = database;
    if (this.isConnected) this.start();
  }

  /// The logic of the palette (available once the database is loaded).
  get controller(): PaletteController | undefined {
    return this._controller;
  }

  /// The texts of the palette. Set it before the database is loaded, or the palette is built again.
  get labels(): PaletteLabels {
    return this._labels;
  }

  set labels(labels: Partial<PaletteLabels>) {
    this._labels = { ...DEFAULT_PALETTE_LABELS, ...labels };
    if (this.isConnected) this.start();
  }

  /// Where the latest signs and the user palette are kept (`localStorage` by default).
  get storage(): PaletteStorage | undefined {
    return this._storage;
  }

  set storage(storage: PaletteStorage | undefined) {
    this._storage = storage;
    if (this.isConnected) this.start();
  }

  /// Chooses the selected sign, as the *Choose* button does.
  choose(code?: string): void {
    this._controller?.choose(code);
  }

  // ---- Life cycle

  connectedCallback(): void {
    this.start();
  }

  disconnectedCallback(): void {
    this.stop();
  }

  attributeChangedCallback(): void {
    // The database is loaded from the URL when the element is connected; a change of URL loads it again.
    if (this.isConnected && this.getAttribute("glyphs-url") !== null) {
      this._database = undefined;
      this.start();
    }
  }

  private stop(): void {
    this.unsubscribe?.();
    this.unsubscribe = undefined;
    this.observer?.disconnect();
    this.observer = undefined;
    this._controller = undefined;
  }

  /// (Re)builds the palette, loading the database if needed.
  private start(): void {
    this.stop();
    const loading = ++this.loading;
    if (this._database === undefined) {
      this.root.innerHTML = `<style>${STYLE}</style><div class="message">${esc(this._labels.loading)}</div>`;
      // Not at once: the page may call `configureJSesh` right after importing this module, as for <jsesh-text>.
      Promise.resolve()
        .then(() => loadSignDatabase(resolveGlyphsUrl(this.getAttribute("glyphs-url") ?? undefined)))
        .then(
          (database) => {
            if (loading !== this.loading) return;
            this._database = database;
            this.build(database);
          },
          (error: unknown) => {
            if (loading !== this.loading) return;
            this.root.innerHTML = `<style>${STYLE}</style><div class="message" role="alert">${esc(String(error))}</div>`;
            this.dispatchEvent(new CustomEvent("jsesh-error", { detail: error }));
          },
        );
    } else {
      this.build(this._database);
    }
  }

  private build(database: SignDatabase): void {
    const controller = new PaletteController(database, { storage: this._storage, labels: this._labels });
    this._controller = controller;
    this.root.innerHTML = `<style>${STYLE}</style><div class="palette" part="palette">${template(this._labels)}</div>`;
    this.ui = new Map();
    for (const element of this.root.querySelectorAll<HTMLElement>("[data-palette]")) {
      const name = element.dataset.palette!;
      this.ui.set(name, element);
      element.part.add(name);
    }
    this.shownSigns = undefined;
    this.shownSelected = undefined;
    this.shownInfoCode = undefined;
    this.cells = new Map();

    const $ = <T extends HTMLElement>(name: string): T => this.ui.get(name) as T;
    const l = this._labels;
    const family = $<HTMLSelectElement>("family");
    family.add(new Option(l.selectFamily, ""));
    for (const o of controller.familyOptions) family.add(new Option(o.label, o.value));

    this.observer = new IntersectionObserver((entries) => this.drawVisibleCells(entries), {
      root: $("signs"),
      rootMargin: "200px",
    });

    family.addEventListener("change", () => controller.setFamily(family.value));
    $<HTMLInputElement>("show-all").addEventListener("change", (e) =>
      controller.setShowAllVariants((e.target as HTMLInputElement).checked),
    );
    const tag = $<HTMLSelectElement>("tag");
    tag.addEventListener("change", () => controller.setTag(tag.value));
    const secondary = $<HTMLSelectElement>("secondary-tag");
    secondary.addEventListener("change", () => controller.setSecondaryTag(secondary.value));
    const search = $<HTMLInputElement>("search");
    search.addEventListener("keydown", (e) => {
      if (e.key === "Enter") controller.search(search.value);
    });
    const contains = $<HTMLSelectElement>("contains");
    contains.addEventListener("change", () => controller.filterContaining(contains.value));
    $("variants").addEventListener("click", () => controller.showVariants());
    $("containing").addEventListener("click", () => controller.showContaining());
    $("back").addEventListener("click", () => controller.back());
    $("choose").addEventListener("click", () => controller.choose());
    $<HTMLInputElement>("in-user-palette").addEventListener("change", (e) =>
      controller.setInUserPalette((e.target as HTMLInputElement).checked),
    );
    $("signs").addEventListener("keydown", (e) => this.gridKey(e as KeyboardEvent));

    controller.onSignChosen((code) =>
      this.dispatchEvent(new CustomEvent("sign-chosen", { detail: code, bubbles: true, composed: true })),
    );
    this.unsubscribe = controller.subscribe(() => this.update());
    this.update();
  }

  // ---- Showing the controller's state

  private update(): void {
    const controller = this._controller;
    if (controller === undefined) return;
    const s = controller.state;
    const $ = <T extends HTMLElement>(name: string): T => this.ui.get(name) as T;
    const l = this._labels;
    const plain = (values: readonly string[], empty = "") =>
      values.map((v) => ({ value: v, label: v === "" ? empty : v }));

    $<HTMLSelectElement>("family").value = s.family;
    $<HTMLInputElement>("show-all").checked = s.showAllVariants;
    setOptions($<HTMLSelectElement>("tag"), plain(s.tags));
    $<HTMLSelectElement>("tag").value = s.tag;
    setOptions($<HTMLSelectElement>("secondary-tag"), plain(s.secondaryTags));
    $<HTMLSelectElement>("secondary-tag").value = s.secondaryTag;
    setOptions($<HTMLSelectElement>("contains"), plain(s.containingOptions, l.containingPlaceholder));
    $<HTMLSelectElement>("contains").value = "";
    $("count").textContent = l.signCount(s.displayed.length);
    $<HTMLButtonElement>("back").disabled = !s.canGoBack;

    if (s.displayed !== this.shownSigns) this.showSigns(s.displayed);
    this.showSelected(s);
    this.showInfo(s);
  }

  private showSigns(codes: readonly string[]): void {
    this.shownSigns = codes;
    this.shownSelected = undefined;
    const grid = this.ui.get("signs")!;
    const controller = this._controller!;
    this.observer?.disconnect();
    grid.scrollTop = 0;
    this.cells = new Map();
    const cells = codes.map((code) => {
      const cell = document.createElement("button");
      cell.className = "sign";
      cell.part.add("sign");
      cell.dataset.code = code;
      cell.title = code;
      cell.tabIndex = -1;
      cell.setAttribute("role", "option");
      cell.innerHTML = `<span class="picture"></span><span class="code">${esc(code)}</span>`;
      cell.addEventListener("click", () => controller.select(code));
      cell.addEventListener("dblclick", () => controller.choose(code));
      this.cells.set(code, cell);
      this.observer?.observe(cell);
      return cell;
    });
    grid.replaceChildren(...cells);
  }

  private showSelected(s: PaletteState): void {
    if (s.selected === this.shownSelected) return;
    const grid = this.ui.get("signs")!;
    const inGrid = this.root.activeElement !== null && grid.contains(this.root.activeElement);
    const previous = this.shownSelected === undefined ? undefined : this.cells.get(this.shownSelected);
    if (previous !== undefined) {
      previous.classList.remove("selected");
      previous.part.remove("selected");
      previous.tabIndex = -1;
      previous.setAttribute("aria-selected", "false");
    }
    this.shownSelected = s.selected;
    const cell = s.selected === undefined ? undefined : this.cells.get(s.selected);
    if (cell !== undefined) {
      cell.classList.add("selected");
      cell.part.add("selected");
      cell.tabIndex = 0;
      cell.setAttribute("aria-selected", "true");
      cell.scrollIntoView({ block: "nearest" });
      if (inGrid) cell.focus();
    }
  }

  private showInfo(s: PaletteState): void {
    const $ = <T extends HTMLElement>(name: string): T => this.ui.get(name) as T;
    const info = s.info;
    $("info").hidden = info === undefined;
    if (info === undefined) {
      this.shownInfoCode = undefined;
      return;
    }
    $<HTMLInputElement>("in-user-palette").checked = info.inUserPalette;
    if (info.code === this.shownInfoCode) return;
    this.shownInfoCode = info.code;
    const controller = this._controller!;
    const l = this._labels;
    ($("picture") as HTMLElement & { mdc: string }).mdc = info.code;
    $("code").textContent = info.code;
    $("values").textContent = info.values.join(", ") || "—";
    $("tags").textContent = info.tags.join(", ") || "—";
    const link = (code: string, title: string): HTMLElement => {
      const button = document.createElement("button");
      button.className = "code-link";
      button.part.add("code-link");
      button.title = `${code} (${title})`;
      button.innerHTML = `<jsesh-text cadrat-height="22">${esc(code)}</jsesh-text>${esc(code)}`;
      button.addEventListener("click", () => controller.reveal(code));
      return button;
    };
    $("variant-list").replaceChildren(...info.variants.map((v) => link(v.code, v.typeLabel)));
    $("parts").replaceChildren(...info.parts.map((c) => link(c, l.part)));
    $("description").hidden = info.description === "";
    ($("description-text") as HTMLElement & { mdc: string }).mdc = info.description;
  }

  /// Signs are drawn when they become visible.
  private drawVisibleCells(entries: IntersectionObserverEntry[]): void {
    for (const entry of entries) {
      if (!entry.isIntersecting) continue;
      const cell = entry.target as HTMLElement;
      this.observer?.unobserve(cell);
      const picture = cell.firstElementChild!;
      renderMdc(cell.dataset.code!, { cadratHeight: 34 }).then(
        (svg) => (picture.innerHTML = svg),
        () => (picture.textContent = "?"),
      );
    }
  }

  private gridKey(e: KeyboardEvent): void {
    const controller = this._controller!;
    const grid = this.ui.get("signs")!;
    const first = grid.firstElementChild as HTMLElement | null;
    const perRow = Math.max(1, Math.round(grid.clientWidth / (first?.offsetWidth || 1)));
    const moves: Record<string, number> = { ArrowRight: 1, ArrowLeft: -1, ArrowDown: perRow, ArrowUp: -perRow };
    if (e.key in moves) controller.move(moves[e.key]);
    else if (e.key === "Enter" || e.key === " ") controller.choose();
    else return;
    e.preventDefault();
  }
}
