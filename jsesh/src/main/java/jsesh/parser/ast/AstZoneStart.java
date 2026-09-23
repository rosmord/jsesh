package jsesh.parser.ast;

import jsesh.model.api.ZoneStartInterface;

/**
 * A zone marker, introducing a new drawing zone (position, dimensions,
 * text orientation and direction), given as options: {@code zone[...]}.
 * <p>{@link jsesh.parser.AstModelBuilder} drops the zone marker from the
 * model entirely (it never fills in its options); here the raw option list
 * is kept as parsed.
 *
 * @param options the raw options given between braces, or {@code null} if
 * the zone marker had none (bare {@code zone}).
 * @author rosmord
 */
public record AstZoneStart(AstOptionList options) implements AstNode, ZoneStartInterface {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitZoneStart(this);
    }
}
