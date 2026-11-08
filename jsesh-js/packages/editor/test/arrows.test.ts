/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

import { describe, expect, it } from "vitest";
import { LogicalArrowKeyPolicy, VisualArrowKeyPolicy } from "../src/arrows.js";
import { EditorSession } from "../src/session.js";

function session(mdc: string): EditorSession {
  const s = new EditorSession();
  s.setMdc(mdc);
  s.moveCaret(1);
  return s;
}

describe("logical arrows (default)", () => {
  it("moves the cursor logically, whatever the direction of the text", () => {
    for (const options of [{}, { textDirection: "RIGHT_TO_LEFT" }, { textOrientation: "VERTICAL" }] as const) {
      const s = session("n-p-d");
      s.setOptions(options);
      s.moveCaret(1);
      s.handleKey({ key: "ArrowRight" });
      expect(s.state.caret).toBe(2);
      s.handleKey({ key: "ArrowLeft" });
      expect(s.state.caret).toBe(1);
    }
  });

  it("swaps the horizontal arrows if the right arrow is backward", () => {
    const s = session("n-p-d");
    s.arrowPolicy = new LogicalArrowKeyPolicy("rtl");
    s.handleKey({ key: "ArrowRight" });
    expect(s.state.caret).toBe(0);
    s.handleKey({ key: "ArrowLeft" });
    expect(s.state.caret).toBe(1);
  });

  it("uses Up and Down inside a column in column mode", () => {
    const s = session("n-p-d");
    s.arrowPolicy = new LogicalArrowKeyPolicy("ltr", "vertical");
    s.handleKey({ key: "ArrowDown" });
    expect(s.state.caret).toBe(2);
    s.handleKey({ key: "ArrowUp" });
    expect(s.state.caret).toBe(1);
    s.handleKey({ key: "ArrowRight" });
    expect(s.state.caret).toBe(1);
  });
});

describe("visual arrows", () => {
  it("reverses the arrows in right-to-left text", () => {
    const s = new EditorSession();
    s.arrowPolicy = new VisualArrowKeyPolicy();
    s.setOptions({ textDirection: "RIGHT_TO_LEFT" });
    s.setMdc("a-b-c");
    s.handleKey({ key: "Home" });
    s.handleKey({ key: "ArrowLeft" });
    expect(s.state.caret).toBe(1);
  });

  it("uses Down and Up as well in vertical text", () => {
    const s = session("a-b-c");
    s.arrowPolicy = new VisualArrowKeyPolicy();
    s.setOptions({ textOrientation: "VERTICAL" });
    s.moveCaret(1);
    s.handleKey({ key: "ArrowDown" });
    expect(s.state.caret).toBe(2);
    s.handleKey({ key: "ArrowRight" });
    expect(s.state.caret).toBe(3);
  });
});
