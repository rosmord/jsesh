package jsesh.render.layout;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import jsesh.model.AlphabeticCharacter;
import jsesh.model.transliteration.TransliterationEncoding;
import jsesh.render.style.FontSpecification;
import jsesh.render.view.MDCView;

/// A run of alphabetic text among the subviews of a view: consecutive
/// [AlphabeticCharacter] views in the same script.
///
/// Each character has its own view (so that the caret can go between them),
/// but the run is measured as a whole, with kerning, and placed as one block.
/// [#layoutRuns] computes the runs of a view and sets the width of each
/// character view to its share of the run.
///
/// Positions are *visual*: `offsets[k]` is the distance between the left of
/// the run and the left of its `k`-th character (in logical order). For a
/// right-to-left script, the first character is thus the rightmost one.
///
/// @param start index of the first character view in the parent view.
/// @param end index after the last character view.
/// @param offsets visual x offset of each character inside the run.
/// @param widths width of each character.
/// @param width total width of the run.
/// @author rosmord
public record TextRun(int start, int end, double[] offsets, double[] widths, double width) {

    /// The x position of the `k`-th character, relative to the start of the
    /// run, in the layout coordinates of the parent view.
    ///
    /// When the parent view is drawn right to left, its subviews are mirrored
    /// (see `ViewDrawer`); the character positions are mirrored here
    /// beforehand, so that the text itself still reads in its own direction.
    ///
    /// @param k the character index, from 0 to `end - start`.
    /// @param mirrored true if the parent view is drawn right to left.
    public double layoutOffset(int k, boolean mirrored) {
        return mirrored ? width - offsets[k] - widths[k] : offsets[k];
    }

    /// The layout offsets of all characters.
    ///
    /// @see #layoutOffset(int, boolean)
    public double[] layoutOffsets(boolean mirrored) {
        double[] result = new double[offsets.length];
        for (int k = 0; k < result.length; k++) {
            result[k] = layoutOffset(k, mirrored);
        }
        return result;
    }

    /// The visual left side of the characters `from` to `to - 1` (logical order),
    /// relative to the run.
    private double minX(int from, int to) {
        double min = Double.POSITIVE_INFINITY;
        for (int k = from; k < to; k++) {
            min = Math.min(min, offsets[k]);
        }
        return min;
    }

    /// The width of a part of the run, i.e. of the characters `from` to
    /// `to - 1` (logical order). The width of the whole run is the one of the
    /// part `0` to `n`.
    ///
    /// @param from index of the first character of the part.
    /// @param to index after the last character of the part.
    public double width(int from, int to) {
        double max = Double.NEGATIVE_INFINITY;
        for (int k = from; k < to; k++) {
            max = Math.max(max, offsets[k] + widths[k]);
        }
        return max - minX(from, to);
    }

    /// The layout offsets of a part of the run, when it is placed on its own.
    ///
    /// @param from index of the first character of the part.
    /// @param to index after the last character of the part.
    /// @param mirrored true if the parent view is drawn right to left.
    /// @return the x position of each character from `from` to `to - 1`, relative to the part.
    /// @see #layoutOffset(int, boolean)
    public double[] layoutOffsets(int from, int to, boolean mirrored) {
        double min = minX(from, to);
        double partWidth = width(from, to);
        double[] result = new double[to - from];
        for (int k = from; k < to; k++) {
            double x = offsets[k] - min;
            result[k - from] = mirrored ? partWidth - x - widths[k] : x;
        }
        return result;
    }

    /// Can a line end after this character? True for white space and
    /// punctuation (but not opening brackets and quotes).
    ///
    /// @param c a character.
    /// @return true if a text may be cut after `c`.
    public static boolean canBreakAfter(AlphabeticCharacter c) {
        if (c.isSpace()) {
            return true;
        }
        return switch (Character.getType(c.getCodePoint())) {
            case Character.DASH_PUNCTUATION, Character.END_PUNCTUATION, Character.FINAL_QUOTE_PUNCTUATION,
                    Character.OTHER_PUNCTUATION -> true;
            default -> false;
        };
    }

    /// The subviews of `parent` in this run.
    public List<MDCView> views(MDCView parent) {
        List<MDCView> result = new ArrayList<>(end - start);
        for (int i = start; i < end; i++) {
            result.add(parent.getSubView(i));
        }
        return result;
    }

    /// Finds the runs of text among the subviews of `parent`, measures them,
    /// and sets the width of each character view to its share of its run.
    ///
    /// The character views must already have been laid out individually
    /// (which sets their height and baseline).
    ///
    /// @return the runs, in order.
    public static List<TextRun> layoutRuns(MDCView parent, FontSpecification fonts, FontRenderContext frc) {
        List<TextRun> runs = new ArrayList<>();
        int n = parent.getNumberOfSubviews();
        int i = 0;
        while (i < n) {
            if (!(parent.getSubView(i).getModel() instanceof AlphabeticCharacter first)) {
                i++;
                continue;
            }
            int end = i + 1;
            while (end < n && parent.getSubView(end).getModel() instanceof AlphabeticCharacter c
                    && c.isSameScriptAs(first)) {
                end++;
            }
            TransliterationEncoding encoding = fonts.transliterationEncoding();
            List<String> pieces = new ArrayList<>(end - i);
            for (int k = i; k < end; k++) {
                pieces.add(((AlphabeticCharacter) parent.getSubView(k).getModel()).getDisplayString(encoding));
            }
            TextRun run = measure(i, end, fonts.getFont(first.getScript()), pieces,
                    first.getScript().isRightToLeft(), frc);
            for (int k = i; k < end; k++) {
                parent.getSubView(k).setWidth((float) run.widths()[k - i]);
            }
            runs.add(run);
            i = end;
        }
        return runs;
    }

    /// Measures a run of text.
    ///
    /// The run is laid out as a single glyph vector, with kerning, and each
    /// piece gets the space between its own first glyph and the first glyph of
    /// the next piece (visually). The widths thus add up to the kerned width
    /// of the whole run.
    ///
    /// @param start index of the first character (stored in the result).
    /// @param end index after the last character (stored in the result).
    /// @param font the font (kerning is added to it).
    /// @param pieces the displayed string for each character.
    /// @param rightToLeft true for a right-to-left script.
    /// @param frc the font render context.
    /// @return the run.
    public static TextRun measure(int start, int end, Font font, List<String> pieces, boolean rightToLeft,
            FontRenderContext frc) {
        int n = pieces.size();
        // Char index in the full text -> piece index.
        StringBuilder text = new StringBuilder();
        int[] pieceStart = new int[n + 1];
        for (int k = 0; k < n; k++) {
            pieceStart[k] = text.length();
            text.append(pieces.get(k));
        }
        pieceStart[n] = text.length();
        char[] chars = text.toString().toCharArray();

        Font kerned = font.deriveFont(Map.of(TextAttribute.KERNING, TextAttribute.KERNING_ON));
        GlyphVector gv = kerned.layoutGlyphVector(frc, chars, 0, chars.length,
                rightToLeft ? Font.LAYOUT_RIGHT_TO_LEFT : Font.LAYOUT_LEFT_TO_RIGHT);
        int numGlyphs = gv.getNumGlyphs();
        double totalWidth = numGlyphs == 0 ? 0 : gv.getGlyphPosition(numGlyphs).getX();

        double[] offsets = new double[n];
        Arrays.fill(offsets, Double.NaN);
        for (int g = 0; g < numGlyphs; g++) {
            int piece = pieceFor(gv.getGlyphCharIndex(g), pieceStart);
            double x = gv.getGlyphPosition(g).getX();
            if (Double.isNaN(offsets[piece]) || x < offsets[piece]) {
                offsets[piece] = x;
            }
        }
        // Pieces without glyphs (should not happen) are put against the
        // previous one, with no width.
        for (int k = 0; k < n; k++) {
            if (Double.isNaN(offsets[k])) {
                offsets[k] = k == 0 ? (rightToLeft ? totalWidth : 0) : offsets[k - 1];
            }
        }
        // Each piece extends to the next piece in visual order.
        Integer[] visualOrder = new Integer[n];
        for (int k = 0; k < n; k++) {
            visualOrder[k] = k;
        }
        Arrays.sort(visualOrder, Comparator.comparingDouble(k -> offsets[k]));
        double[] widths = new double[n];
        for (int v = 0; v < n; v++) {
            int k = visualOrder[v];
            double next = v + 1 < n ? offsets[visualOrder[v + 1]] : totalWidth;
            widths[k] = Math.max(0, next - offsets[k]);
        }
        return new TextRun(start, end, offsets, widths, totalWidth);
    }

    private static int pieceFor(int charIndex, int[] pieceStart) {
        int k = Arrays.binarySearch(pieceStart, charIndex);
        if (k < 0) {
            k = -k - 2;
        } else {
            // Skip empty pieces starting at the same index.
            while (k + 1 < pieceStart.length - 1 && pieceStart[k + 1] == charIndex) {
                k++;
            }
        }
        return Math.min(k, pieceStart.length - 2);
    }
}
