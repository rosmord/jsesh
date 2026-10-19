package jsesh.parser.ast;

import java.util.List;

/// A Rectangular zone containing text.
/// 
/// It can be text in lines or in columns.
/// @param options the raw options given between braces, or their default values.
/// @param list the content of the zone, as a list of top items.
/// @author rosmord
/// 
public record AstZone(AstOptionList options, AstTopItemList list) implements AstNode {

    public AstZone (AstOptionList options, List<AstNode> list) {
        this(options, AstTopItemList.of(list.toArray(new AstNode[0])));
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitZone(this);
    }
    
}
