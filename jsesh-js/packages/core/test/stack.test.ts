/// `%stack(g1,g2)`: the model, the writer and the layout. The comparison with Java's output is
/// done through the exported fixtures (see `parser-cases.json`).
import { describe, expect, it } from "vitest";
import { parseMdc } from "../src/mdcreader/builder.js";
import { Stack, type HieroglyphicText, type ModelElement } from "../src/model/model.js";
import { toMdc } from "../src/writer/writer.js";

function stacks(text: HieroglyphicText): Stack[] {
  const found: Stack[] = [];
  const walk = (e: ModelElement): void => {
    if (e instanceof Stack) found.push(e);
    for (let i = 0; i < e.numberOfChildren; i++) walk(e.getChildAt(i));
  };
  walk(text);
  return found;
}

describe("%stack", () => {
  it("is built from MdC", () => {
    const stack = stacks(parseMdc("i-%stack(O1:n,t*w)-w"))[0]!;
    expect(stack.first.basicItemList.numberOfChildren).toBe(1);
    expect(stack.second.basicItemList.numberOfChildren).toBe(1);
  });

  it.each([
    "%stack(O1,n)",
    "i-%stack(O1:n,t*w)-w",
    "%stack(O1-n:t,x)",
    "%stack(%stack(O1,n),t)",
  ])("is written back unchanged: %s", (mdc) => {
    expect(toMdc(parseMdc(mdc))).toBe(mdc);
  });

  it.each(["%stack(A1)", "%stack(A1,B1"])("rejects %s", (mdc) => {
    expect(() => parseMdc(mdc)).toThrow();
  });
});
