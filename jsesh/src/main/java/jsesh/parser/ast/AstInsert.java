package jsesh.parser.ast;

import java.util.HashMap;
import java.util.Map;

/// An insertion, inspired by Mark-Jan Nederhof's work on RES.
/// Used in particular to cover the possibilities of the Unicode insertion operators.
/// 
/// @param direction how the inserted element is placed relative to the main one.
/// @param main the main element.
/// @param inserted the element to be inserted.

public record AstInsert(
    InsertDirection direction, AstOptionList options,
    AstBasicItemList mainItems, AstBasicItemList insertedItems
) implements AstInnerGroup {
    
    /// Where the inserted element is placed relative to the main one.
    /// Each direction has a short code, as used in the MDC syntax.
    public static enum InsertDirection {
        START("s"),
        END("e"),
        TOP("t"),
        BOTTOM("b"),
        START_TOP("st"),
        START_BOTTOM("sb"),
        END_TOP("et"),
        END_BOTTOM("eb"),
        CENTER("c");

        private static final Map<String, InsertDirection> BY_CODE = new HashMap<>();

        static {
            for (InsertDirection d : values()) {
                BY_CODE.put(d.code, d);
            }
        }

        private final String code;

        private InsertDirection(String code) {
            this.code = code;
        }

        /// The short code for this direction (e.g. "st").
        public String getCode() {
            return code;
        }

        /// Find the direction corresponding to a code.
        ///
        /// @param code one of "s", "e", "t", "b", "st", "sb", "et", "eb", "c"
        /// @return the matching direction, or null if the code is unknown.
        public static InsertDirection fromCode(String code) {
            return BY_CODE.get(code);
        }
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitInsert(this);
    }
}
