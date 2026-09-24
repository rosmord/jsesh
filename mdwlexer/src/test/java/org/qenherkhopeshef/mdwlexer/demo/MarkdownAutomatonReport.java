/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package org.qenherkhopeshef.mdwlexer.demo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import org.qenherkhopeshef.mdwlexer.automata.AutomataHelper;
import org.qenherkhopeshef.mdwlexer.automata.DeterministicFiniteAutomaton;
import org.qenherkhopeshef.mdwlexer.automata.FiniteAutomaton;
import org.qenherkhopeshef.mdwlexer.automata.NondeterministicFiniteAutomaton;

/// Writes a markdown file showing one automaton at each build stage (as produced by an NFA,
/// then [AutomataHelper#determinize], then [AutomataHelper#minimize]), each stage
/// rendered as an embedded Mermaid diagram via [FiniteAutomaton#toMermaid(Function)].
///
/// Used by [AutomatonShowcase] to turn a handful of illustrative languages into
/// something visually inspectable, under {@value #OUTPUT_DIRECTORY}.
final class MarkdownAutomatonReport {
    private static final String OUTPUT_DIRECTORY = "build/demo-automata";

    private MarkdownAutomatonReport() {
    }

    static <I, T extends Enum<T>> void write(String title, String description,
            NondeterministicFiniteAutomaton<I, T> nfa, Function<I, String> inputLabel) {
        DeterministicFiniteAutomaton<I, T> dfa = AutomataHelper.determinize(nfa);
        DeterministicFiniteAutomaton<I, T> minimized = AutomataHelper.minimize(dfa);

        StringBuilder markdown = new StringBuilder();
        markdown.append("# ").append(title).append("\n\n");
        markdown.append(description).append("\n\n");
        appendStage(markdown, "Non-deterministic (NFA)", nfa, inputLabel);
        appendStage(markdown, "Deterministic (DFA)", dfa, inputLabel);
        appendStage(markdown, "Minimized DFA", minimized, inputLabel);

        try {
            Path directory = Path.of(OUTPUT_DIRECTORY);
            Files.createDirectories(directory);
            Files.writeString(directory.resolve(slug(title) + ".md"), markdown.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static <I> void appendStage(StringBuilder markdown, String heading,
            FiniteAutomaton<I, ?> automaton, Function<I, String> inputLabel) {
        markdown.append("## ").append(heading).append(" (").append(automaton.states().size()).append(" states)\n\n");
        markdown.append("```mermaid\n").append(automaton.toMermaid(inputLabel)).append("```\n\n");
    }

    private static String slug(String title) {
        return title.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}
