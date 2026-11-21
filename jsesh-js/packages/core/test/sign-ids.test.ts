import { describe, expect, it } from "vitest";
import { readMdcDocument, writeMdcDocument } from "../src/document/document.js";

describe("sign ids (\\sid)", () => {
  it("keeps the \\sid modifiers and the max id in a round trip", () => {
    const source = "++JSesh_Info 2.0 +s\n++JSesh_max_sign_id 4 +s\np\\sid2*t\\sid3:pt\\sid4";
    const document = readMdcDocument(source, { failFast: true });
    expect(document.maxSignId).toBe(4);
    const written = writeMdcDocument(document);
    expect(written).toContain("++JSesh_max_sign_id 4 +s");
    expect(written).toContain("p\\sid2*t\\sid3:pt\\sid4");
  });

  it("has no max id for a document which is not numbered", () => {
    const document = readMdcDocument("++JSesh_Info 2.0 +s\np-t", { failFast: true });
    expect(document.maxSignId).toBeUndefined();
    expect(writeMdcDocument(document)).not.toContain("max_sign_id");
  });
});
