/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 */
package jsesh.jsexport;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Dimension2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jsesh.glyphs.fonts.HieroglyphShapeRepository;
import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.model.AlphabeticCharacter;
import jsesh.model.Hieroglyph;
import jsesh.model.HieroglyphicText;
import jsesh.model.ModelElement;
import jsesh.model.Superscript;
import jsesh.model.constants.SymbolCodes;
import jsesh.model.constants.TextDirection;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.elements.HieroglyphDrawer;
import jsesh.render.layout.TextRun;
import jsesh.render.style.FontSpecification;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// Exports the view tree [ViewBuilder] builds for a text, and the text
/// measures the layout used, so that the JavaScript layout can be compared
/// with it.
///
/// Each view is written as `{t, x, y, w, h}` (the model type, with the code
/// for hieroglyphs, the position and size), plus `sx`/`sy` (scales), `dx`/`dy`
/// (base deltas) and `rtl` when they are not the default, and `c` (the
/// subviews).
///
/// The text measures are a list of entries: `run` (a run of alphabetic text,
/// as [TextRun#measure] measures it), `char` (the metrics of one character)
/// and `small` (the size of a text in the superscript font). The JavaScript
/// tests replay them instead of measuring text.
final class LayoutExporter {

    private static final HieroglyphShapeRepository FONTS = PredefinedFonts.buildAllEmbeddedFonts();

    private static final JSeshTechRenderContext TECH = JSeshTechRenderContext.APPROXIMATIVE_CONTEXT;

    private final JSeshStyle style;

    private final HieroglyphDrawer drawer = new HieroglyphDrawer(FONTS);

    private final List<Object> measures = new ArrayList<>();

    private final java.util.Set<String> measured = new java.util.HashSet<>();

    LayoutExporter(JSeshStyle style) {
        this.style = style;
    }

    /// Lays out `text` and returns `{view, measures}`.
    Map<String, Object> export(HieroglyphicText text) {
        MDCView view = new ViewBuilder().buildView(text, new JSeshRenderContext(style, FONTS), TECH);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("view", exportView(view));
        result.put("measures", measures);
        return result;
    }

    private Map<String, Object> exportView(MDCView v) {
        Map<String, Object> result = new LinkedHashMap<>();
        ModelElement model = v.getModel();
        String type = model.getClass().getSimpleName();
        if (model instanceof Hieroglyph h) {
            type += ":" + h.getCode();
        }
        result.put("t", type);
        result.put("x", v.getPosition().x);
        result.put("y", v.getPosition().y);
        result.put("w", v.getWidth());
        result.put("h", v.getHeight());
        if (v.getXScale() != 1f) {
            result.put("sx", v.getXScale());
        }
        if (v.getYScale() != 1f) {
            result.put("sy", v.getYScale());
        }
        if (v.getDeltaBaseX() != 0) {
            result.put("dx", v.getDeltaBaseX());
        }
        if (v.getDeltaBaseY() != 0) {
            result.put("dy", v.getDeltaBaseY());
        }
        if (v.getDirection() == TextDirection.RIGHT_TO_LEFT) {
            result.put("rtl", true);
        }
        recordMeasures(v);
        if (v.getNumberOfSubviews() > 0) {
            List<Object> children = new ArrayList<>();
            for (int i = 0; i < v.getNumberOfSubviews(); i++) {
                children.add(exportView(v.getSubView(i)));
            }
            result.put("c", children);
        }
        return result;
    }

    /// Records the text measures the layout of `v` needed.
    private void recordMeasures(MDCView v) {
        FontSpecification fonts = style.fonts();
        FontRenderContext frc = TECH.fontRenderContext();
        switch (v.getModel()) {
            case AlphabeticCharacter c -> {
                String text = c.getDisplayString(fonts.transliterationEncoding());
                if (measured.add("char\u0000" + c.getScript() + "\u0000" + text)) {
                    TextLayout layout = new TextLayout(text, fonts.getFont(c.getScript()), frc);
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("kind", "char");
                    m.put("script", c.getScript());
                    m.put("text", text);
                    m.put("advance", layout.getAdvance());
                    m.put("ascent", layout.getAscent());
                    m.put("descent", layout.getDescent());
                    measures.add(m);
                }
            }
            case Superscript s -> recordSmallText(s.getText());
            case Hieroglyph h when h.getType() == SymbolCodes.SMALLTEXT -> recordSmallText(h.getSmallText());
            case Hieroglyph h when h.getType() == SymbolCodes.MDCCODE && !drawer.isKnown(h.getCode()) ->
                recordSmallText(h.getCode());
            default -> {
            }
        }
        // The runs of text among the subviews.
        int n = v.getNumberOfSubviews();
        int i = 0;
        while (i < n) {
            if (!(v.getSubView(i).getModel() instanceof AlphabeticCharacter first)) {
                i++;
                continue;
            }
            int end = i + 1;
            while (end < n && v.getSubView(end).getModel() instanceof AlphabeticCharacter c
                    && c.isSameScriptAs(first)) {
                end++;
            }
            List<String> pieces = new ArrayList<>();
            for (int k = i; k < end; k++) {
                pieces.add(((AlphabeticCharacter) v.getSubView(k).getModel())
                        .getDisplayString(fonts.transliterationEncoding()));
            }
            boolean rightToLeft = first.getScript().isRightToLeft();
            if (measured.add("run\u0000" + first.getScript() + "\u0000" + rightToLeft + "\u0000"
                    + String.join("\u0001", pieces))) {
                Font font = fonts.getFont(first.getScript());
                TextRun run = TextRun.measure(0, pieces.size(), font, pieces, rightToLeft, frc);
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("kind", "run");
                m.put("script", first.getScript());
                m.put("rightToLeft", rightToLeft);
                m.put("pieces", pieces);
                m.put("offsets", toList(run.offsets()));
                m.put("widths", toList(run.widths()));
                m.put("width", run.width());
                measures.add(m);
            }
            i = end;
        }
    }

    private void recordSmallText(String text) {
        if (measured.add("small\u0000" + text)) {
            Dimension2D d = style.fonts().superScriptDimensions(TECH, text);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("kind", "small");
            m.put("text", text);
            m.put("width", d.getWidth());
            m.put("height", d.getHeight());
            measures.add(m);
        }
    }

    private static List<Double> toList(double[] values) {
        List<Double> result = new ArrayList<>(values.length);
        for (double d : values) {
            result.add(d);
        }
        return result;
    }
}
