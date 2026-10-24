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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import jsesh.defaults.HieroglyphDatabaseFactory;
import jsesh.glyphs.fonts.HieroglyphShapeRepository;
import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.glyphs.signdata.HieroglyphDatabase;
import jsesh.glyphs.signdata.HieroglyphFamily;
import jsesh.glyphs.signdata.Possibility;
import jsesh.glyphs.signdata.SignDescriptionConstants;
import jsesh.glyphs.signdata.SignVariant;
import jsesh.signcodes.GardinerCode;
import jsesh.signcodes.ManuelDeCodage;

/// Writes the sign database used by the palette of `@jsesh/glyphs`
/// (`SignDatabase`), as `sign-info.json`.
///
/// Usage: `ExportSignInfo <outputDir>`. The database is JSesh's default one
/// (embedded fonts and sign descriptions, without the user's definitions).
/// The file holds:
/// - `families`: `[code, label]` for each Gardiner family, in JSesh's order;
/// - `signs`: one entry per sign with a well-formed Gardiner code, in
///   Manuel de Codage order: `c` (code), and, when they are not empty, `b`
///   (1 if the sign is listed when variants are hidden,
///   [HieroglyphDatabase#isAlwaysDisplayed(String)]), `v` (values), `t`
///   (tags), `var` (`[code, type]` of its variants), `in` (the signs it
///   contains), `d` (its description, in MdC);
/// - `values`: for each value, the signs the palette proposes for it
///   ([HieroglyphDatabase#getPossibilityFor(String, String)] with
///   `PALETTE`);
/// - `keyboard`: for each value typed at the keyboard, the signs a
///   possibility list cycles through ([HieroglyphDatabase#getPossibilityFor(String, String)]
///   with `KEYBOARD`, as `PossibilityRepositoryFromGlossary` does without
///   glossary);
/// - `tallNarrow`, `lowBroad`, `lowNarrow`: Gardiner's lists of sign shapes.
public final class ExportSignInfo {

    public static void main(String[] args) throws IOException {
        Path outputDir = Path.of(args[0]);
        Files.createDirectories(outputDir);
        HieroglyphShapeRepository repository = PredefinedFonts.buildAllEmbeddedFonts();
        HieroglyphDatabase database = HieroglyphDatabaseFactory.buildPlainDefault(repository);
        ManuelDeCodage mdc = ManuelDeCodage.getInstance();

        List<Object> families = new ArrayList<>();
        for (HieroglyphFamily family : database.getFamilies()) {
            families.add(List.of(family.getCode(), family.getDescription()));
        }

        List<Object> signs = new ArrayList<>();
        Map<String, Object> values = new TreeMap<>();
        Map<String, Object> keyboard = new TreeMap<>();
        for (String code : database.getCodesForFamily("", true)) {
            Map<String, Object> sign = new LinkedHashMap<>();
            sign.put("c", code);
            if (database.isAlwaysDisplayed(code)) {
                sign.put("b", 1);
            }
            List<String> signValues = database.getValuesFor(code);
            putIfNotEmpty(sign, "v", signValues);
            putIfNotEmpty(sign, "t", new ArrayList<>(new TreeSet<>(database.getTagsForSign(code))));
            List<Object> variants = new ArrayList<>();
            for (SignVariant variant : new TreeSet<>(database.getVariants(code))) {
                variants.add(List.of(variant.getCode(), variant.getType().name()));
            }
            putIfNotEmpty(sign, "var", variants);
            putIfNotEmpty(sign, "in", sorted(database.getSignsIn(code)));
            String description = database.getDescriptionFor(code);
            if (description != null && !description.isBlank()) {
                sign.put("d", description.trim());
            }
            signs.add(sign);
            for (String value : signValues) {
                values.computeIfAbsent(value, v -> paletteSignsFor(database, v));
                List<String> typed = keyboardSignsFor(database, value);
                if (!typed.isEmpty()) {
                    keyboard.putIfAbsent(value.replace("-", ""), typed);
                }
            }
        }

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("format", 1);
        info.put("families", families);
        info.put("signs", signs);
        info.put("values", values);
        info.put("keyboard", keyboard);
        info.put("tallNarrow", mdc.getTallNarrowSigns());
        info.put("lowBroad", mdc.getLowBroadSigns());
        info.put("lowNarrow", mdc.getLowNarrowSigns());
        Files.writeString(outputDir.resolve("sign-info.json"), Json.toJson(info), StandardCharsets.UTF_8);
        System.out.println("Exported the description of " + signs.size() + " signs to " + outputDir);
    }

    /// The signs proposed by the palette for a value: single signs, with a
    /// Gardiner code (as `PalettePresenter.selectFromTransliterationOrCode`).
    private static List<String> paletteSignsFor(HieroglyphDatabase database, String value) {
        List<String> result = new ArrayList<>();
        var possibilities = database.getPossibilityFor(value, SignDescriptionConstants.PALETTE);
        if (possibilities != null) {
            for (Possibility p : possibilities.asList()) {
                if (p.isSingleSign() && GardinerCode.isWellFormedGardinerCode(p.getCode())
                        && !result.contains(p.getCode())) {
                    result.add(p.getCode());
                }
            }
        }
        return result;
    }

    /// The single signs a possibility list offers for a value typed at the keyboard, in order.
    private static List<String> keyboardSignsFor(HieroglyphDatabase database, String value) {
        List<String> result = new ArrayList<>();
        var possibilities = database.getPossibilityFor(value, SignDescriptionConstants.KEYBOARD);
        if (possibilities != null) {
            for (Possibility p : possibilities.asList()) {
                if (p.isSingleSign() && !result.contains(p.getCode())) {
                    result.add(p.getCode());
                }
            }
        }
        return result;
    }

    private static List<String> sorted(Collection<String> codes) {
        List<String> result = new ArrayList<>(codes);
        result.sort(ManuelDeCodage.getCodeComparator());
        return result;
    }

    private static void putIfNotEmpty(Map<String, Object> map, String key, List<?> list) {
        if (!list.isEmpty()) {
            map.put(key, list);
        }
    }
}
