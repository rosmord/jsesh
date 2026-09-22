package jsesh.parser.ast;

import jsesh.model.api.HieroglyphInterface;
import jsesh.model.constants.WordEndingCode;

/**
 * Mutable, transient stand-in for a {@link AstHieroglyph}, used only inside
 * {@link AstBuilder}.
 * <p>The grammar builds a hieroglyph, then <em>optionally</em> attaches an
 * explicit position to it (via {@code setHieroglyphPosition}) before it is
 * consumed by whatever contains it. Since {@link AstHieroglyph} is an
 * immutable record, that one-shot "maybe patch a field, then finish" step
 * needs a mutable holder; this is it. It is never exposed outside this
 * package — every place that receives a {@code HieroglyphInterface} turns it
 * into a real {@link AstHieroglyph} via {@link #toAstHieroglyph()}.
 *
 * @author rosmord
 */
final class HieroglyphDraft implements HieroglyphInterface {

    private final boolean isGrammar;
    private final int type;
    private final String code;
    private final AstModifierList modifiers;
    private final WordEndingCode endingCode;
    private int x;
    private int y;
    private int scale = 100;

    HieroglyphDraft(boolean isGrammar, int type, String code, AstModifierList modifiers,
            WordEndingCode endingCode) {
        this.isGrammar = isGrammar;
        this.type = type;
        this.code = code;
        this.modifiers = modifiers;
        this.endingCode = endingCode;
    }

    void setPosition(int x, int y, int scale) {
        this.x = x;
        this.y = y;
        this.scale = scale;
    }

    AstHieroglyph toAstHieroglyph() {
        return new AstHieroglyph(isGrammar, type, code, modifiers, endingCode, x, y, scale);
    }
}
