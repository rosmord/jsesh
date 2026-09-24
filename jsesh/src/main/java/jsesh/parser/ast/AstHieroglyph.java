package jsesh.parser.ast;

import jsesh.parser.lexer.SignSubType;

/**
 * A single sign, in context: a Gardiner-ish code, its modifiers, and whether
 * it marks a word or sentence end.
 *
 * @param isGrammar true if the sign is part of a grammatical ending.
 * @param type distinguishes real hieroglyphs ({@link SignSubType.Plain#MDC_CODE})
 * from other symbols, such as shading or red points, or philological brackets
 * read as signs.
 * @param code the raw Manuel de Codage code for this sign, exactly as typed
 * (not canonicalized).
 * @param x explicit x position, in 1/1000 of the height of an A1 sign; only
 * meaningful when set through "{{x,y,scale}}", typically in an absolute group.
 * @param y explicit y position, same unit as {@code x}.
 * @param scale explicit scale, in percent (100 is the natural size).
 * @author rosmord
 */
public record AstHieroglyph(boolean isGrammar, SignSubType type, String code, AstModifierList modifiers,
        WordEnding endingCode, int x, int y, int scale) implements AstInnerGroup {

    /**
     * A plain sign with the given code: no grammar flag, no modifiers, no
     * word/sentence end, no explicit position. For tests.
     */
    public static AstHieroglyph of(String code) {
        return builder(code).build();
    }

    /**
     * Same as {@link #of(String)}, with modifiers. For tests.
     */
    public static AstHieroglyph of(String code, AstModifierList modifiers) {
        return builder(code).modifiers(modifiers).build();
    }

    public static Builder builder(String code) {
        return new Builder(code);
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitHieroglyph(this);
    }

    /**
     * Fluent construction of an {@link AstHieroglyph} for tests, covering
     * the grammar flag, sign type, word/sentence end and explicit position
     * that {@link #of} alone cannot express.
     */
    public static final class Builder {

        private final String code;
        private boolean isGrammar;
        private SignSubType type = SignSubType.Plain.MDC_CODE;
        private AstModifierList modifiers = AstModifierList.of();
        private WordEnding endingCode = WordEnding.NONE;
        private int x;
        private int y;
        private int scale = 100;

        private Builder(String code) {
            this.code = code;
        }

        /**
         * Marks the sign as part of a grammatical ending (Manuel de Codage "=").
         */
        public Builder grammar() {
            isGrammar = true;
            return this;
        }

        public Builder type(SignSubType type) {
            this.type = type;
            return this;
        }

        public Builder modifiers(AstModifierList modifiers) {
            this.modifiers = modifiers;
            return this;
        }

        public Builder wordEnd() {
            endingCode = WordEnding.WORD_END;
            return this;
        }

        public Builder sentenceEnd() {
            endingCode = WordEnding.SENTENCE_END;
            return this;
        }

        public Builder position(int x, int y, int scale) {
            this.x = x;
            this.y = y;
            this.scale = scale;
            return this;
        }

        public AstHieroglyph build() {
            return new AstHieroglyph(isGrammar, type, code, modifiers, endingCode, x, y, scale);
        }
    }
}
