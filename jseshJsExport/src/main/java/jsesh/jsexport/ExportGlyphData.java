/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */
package jsesh.jsexport;

import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import jsesh.glyphs.fonts.HieroglyphShapeRepository;
import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.glyphs.shape.LigatureZone;
import jsesh.glyphs.shape.ShapeChar;
import jsesh.render.elements.HieroglyphDrawer;
import jsesh.signcodes.CanonicalCode;
import jsesh.signcodes.ManuelDeCodage;
import jsesh.utils.resources.ResourcesManager;

/// Writes the glyph data used by `@jsesh/glyphs`.
///
/// Usage: `ExportGlyphData <outputDir>`. Writes:
/// - `glyph-index.json`: what the layout needs, loaded once: for each sign
///   of the embedded fonts (JSesh font, then Gnutrace), its size and its three
///   ligature zones (computed here, with the same code JSesh uses); the
///   phonetic code to Gardiner code map; the predefined ligatures.
/// - `glyphs/<file>.json`: the shape of each sign, as an SVG path, loaded
///   lazily. [#fileNameForCode(String)] gives the file name;
/// - `fonts/EgyptoSerif.ttf`: the transliteration font (a Bitstream Vera
///   derivative, freely redistributable).
public final class ExportGlyphData {

    /// Decimals of the numbers written in shapes (in font units).
    private static final int PATH_DECIMALS = 3;

    /// Decimals of the sizes and zones, which the layout uses.
    private static final int INDEX_DECIMALS = 4;

    public static void main(String[] args) throws IOException {
        Path outputDir = Path.of(args[0]);
        Path glyphDir = outputDir.resolve("glyphs");
        Files.createDirectories(glyphDir);

        HieroglyphShapeRepository repository = PredefinedFonts.buildAllEmbeddedFonts();
        HieroglyphDrawer drawer = new HieroglyphDrawer(repository);
        ManuelDeCodage mdc = ManuelDeCodage.getInstance();

        Map<String, Object> glyphs = new TreeMap<>(ManuelDeCodage.getCodeComparator());
        for (String code : repository.getCodes()) {
            if (code.endsWith("_BOLD")) {
                continue; // Small body variants are not exported.
            }
            CanonicalCode canonical = mdc.getCanonicalCode(code);
            ShapeChar shape = repository.get(canonical);
            if (shape == null || glyphs.containsKey(canonical.code())) {
                continue;
            }
            Rectangle2D bbox = shape.getBbox();
            List<Object> entry = new ArrayList<>();
            entry.add(round(bbox.getWidth()));
            entry.add(round(bbox.getHeight()));
            List<Object> zones = new ArrayList<>();
            boolean hasZones = false;
            for (int i = 0; i < 3; i++) {
                Optional<LigatureZone> zone = drawer.getLigatureZone(i, canonical.code());
                if (zone.isPresent()) {
                    LigatureZone z = zone.get();
                    zones.add(List.of(round(z.getMinX()), round(z.getMinY()), round(z.getWidth()),
                            round(z.getHeight()),
                            "" + z.getHorizontalGravity().getCode() + z.getVerticalGravity().getCode()));
                    hasZones = true;
                } else {
                    zones.add(null);
                }
            }
            if (hasZones) {
                entry.add(zones);
            }
            glyphs.put(canonical.code(), entry);
            writeShape(glyphDir.resolve(fileNameForCode(canonical.code()) + ".json"), shape);
        }

        Map<String, Object> index = new LinkedHashMap<>();
        index.put("format", 1);
        index.put("a1", glyphs.get(mdc.getA1Code().code()));
        index.put("canonical", canonicalCodes(mdc));
        index.put("ligatures", ligatures(mdc));
        index.put("glyphs", glyphs);
        Files.writeString(outputDir.resolve("glyph-index.json"), Json.toJson(index), StandardCharsets.UTF_8);
        Files.createDirectories(outputDir.resolve("fonts"));
        try (var in = ResourcesManager.class.getResourceAsStream("/jseshResources/fonts/EgyptoSerif.ttf")) {
            Files.copy(in, outputDir.resolve("fonts/EgyptoSerif.ttf"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println("Exported " + glyphs.size() + " glyphs to " + outputDir);
    }

    /// The file name (without extension) for a sign's shape.
    ///
    /// Codes are case-sensitive, but file systems often aren't: lowercase
    /// letters and digits are kept, an uppercase letter becomes `_` and the
    /// letter in lowercase, any other character `-` and its hexadecimal code.
    /// `jsesh-js/packages/glyphs/src/glyph-files.ts` mirrors this function.
    static String fileNameForCode(String code) {
        StringBuilder result = new StringBuilder();
        for (char c : code.toCharArray()) {
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                result.append(c);
            } else if (c >= 'A' && c <= 'Z') {
                result.append('_').append(Character.toLowerCase(c));
            } else {
                result.append('-').append(Integer.toHexString(c)).append('-');
            }
        }
        return result.toString();
    }

    private static void writeShape(Path file, ShapeChar shape) throws IOException {
        Map<String, Object> result = new LinkedHashMap<>();
        PathIterator iterator = shape.getShape().getPathIterator(null);
        if (iterator.getWindingRule() == PathIterator.WIND_EVEN_ODD) {
            result.put("evenOdd", true);
        }
        result.put("d", svgPath(iterator));
        Files.writeString(file, Json.toJson(result), StandardCharsets.UTF_8);
    }

    private static String svgPath(PathIterator iterator) {
        StringBuilder out = new StringBuilder();
        double[] coords = new double[6];
        while (!iterator.isDone()) {
            int type = iterator.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_MOVETO -> appendCommand(out, 'M', coords, 2);
                case PathIterator.SEG_LINETO -> appendCommand(out, 'L', coords, 2);
                case PathIterator.SEG_QUADTO -> appendCommand(out, 'Q', coords, 4);
                case PathIterator.SEG_CUBICTO -> appendCommand(out, 'C', coords, 6);
                case PathIterator.SEG_CLOSE -> out.append('Z');
                default -> throw new IllegalStateException("Unknown segment " + type);
            }
            iterator.next();
        }
        return out.toString();
    }

    private static void appendCommand(StringBuilder out, char command, double[] coords, int n) {
        out.append(command);
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                out.append(' ');
            }
            out.append(format(coords[i]));
        }
    }

    /// The canonical code of the codes which have one (mostly phonetic codes).
    @SuppressWarnings("unchecked")
    private static Map<String, String> canonicalCodes(ManuelDeCodage mdc) {
        try {
            Field field = ManuelDeCodage.class.getDeclaredField("canonical");
            field.setAccessible(true);
            Map<String, CanonicalCode> map = (Map<String, CanonicalCode>) field.get(mdc);
            Map<String, String> result = new TreeMap<>();
            map.forEach((code, canonical) -> result.put(code, canonical.code()));
            return result;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /// The predefined ligatures, read as `TkseshLigatureCatalogue` reads them
    /// (a later definition replaces an earlier one): a list of
    /// `[codes, positions]`, each position being `[x, y, scale]`.
    private static List<Object> ligatures(ManuelDeCodage mdc) throws IOException {
        Map<List<String>, List<Object>> map = new LinkedHashMap<>();
        try (Reader in = ResourcesManager.getInstance().getLigatureData();
                BufferedReader reader = new BufferedReader(in)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                List<String> codes = Arrays.stream(parts[0].split("&"))
                        .map(c -> mdc.getCanonicalCode(c).code()).toList();
                String[] pos = parts[1].strip().split(" ");
                List<Object> positions = new ArrayList<>();
                for (int i = 0; i < pos.length; i += 4) {
                    positions.add(List.of(Float.parseFloat(pos[i + 1]), Float.parseFloat(pos[i + 2]),
                            Integer.parseInt(pos[i + 3])));
                }
                map.put(codes, positions);
            }
        }
        List<Object> result = new ArrayList<>();
        map.forEach((codes, positions) -> result.add(List.of(codes, positions)));
        return result;
    }

    private static double round(double d) {
        return round(d, INDEX_DECIMALS);
    }

    private static double round(double d, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(d * factor) / factor;
    }

    private static String format(double d) {
        double r = round(d, PATH_DECIMALS);
        if (r == Math.rint(r)) {
            return Long.toString((long) r);
        }
        String s = Double.toString(r);
        return s.contains("E") ? new java.math.BigDecimal(r).toPlainString() : s;
    }
}
