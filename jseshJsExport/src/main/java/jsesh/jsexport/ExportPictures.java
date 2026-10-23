/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */
package jsesh.jsexport;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import jsesh.document.MDCDocument;
import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.io.document.MDCDocumentReader;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.HieroglyphicText;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.draw.MDCDrawingFacade;
import jsesh.render.style.DocumentPreferencesStyleConverter;
import jsesh.render.style.JSeshStyle;

/// Draws pictures with JSesh, to compare them with the drawings of the
/// JavaScript port (`jsesh-js/examples/compare`).
///
/// Usage: `ExportPictures <outputDir> <corpusDir>`. Writes `cases/<n>.png`
/// for each snippet of [ParserCases], `corpus/<name>.png` for each corpus
/// file (cut at 1600×3000 pixels), and `index.json`, which lists them.
public final class ExportPictures {

    /// Height of A1, in pixels.
    static final int CADRAT_HEIGHT = 30;

    public static void main(String[] args) throws IOException {
        Path outputDir = Path.of(args[0]);
        Path corpusDir = Path.of(args[1]);
        Files.createDirectories(outputDir.resolve("cases"));
        Files.createDirectories(outputDir.resolve("corpus"));

        List<Object> cases = new ArrayList<>();
        int n = 0;
        for (String mdc : ParserCases.CASES) {
            HieroglyphicText text;
            try {
                text = new MDCParserModelGenerator().parse(mdc);
            } catch (Exception e) {
                continue;
            }
            String file = "cases/" + n++ + ".png";
            write(facade(JSeshStyle.DEFAULT).createImage(text), outputDir.resolve(file));
            cases.add(Map.of("mdc", mdc, "picture", file));
        }

        List<Object> corpus = new ArrayList<>();
        try (Stream<Path> files = Files.list(corpusDir)) {
            for (Path path : files.filter(f -> f.toString().endsWith(".gly")).sorted().toList()) {
                String name = path.getFileName().toString();
                try {
                    MDCDocumentReader reader = new MDCDocumentReader();
                    reader.failFast();
                    MDCDocument document = reader.loadFile(path.toFile());
                    JSeshStyle style = DocumentPreferencesStyleConverter.applyDocumentPreferences(
                            document.getDocumentPreferences(), JSeshStyle.DEFAULT);
                    String file = "corpus/" + name.replaceAll("\\.gly$", ".png");
                    write(facade(style).createImage(document.getHieroglyphicText()), outputDir.resolve(file));
                    corpus.add(Map.of("file", name, "picture", file));
                } catch (Exception e) {
                    System.err.println(name + ": " + e);
                }
            }
        }

        Map<String, Object> index = new LinkedHashMap<>();
        index.put("cadratHeight", CADRAT_HEIGHT);
        index.put("cases", cases);
        index.put("corpus", corpus);
        Files.writeString(outputDir.resolve("index.json"), Json.toJson(index), StandardCharsets.UTF_8);
        System.out.println("Exported " + cases.size() + " cases and " + corpus.size() + " corpus pictures to "
                + outputDir);
    }

    private static MDCDrawingFacade facade(JSeshStyle style) {
        return MDCDrawingFacade.builder(new JSeshRenderContext(style, PredefinedFonts.buildAllEmbeddedFonts()))
                .cadratHeight(CADRAT_HEIGHT)
                .maxSize(1600, 3000)
                .build();
    }

    private static void write(BufferedImage image, Path file) throws IOException {
        ImageIO.write(image, "png", file.toFile());
    }
}
