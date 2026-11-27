/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jseshtemplates;

import java.util.ArrayList;
import java.util.List;

/// Splits a template file into its template header (name and description) and
/// the document to create from it.
///
/// A template is a JSesh file. After the usual document preferences
/// (`++JSesh_...` lines), it starts with a *template header*: lines of text,
/// ended by a line whose text is [#END_MARKER]. The first line is the name of
/// the template, the others are its description.
///
/// Only the text blocks of the header are read (`+l`, `+b`... up to `+s`). A
/// "line" is a line of the MdC text, ended by `!`.
final class TemplateParser {

    /// The text of the line which ends the template header.
    static final String END_MARKER = "END";

    private static final String PREFERENCES_PREFIX = "++JSesh_";

    /// The result of the parsing of a template file.
    ///
    /// @param name        the name of the template.
    /// @param description its description, possibly empty, with `\n` between lines.
    /// @param document    the document to create: preferences and content, without
    ///                    the template header.
    record ParsedTemplate(String name, String description, String document) {
    }

    private final String source;
    private int pos = 0;

    private TemplateParser(String source) {
        this.source = source;
    }

    /// @param content content of a template file.
    /// @return the parts of the template.
    /// @throws IllegalArgumentException if there is no end marker, or no name.
    static ParsedTemplate parse(String content) {
        String source = content.startsWith("﻿") ? content.substring(1) : content;
        return new TemplateParser(source).parse();
    }

    private ParsedTemplate parse() {
        while (source.startsWith(PREFERENCES_PREFIX, pos)) {
            skipPhysicalLine();
        }
        String preferences = source.substring(0, pos);
        List<String> header = readHeader();
        if (header.isEmpty() || header.get(0).isEmpty()) {
            throw new IllegalArgumentException("A template needs a name on its first line");
        }
        String description = String.join("\n", header.subList(1, header.size())).strip();
        return new ParsedTemplate(header.get(0), description, preferences + source.substring(pos));
    }

    /// Reads the lines of the header, and leaves [#pos] at the start of the content.
    ///
    /// @return the text of the lines before the end marker.
    private List<String> readHeader() {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        boolean inText = false;
        boolean inComment = false;
        while (pos < source.length()) {
            char c = source.charAt(pos);
            char next = pos + 1 < source.length() ? source.charAt(pos + 1) : 0;
            // "+x" starts a text block, "++" a comment, and "+s" goes back to signs.
            if (c == '+' && (Character.isLowerCase(next) || next == '+')) {
                inText = next != 's';
                inComment = next == '+';
                pos += 2;
            } else if (inText) {
                if (c == '\\' && (next == '+' || next == '\\')) {
                    c = next;
                    pos++;
                }
                if (!inComment) {
                    line.append(c);
                }
                pos++;
            } else if (c == '!') {
                skipLineBreak();
                String text = normalizeSpaces(line);
                line.setLength(0);
                if (END_MARKER.equals(text)) {
                    return lines;
                }
                lines.add(text);
            } else {
                pos++;
            }
        }
        // The end marker may be the very end of the file (empty template).
        if (END_MARKER.equals(normalizeSpaces(line))) {
            return lines;
        }
        throw new IllegalArgumentException("Template without " + END_MARKER + " line");
    }

    /// Skips a line or page break (`!`, `!!`, `!=150%`, `![align=center]`) and
    /// the separators which follow it.
    private void skipLineBreak() {
        while (at('!')) {
            pos++;
        }
        if (at('=')) {
            int percent = source.indexOf('%', pos);
            pos = percent == -1 ? source.length() : percent + 1;
        }
        if (at('[')) {
            int bracket = source.indexOf(']', pos);
            pos = bracket == -1 ? source.length() : bracket + 1;
        }
        while (pos < source.length() && (at('-') || Character.isWhitespace(source.charAt(pos)))) {
            pos++;
        }
    }

    private void skipPhysicalLine() {
        while (pos < source.length() && !at('\n') && !at('\r')) {
            pos++;
        }
        while (at('\n') || at('\r')) {
            pos++;
        }
    }

    private boolean at(char c) {
        return pos < source.length() && source.charAt(pos) == c;
    }

    private static String normalizeSpaces(CharSequence text) {
        return text.toString().replaceAll("\\s+", " ").strip();
    }
}
