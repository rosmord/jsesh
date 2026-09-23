package org.qenherkhopeshef.mdwlexer;

/// Enum class for demo token types.
/// DemoToken
public enum DemoToken {
    IF, // if
    ELSE, // else
    FUN, // fun
    IDENTIFIER, // [a-zA-Z_][a-zA-Z0-9_]*
    INTEGER, // [0-9]+
    REAL, // [0-9]+.[0-9]+
    STRING  // "[^"]*"
}
