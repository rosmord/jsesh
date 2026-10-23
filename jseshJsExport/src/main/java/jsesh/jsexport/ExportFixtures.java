/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */
package jsesh.jsexport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

import jsesh.document.MDCDocument;
import jsesh.io.document.JSeshInfoReader;
import jsesh.io.document.MDCDocumentReader;
import jsesh.io.mdc.MdCModelWriter;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.HieroglyphicText;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.parser.MDCParser;
import jsesh.parser.lexer.MdcLexer;
import jsesh.parser.lexer.MdcLexicon;
import jsesh.parser.lexer.symbols.MdcSymbol;
import jsesh.render.style.DocumentPreferencesStyleConverter;
import jsesh.render.style.JSeshStyle;

/// Writes golden fixtures for the JavaScript port of the parser and writer.
///
/// Usage: `ExportFixtures <outputDir> <corpusDir>`. Writes:
/// - `parser-cases.json`: for each snippet of [ParserCases], the lexer symbols,
///   the AST (with philology read as signs, and as groups), and the MdC written
///   back from the model;
/// - `corpus/<name>.json`: for each `.gly` file of the corpus, the document
///   preferences, the AST of the text and the MdC written back from the model;
/// - `layout-cases.json`: for each snippet, the view tree built by the layout
///   in several styles ([#LAYOUT_VARIANTS]), see [LayoutExporter];
/// - `layout/<name>.json`: the view tree of each corpus file, laid out with
///   its own document preferences.
public final class ExportFixtures {

    /// The styles used to lay out the snippets.
    private static final Map<String, JSeshStyle> LAYOUT_VARIANTS = new LinkedHashMap<>();

    static {
        JSeshStyle base = JSeshStyle.DEFAULT;
        LAYOUT_VARIANTS.put("default", base);
        LAYOUT_VARIANTS.put("rtl", base.copy().options(o -> o.textDirection(TextDirection.RIGHT_TO_LEFT)).build());
        LAYOUT_VARIANTS.put("columns",
                base.copy().options(o -> o.textOrientation(TextOrientation.VERTICAL)).build());
        LAYOUT_VARIANTS.put("columnsRtl", base.copy().options(o -> o.textOrientation(TextOrientation.VERTICAL)
                .textDirection(TextDirection.RIGHT_TO_LEFT)).build());
        LAYOUT_VARIANTS.put("centered", base.copy().options(o -> o.smallSignCentered(true)).build());
        LAYOUT_VARIANTS.put("justified", base.copy().options(o -> o.justified(true)).build());
    }

    public static void main(String[] args) throws IOException {
        Path outputDir = Path.of(args[0]);
        Path corpusDir = Path.of(args[1]);
        Files.createDirectories(outputDir.resolve("corpus"));

        List<Object> cases = new ArrayList<>();
        for (String mdc : ParserCases.CASES) {
            cases.add(exportCase(mdc));
        }
        Files.writeString(outputDir.resolve("parser-cases.json"), Json.toJson(cases), StandardCharsets.UTF_8);

        List<Object> layoutCases = new ArrayList<>();
        for (String mdc : ParserCases.CASES) {
            exportLayoutCase(mdc).ifPresent(layoutCases::add);
        }
        Files.writeString(outputDir.resolve("layout-cases.json"), Json.toJson(layoutCases), StandardCharsets.UTF_8);
        Files.createDirectories(outputDir.resolve("layout"));

        List<String> names = new ArrayList<>();
        try (Stream<Path> files = Files.list(corpusDir)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".gly")).sorted().toList()) {
                String name = file.getFileName().toString();
                names.add(name);
                Files.writeString(outputDir.resolve("corpus").resolve(name.replaceAll("\\.gly$", ".json")),
                        Json.toJson(exportCorpusFile(file)), StandardCharsets.UTF_8);
                Files.writeString(outputDir.resolve("layout").resolve(name.replaceAll("\\.gly$", ".json")),
                        Json.toJson(exportCorpusLayout(file)), StandardCharsets.UTF_8);
            }
        }
        Files.writeString(outputDir.resolve("corpus").resolve("index.json"), Json.toJson(names),
                StandardCharsets.UTF_8);
        System.out.println("Exported " + cases.size() + " cases and " + names.size() + " corpus files to " + outputDir);
    }

    private static Map<String, Object> exportCase(String mdc) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("input", mdc);
        result.put("symbols", lex(mdc));
        putAst(result, "ast", mdc, true);
        putAst(result, "astPhilologyGrouping", mdc, false);
        try {
            HieroglyphicText text = new MDCParserModelGenerator().parse(mdc);
            result.put("written", new MdCModelWriter().toMdC(text));
        } catch (Exception e) {
            result.put("writtenError", String.valueOf(e.getMessage()));
        }
        return result;
    }

    private static Optional<Object> exportLayoutCase(String mdc) {
        HieroglyphicText text;
        try {
            text = new MDCParserModelGenerator().parse(mdc);
        } catch (Exception e) {
            return Optional.empty();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("input", mdc);
        Map<String, Object> variants = new LinkedHashMap<>();
        LAYOUT_VARIANTS.forEach((name, style) -> variants.put(name, new LayoutExporter(style).export(text)));
        result.put("variants", variants);
        return Optional.of(result);
    }

    private static Map<String, Object> exportCorpusLayout(Path file) throws IOException {
        MDCDocumentReader reader = new MDCDocumentReader();
        reader.failFast();
        MDCDocument document;
        try {
            document = reader.loadFile(file.toFile());
        } catch (Exception e) {
            return Map.of("error", String.valueOf(e.getMessage()));
        }
        JSeshStyle style = DocumentPreferencesStyleConverter.applyDocumentPreferences(
                document.getDocumentPreferences(), JSeshStyle.DEFAULT);
        return new LayoutExporter(style).export(document.getHieroglyphicText());
    }

    private static List<Object> lex(String mdc) {
        List<Object> symbols = new ArrayList<>();
        MdcLexer lexer = MdcLexicon.instance().newLexer(mdc);
        lexer.setPhilologyAsSigns(true);
        Optional<MdcSymbol> symbol;
        while ((symbol = lexer.nextSymbol()).isPresent()) {
            MdcSymbol s = symbol.get();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", s.code());
            m.put("text", s.text());
            m.put("value", s.value());
            symbols.add(m);
        }
        return symbols;
    }

    private static void putAst(Map<String, Object> result, String key, String mdc, boolean philologyAsSigns) {
        try {
            MDCParser parser = new MDCParser();
            parser.setPhilologyAsSigns(philologyAsSigns);
            result.put(key, parser.parse(mdc));
        } catch (Exception e) {
            result.put(key + "Error", String.valueOf(e.getMessage()));
        }
    }

    private static Map<String, Object> exportCorpusFile(Path file) throws IOException {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("file", file.getFileName().toString());
        String source = Files.readString(file, StandardCharsets.UTF_8);
        if (source.startsWith("﻿")) {
            source = source.substring(1);
        }
        StringBuffer buffer = new StringBuffer(source);
        JSeshInfoReader infoReader = new JSeshInfoReader();
        infoReader.process(buffer, new MDCDocument());
        putAst(result, "ast", buffer.substring(infoReader.getNextPos()), true);
        try {
            MDCDocumentReader reader = new MDCDocumentReader();
            reader.failFast();
            MDCDocument document = reader.loadFile(file.toFile());
            result.put("preferences", new TreeMap<>(document.getDocumentPreferences().getStringRepresentation()));
            result.put("written", new MdCModelWriter().toMdC(document.getHieroglyphicText()));
        } catch (Exception e) {
            result.put("writtenError", String.valueOf(e.getMessage()));
        }
        return result;
    }
}
