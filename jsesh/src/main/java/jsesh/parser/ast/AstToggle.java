package jsesh.parser.ast;

import jsesh.model.constants.ToggleType;

/**
 * A toggle marker (e.g. {@code $r}, {@code $b}, {@code #}), either at top
 * level or inside a basic item list.
 * <p>Unlike {@link jsesh.model.MDCModelBuilder}, which folds toggles into
 * hidden red/shaded state on the items that follow, this AST keeps each
 * toggle as its own node, in the position where it was parsed.
 *
 * @author rosmord
 */
public record AstToggle(ToggleType toggleType) implements AstNode {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitToggle(this);
    }
}
