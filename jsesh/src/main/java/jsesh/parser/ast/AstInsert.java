package jsesh.parser.ast;

import jsesh.sharedvocabulary.InsertionDirection;

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
    InsertionDirection direction, int target, AstOptionList options,
    AstBasicItemList firstItems, AstBasicItemList secondItems
) implements AstInnerGroup {
    
    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitInsert(this);
    }
}
