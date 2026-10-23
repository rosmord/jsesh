/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */
package jsesh.jsexport;

import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.Map;

/// Minimal JSON writer for the fixtures.
///
/// Records are written as objects whose `nodeType` is the record's simple name
/// without its `Ast` prefix, followed by the record components; enums are
/// written as their names, `char`s as one-character strings. This is the
/// shape of the TypeScript AST (`jsesh-js/packages/core/src/parser/ast.ts`).
final class Json {
    private final StringBuilder out = new StringBuilder();

    private Json() {
    }

    static String toJson(Object value) {
        Json json = new Json();
        json.write(value);
        return json.out.toString();
    }

    private void write(Object value) {
        switch (value) {
            case null -> out.append("null");
            case String s -> writeString(s);
            case Character c -> writeString(c.toString());
            case Boolean b -> out.append(b);
            case Double d -> writeDouble(d);
            case Float f -> writeDouble(f.doubleValue());
            case Number n -> out.append(n);
            case Enum<?> e -> writeString(e.name());
            case Map<?, ?> map -> writeMap(map);
            case Collection<?> collection -> writeCollection(collection);
            case Object[] array -> writeCollection(java.util.Arrays.asList(array));
            case Record record -> writeRecord(record);
            default -> throw new IllegalArgumentException("Can't write " + value.getClass() + " as JSON");
        }
    }

    private void writeDouble(double d) {
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            out.append("null");
        } else if (d == Math.rint(d) && Math.abs(d) < 1e15) {
            out.append((long) d);
        } else {
            out.append(d);
        }
    }

    private void writeMap(Map<?, ?> map) {
        out.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                out.append(',');
            }
            first = false;
            writeString(entry.getKey().toString());
            out.append(':');
            write(entry.getValue());
        }
        out.append('}');
    }

    private void writeCollection(Collection<?> collection) {
        out.append('[');
        boolean first = true;
        for (Object o : collection) {
            if (!first) {
                out.append(',');
            }
            first = false;
            write(o);
        }
        out.append(']');
    }

    private void writeRecord(Record record) {
        String name = record.getClass().getSimpleName();
        if (name.startsWith("Ast")) {
            name = name.substring(3);
        }
        out.append("{\"nodeType\":");
        writeString(name);
        for (RecordComponent component : record.getClass().getRecordComponents()) {
            out.append(',');
            writeString(component.getName());
            out.append(':');
            try {
                write(component.getAccessor().invoke(record));
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
        out.append('}');
    }

    private void writeString(String s) {
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }
}
