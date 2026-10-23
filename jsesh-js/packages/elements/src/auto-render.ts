/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Replaces MdC text in a page with hieroglyphs, without custom elements:
///
/// ```html
/// <span class="mdc">i-n:n-A1</span>
/// <script type="module">
///   import { autoRender } from "@jsesh/elements";
///   autoRender();
/// </script>
/// ```

import type { StyleChanges } from "@jsesh/core";
import { renderMdc } from "./config.js";

export interface AutoRenderOptions {
  /// Class of the elements to process (default `mdc`). Ignored if `selector` is given.
  className?: string;
  /// A CSS selector for the elements to process, for cases `className` can't express
  /// (default: `.` followed by `className`).
  selector?: string;
  /// Where to look (default: the whole document).
  root?: ParentNode;
  /// Also process the elements added later to the page.
  observe?: boolean;
  /// Height of a quadrat in pixels (default: 1.5 times the element's font size).
  cadratHeight?: number;
  style?: StyleChanges;
}

/// Default class of the elements to process.
export const DEFAULT_CLASS = "mdc";

/// Class added to the elements once processed.
export const PROCESSED_CLASS = "hieroglyphs-processed";

/// Processes the matching elements: their text (MdC) is replaced by an SVG
/// picture, and kept in their `data-mdc` attribute. Returns an object to
/// stop observing the page.
export function autoRender(options: AutoRenderOptions = {}): { done: Promise<void>; disconnect(): void } {
  const selector = options.selector ?? `.${CSS.escape(options.className ?? DEFAULT_CLASS)}`;
  const root = options.root ?? document;
  const processAll = () =>
    Promise.all(
      Array.from(root.querySelectorAll<HTMLElement>(selector))
        .filter((e) => !e.classList.contains(PROCESSED_CLASS))
        .map((e) => processElement(e, options)),
    ).then(() => undefined);
  const done = processAll();
  let observer: MutationObserver | undefined;
  if (options.observe) {
    observer = new MutationObserver(() => void processAll());
    observer.observe(root instanceof Document ? root.documentElement : (root as Node), { childList: true, subtree: true });
  }
  return { done, disconnect: () => observer?.disconnect() };
}

async function processElement(element: HTMLElement, options: AutoRenderOptions): Promise<void> {
  element.classList.add(PROCESSED_CLASS);
  const mdc = (element.textContent ?? "").trim();
  const fontSize = Number.parseFloat(getComputedStyle(element).fontSize);
  try {
    const svg = await renderMdc(mdc, {
      style: options.style,
      cadratHeight: options.cadratHeight ?? (Number.isFinite(fontSize) ? fontSize * 1.5 : 24),
    });
    element.dataset.mdc = mdc;
    element.innerHTML = svg;
    const picture = element.firstElementChild as SVGElement | null;
    picture?.style.setProperty("vertical-align", "middle");
  } catch (error) {
    console.error(`Could not display ${mdc}`, error);
  }
}
