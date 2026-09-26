package jsesh.parser.ast;

import java.util.List;


/**
 * A {@code [name, name=value, ...]} option list, attached to a cadrat, a
 * zone marker or a tabbing construct.
 * <p>Unlike {@link jsesh.model.OptionsMap}, which stores options in a map (so
 * a repeated name silently overwrites the earlier one, and order is lost),
 * this keeps every entry exactly as parsed, in order. {@link #getString} and
 * {@link #getInt} still give the "last one wins" lookup a map would give.
 * <p>Not part of the sealed {@link AstNode} hierarchy (like
 * {@link jsesh.model.OptionsMap}, it is an attribute bag attached to a node,
 * not a node in its own right). Built incrementally via a left-recursive
 * grammar rule of unbounded arity, hence the {@link #of} factory rather than
 * a fixed-arity constructor call at each parse site.
 *
 * @author rosmord
 */
public record AstOptionList(List<AstOption> options) {

    public AstOptionList {
        options = List.copyOf(options);
    }

    public boolean hasOption(String name) {
        for (AstOption option : options) {
            if (option.name().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public String getString(String name, String defaultValue) {
        String result = defaultValue;
        for (AstOption option : options) {
            if (option.name().equals(name) && option.value() instanceof String) {
                result = (String) option.value();
            }
        }
        return result;
    }

    public int getInt(String name, int defaultValue) {
        int result = defaultValue;
        for (AstOption option : options) {
            if (option.name().equals(name) && option.value() instanceof Integer) {
                result = (Integer) option.value();
            }
        }
        return result;
    }

    /**
     * Hand-builds an option list, for tests. See {@link AstOption#flag} and
     * {@link AstOption#of}.
     */
    public static AstOptionList of(AstOption... options) {
        return new AstOptionList(List.of(options));
    }
}
