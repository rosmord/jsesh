/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */

/// Useless sub-cadrats are syntax: `i-(mn:n)`, `a:(b:c)` and `x*(a*b)` are read and written
/// as `i-mn:n`, `a:b:c` and `x*a*b` (mirrors Java's `MdCModelWriterSubCadratTest`).

import { describe, expect, it } from "vitest";
import { parseMdc } from "../src/mdcreader/builder.js";
import { toMdc } from "../src/writer/writer.js";

describe("useless sub-cadrats", () => {
  it.each([
    ["i-(mn:n)", "i-mn:n"],
    ["a:(b:c)", "a:b:c"],
    ["x*(a*b)", "x*a*b"],
    ["a:(b*c:d)", "a:b*c:d"],
    ["a:((b:c))", "a:b:c"],
    ["x*(a*(b*c))", "x*a*b*c"],
    ["a:(b:(c:d))", "a:b:c:d"],
    ["n:(x:t)*(a*b)", "n:(x:t)*a*b"],
  ])("%s is written %s", (mdc, expected) => {
    expect(toMdc(parseMdc(mdc)).trim()).toBe(expected);
  });

  it.each(["n:(x:t)*nxt", "x*(a:b)", "a*(b:c)*d", "a:(b:c)*t", "(a:b)^^w"])("%s keeps its sub-cadrat", (mdc) => {
    expect(toMdc(parseMdc(mdc)).trim()).toContain("(");
  });
});
