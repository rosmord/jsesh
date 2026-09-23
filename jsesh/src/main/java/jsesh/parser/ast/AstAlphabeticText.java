package jsesh.parser.ast;

/**
 * Text in an alphabetic script (Latin, transliteration, Coptic, Greek,
 * Hebrew, Cyrillic...), either at top level or inside a basic item list.
 *
 * @param scriptCode 'l' latin, 'b' bold, 'i' italic, 't' transliteration,
 * 'c' coptic, 'g' greek, 'h' hebrew, 'r' cyrillic.
 * @param text the text, exactly as parsed.
 * @author rosmord
 */
public record AstAlphabeticText(char scriptCode, String text) implements AstNode {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitAlphabeticText(this);
    }
}
