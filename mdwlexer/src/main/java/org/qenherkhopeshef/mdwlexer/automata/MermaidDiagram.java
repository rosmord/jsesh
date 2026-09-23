package org.qenherkhopeshef.mdwlexer.automata;

/// Builds the text of a Mermaid flowchart describing an automaton.
/// States are circles, accepting states are double circles labelled with their token type,
/// and epsilon transitions are dashed.
final class MermaidDiagram {
    private final StringBuilder out = new StringBuilder("flowchart LR\n");

    void initialState(State state) {
        out.append("    start([\"start\"]) --> ").append(id(state)).append('\n');
    }

    void state(State state, Object acceptedTokenType) {
        out.append("    ").append(id(state));
        if (acceptedTokenType == null) {
            out.append("((\"").append(id(state)).append("\"))\n");
        } else {
            out.append("(((\"").append(id(state)).append("<br/>")
                    .append(escape(acceptedTokenType)).append("\")))\n");
        }
    }

    void transition(State from, Object input, State to) {
        out.append("    ").append(id(from)).append(" -->|\"").append(escape(input))
                .append("\"| ").append(id(to)).append('\n');
    }

    void epsilonTransition(State from, State to) {
        out.append("    ").append(id(from)).append(" -.->|\"#949;\"| ").append(id(to)).append('\n');
    }

    @Override
    public String toString() {
        return out.toString();
    }

    private static String id(State state) {
        return "q" + state.id();
    }

    /// Escapes text for a double-quoted Mermaid label, using Mermaid's `#code;` entities.
    private static String escape(Object value) {
        StringBuilder escaped = new StringBuilder();
        for (char c : String.valueOf(value).toCharArray()) {
            switch (c) {
                case '#' -> escaped.append("#35;");
                case '"' -> escaped.append("#quot;");
                case '&' -> escaped.append("#38;");
                case '<' -> escaped.append("#lt;");
                case '>' -> escaped.append("#gt;");
                case '\n', '\r' -> escaped.append(' ');
                default -> escaped.append(c);
            }
        }
        return escaped.toString();
    }
}
