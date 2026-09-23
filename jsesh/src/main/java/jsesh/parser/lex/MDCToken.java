package jsesh.parser.lex;

/**
 * A token produced by {@link MDCLex} for {@link jsesh.parser.handmade.MDCHandmadeParser}:
 * a symbol code ({@link MDCSymbols}) plus its lexical value, if any.
 * <p>Replaces the {@code java_cup.runtime.Symbol} the parser used back when it was
 * CUP-generated; only the {@code sym}/{@code value} fields it actually read were kept.
 */
public class MDCToken {

    public final int sym;
    public final Object value;

    public MDCToken(int sym) {
        this(sym, null);
    }

    public MDCToken(int sym, Object value) {
        this.sym = sym;
        this.value = value;
    }

    @Override
    public String toString() {
        return "#" + sym;
    }
}
