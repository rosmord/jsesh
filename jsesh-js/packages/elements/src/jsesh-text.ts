/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// `<jsesh-text>`: displays a hieroglyphic text.
///
/// ```html
/// <jsesh-text>i-n:n-A1</jsesh-text>
/// <jsesh-text mdc="i-n:n-A1" cadrat-height="40" direction="rtl" orientation="vertical"></jsesh-text>
/// ```
///
/// The text is the `mdc` attribute (or property), or else the element's
/// content. It can be a whole JSesh document, with its header.
///
/// Attributes: `cadrat-height` (height of a quadrat in pixels; by default,
/// 1.5 times the font size), `direction` (`ltr` or `rtl`), `orientation`
/// (`horizontal` or `vertical`), `centered` (center small signs),
/// `justified`, `glyphs-url`. The `jseshStyle` property takes any style change.
///
/// Events: `jsesh-render` once the text is displayed, `jsesh-error` if it
/// could not be (with the error as `detail`).

import type { StyleChanges } from "@jsesh/core";
import { drawLaidOutText } from "@jsesh/core";
import { layOutInPage } from "./config.js";

const STYLE = `:host{display:inline-block;vertical-align:middle;line-height:0}svg{display:block}`;

let instances = 0;

export class JSeshTextElement extends HTMLElement {
  static readonly observedAttributes = [
    "mdc",
    "cadrat-height",
    "direction",
    "orientation",
    "centered",
    "justified",
    "glyphs-url",
  ];

  private readonly root: ShadowRoot;
  private readonly idPrefix = `jsesh-text${instances++}`;
  private _style: StyleChanges | undefined;
  private renderPending = false;
  private generation = 0;
  private contentObserver: MutationObserver | undefined;

  constructor() {
    super();
    this.root = this.attachShadow({ mode: "open" });
  }

  /// The MdC text: the `mdc` attribute, or the element's content.
  get mdc(): string {
    return this.getAttribute("mdc") ?? this.textContent ?? "";
  }

  set mdc(value: string) {
    this.setAttribute("mdc", value);
  }

  /// Changes to the style, on top of the page's style and the attributes.
  get jseshStyle(): StyleChanges | undefined {
    return this._style;
  }

  set jseshStyle(style: StyleChanges | undefined) {
    this._style = style;
    this.scheduleRender();
  }

  connectedCallback(): void {
    // The content may still be parsed, or change.
    this.contentObserver ??= new MutationObserver(() => {
      if (!this.hasAttribute("mdc")) this.scheduleRender();
    });
    this.contentObserver.observe(this, { childList: true, characterData: true, subtree: true });
    this.scheduleRender();
  }

  disconnectedCallback(): void {
    this.contentObserver?.disconnect();
  }

  attributeChangedCallback(): void {
    this.scheduleRender();
  }

  private scheduleRender(): void {
    if (this.renderPending || !this.isConnected) return;
    this.renderPending = true;
    queueMicrotask(() => {
      this.renderPending = false;
      void this.render();
    });
  }

  private styleChanges(): StyleChanges {
    const options: NonNullable<StyleChanges["options"]> = {};
    const direction = this.getAttribute("direction");
    if (direction !== null) options.textDirection = direction.toLowerCase() === "rtl" ? "RIGHT_TO_LEFT" : "LEFT_TO_RIGHT";
    const orientation = this.getAttribute("orientation");
    if (orientation !== null) options.textOrientation = orientation.toLowerCase() === "vertical" ? "VERTICAL" : "HORIZONTAL";
    if (this.hasAttribute("centered")) options.smallSignCentered = true;
    if (this.hasAttribute("justified")) options.justified = true;
    return {
      ...this._style,
      options: { ...this._style?.options, ...options },
    };
  }

  private cadratHeight(): number {
    const attribute = Number.parseFloat(this.getAttribute("cadrat-height") ?? "");
    if (Number.isFinite(attribute) && attribute > 0) return attribute;
    const fontSize = Number.parseFloat(getComputedStyle(this).fontSize);
    return Number.isFinite(fontSize) ? fontSize * 1.5 : 24;
  }

  private async render(): Promise<void> {
    const generation = ++this.generation;
    const mdc = this.mdc.trim();
    try {
      const laidOut = await layOutInPage(mdc, {
        style: this.styleChanges(),
        glyphsUrl: this.getAttribute("glyphs-url") ?? undefined,
      });
      if (generation !== this.generation) return;
      const svg = drawLaidOutText(laidOut, { cadratHeight: this.cadratHeight(), title: mdc, idPrefix: this.idPrefix });
      this.root.innerHTML = `<style>${STYLE}</style>${svg}`;
      this.dispatchEvent(new CustomEvent("jsesh-render"));
    } catch (error) {
      if (generation !== this.generation) return;
      this.root.innerHTML = `<style>${STYLE}</style><slot></slot>`;
      this.dispatchEvent(new CustomEvent("jsesh-error", { detail: error }));
    }
  }
}
