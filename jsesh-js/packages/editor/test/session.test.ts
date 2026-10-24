/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

import { describe, expect, it } from "vitest";
import { caretArea, positionAt, type Area } from "../src/geometry.js";
import { EditorSession } from "../src/session.js";

/// Types a string as the editor would receive it.
function type(session: EditorSession, keys: string): void {
  for (const key of keys) session.handleKey({ key });
}

describe("EditorSession typing", () => {
  it("makes a quadrat per sign when separated by - or space", () => {
    const s = new EditorSession();
    type(s, "nfr-A1 ");
    expect(s.state.mdc).toBe("nfr-A1");
    expect(s.state.length).toBe(2);
    expect(s.state.caret).toBe(2);
  });

  it("stacks with ':'", () => {
    const s = new EditorSession();
    type(s, "p:t ");
    expect(s.state.mdc).toBe("p:t");
    expect(s.state.length).toBe(1);
  });

  it("makes a ligature with '&' and a line break with '!'", () => {
    const s = new EditorSession();
    type(s, "a&b !");
    expect(s.state.mdc.trim()).toBe("a&b-!");
  });

  it("shows the code being typed, and erases it with Backspace", () => {
    const s = new EditorSession();
    type(s, "nf");
    expect(s.state.code).toBe("nf");
    s.handleKey({ key: "Backspace" });
    expect(s.state.code).toBe("n");
    s.handleKey({ key: "Escape" });
    expect(s.state.code).toBe("");
  });

  it("reports an unreadable code without changing the text", () => {
    const s = new EditorSession();
    type(s, "nfr ");
    s.typeCharacter("(");
    s.handleKey({ key: " " });
    expect(s.state.error).toMatch(/Can't read/);
    expect(s.state.mdc).toBe("nfr");
  });

  it("inserts a sign as if typed, grouping with the previous separator", () => {
    const s = new EditorSession();
    type(s, "p:");
    s.insertSign("t");
    expect(s.state.mdc).toBe("p:t");
  });
});

describe("EditorSession editing", () => {
  it("undoes and redoes", () => {
    const s = new EditorSession();
    s.setMdc("nfr-A1");
    s.handleKey({ key: "Backspace" });
    expect(s.state.mdc).toBe("nfr");
    expect(s.state.canUndo).toBe(true);
    s.undo();
    expect(s.state.mdc).toBe("nfr-A1");
    expect(s.state.canRedo).toBe(true);
    s.redo();
    expect(s.state.mdc).toBe("nfr");
  });

  it("moves the cursor and selects with shift", () => {
    const s = new EditorSession();
    s.setMdc("a-b-c");
    s.handleKey({ key: "Home" });
    s.handleKey({ key: "ArrowRight", shiftKey: true });
    s.handleKey({ key: "ArrowRight", shiftKey: true });
    expect(s.state).toMatchObject({ caret: 2, selection: 2, selectionStart: 0, selectionEnd: 2 });
    s.handleKey({ key: "Delete" });
    expect(s.state.mdc).toBe("c");
  });

  it("reverses the arrows in right-to-left text", () => {
    const s = new EditorSession();
    s.setOptions({ textDirection: "RIGHT_TO_LEFT" });
    s.setMdc("a-b-c");
    s.handleKey({ key: "Home" });
    s.handleKey({ key: "ArrowLeft" });
    expect(s.state.caret).toBe(1);
  });

  it("groups the selection, makes cartouches and toggles red", () => {
    const s = new EditorSession();
    s.setMdc("p-t");
    s.group(":");
    expect(s.state.mdc).toBe("p:t");
    s.cartouche();
    expect(s.state.mdc).toContain("<");
    s.undo();
    s.toggleRed();
    expect(s.state.mdc).toContain("$r");
  });

  it("leaves browser shortcuts alone", () => {
    const s = new EditorSession();
    expect(s.handleKey({ key: "c", metaKey: true })).toBe(false);
    expect(s.handleKey({ key: "F5" })).toBe(false);
  });
});

describe("EditorSession observers", () => {
  it("notifies, and caches the state between changes", () => {
    const s = new EditorSession();
    const changes: boolean[] = [];
    const stop = s.subscribe((c) => changes.push(c.layout));
    s.setMdc("a-b");
    const state = s.state;
    expect(s.state).toBe(state);
    s.handleKey({ key: "ArrowLeft" });
    expect(s.state).not.toBe(state);
    expect(changes).toEqual([true, false]);
    stop();
    s.undo();
    expect(changes.length).toBe(2);
  });
});

describe("geometry", () => {
  const areas: Area[] = [
    { x: 0, y: 0, width: 10, height: 20 },
    { x: 10, y: 0, width: 10, height: 20 },
  ];
  const flow = { vertical: false, rightToLeft: false };

  it("finds the position closest to a point", () => {
    expect(positionAt(areas, 1, 5, flow)).toBe(0);
    expect(positionAt(areas, 8, 5, flow)).toBe(1);
    expect(positionAt(areas, 100, 5, flow)).toBe(2);
    expect(positionAt([], 3, 3, flow)).toBe(0);
  });

  it("flips the halves in right-to-left text", () => {
    expect(positionAt(areas, 1, 5, { vertical: false, rightToLeft: true })).toBe(1);
  });

  it("places the caret before the next item, or after the last one", () => {
    expect(caretArea(areas, 1, flow, 20, 6)).toMatchObject({ x: 8, y: 0 });
    expect(caretArea(areas, 2, flow, 20, 6)).toMatchObject({ x: 20 });
    expect(caretArea([], 0, flow, 20, 6)).toMatchObject({ x: 6, y: 6, height: 20 });
  });
});
