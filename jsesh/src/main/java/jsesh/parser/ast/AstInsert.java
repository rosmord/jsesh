package jsesh.parser.ast;

import java.util.HashMap;
import java.util.Map;

/// An insertion, inspired by Mark-Jan Nederhof's work on RES.
/// Used in particular to cover the possibilities of the Unicode insertion operators.
/// 
/// The two elements are kept in the order of the text. The `target` option says
/// which of them is the target (the one the other is inserted into).
///
/// @param direction how the inserted element is placed relative to the target.
/// @param target which argument is the target: 1 (the default) or 2.
/// @param options the other options (`dw`, `dh`, ...); `dir` and `target` are not among them.
/// @param firstItems the first argument, which may be a whole group (including another insertion).
/// @param secondItems the second argument, which may be a whole group.

public record AstInsert(
    InsertDirection direction, int target, AstOptionList options,
    AstBasicItemList firstItems, AstBasicItemList secondItems
) implements AstInnerGroup {
    
    /// Where the inserted element is placed relative to the main one.
    /// Each direction has a short code, as used in the MDC syntax.
    public static enum InsertDirection {
        START("s"),
        END("e"),
        TOP("t"),
        BOTTOM("b"),
        TOP_START("ts"),
        BOTTOM_START("bs"),
        TOP_END("te"),
        BOTTOM_END("be"),
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
        /// @param code one of "s", "e", "t", "b", "ts", "bs", "te", "be", "c"
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
