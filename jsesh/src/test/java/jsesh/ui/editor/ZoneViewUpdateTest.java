/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import jsesh.model.MDCPosition;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// The view of the editor is updated incrementally after an edit inside a zone
/// (see [ViewBuilder#updateZoneView]): it must be the same as the view built
/// from scratch for the edited text.
class ZoneViewUpdateTest {

    private static final double EPSILON = 1e-9;

    private static final String BODY = "A1-G17-N35-A2:D21-R8!G5-ra:Z1-i-mn:n-Htp:t*p!D46-A1-x:t-A1";

    private static MDCPosition pos(int... path) {
        return MDCPosition.of(path);
    }

    private static Arguments edit(String name, String mdc, Consumer<JMDCEditorWorkflow> action) {
        return Arguments.of(name, mdc, action);
    }

    static Stream<Arguments> edits() {
        String[] zones = { "%zone", "%zone[dir=rl]", "%zone[layout=columns]", "%zone[scale=50]",
                "%zone[frame]", "%zone[fill=yes]", "%zone[width=300]", "%zone[layout=columns,dir=rl]", "%zone[signheight=50]", "%zone[separator=5]",
                "%zone[fill=yes,frame]" };
        Stream.Builder<Arguments> result = Stream.builder();
        for (String zone : zones) {
            String mdc = "A1-A2!" + zone + "(" + BODY + ")-B1";
            result.add(edit(zone + " insertion", mdc, w -> {
                w.setCursor(pos(3, 0, 2));
                w.addSign("D21");
            }));
            result.add(edit(zone + " line break", mdc, w -> {
                w.setCursor(pos(3, 0, 2));
                w.insertNewLine();
            }));
            result.add(edit(zone + " deletion", mdc, w -> {
                w.setCursor(pos(3, 0, 2));
                w.doBackspace();
            }));
            result.add(edit(zone + " grouping", mdc, w -> {
                w.setCursor(pos(3, 0, 0));
                w.setMark(pos(3, 0, 2));
                w.groupVertically();
            }));
            result.add(edit(zone + " red", mdc, w -> {
                w.setCursor(pos(3, 0, 1));
                w.setMark(pos(3, 0, 4));
                w.paintZoneInRed();
            }));
            result.add(edit(zone + " undo", mdc, w -> {
                w.setCursor(pos(3, 0, 2));
                w.addSign("D21");
                w.undo();
            }));
        }
        String nested = "A1!%zone[layout=columns](B1-%zone[dir=rl](" + BODY + ")-B2)-C1";
        result.add(edit("nested insertion", nested, w -> {
            w.setCursor(pos(2, 0, 1, 0, 3));
            w.addSign("D21");
        }));
        result.add(edit("nested deletion", nested, w -> {
            w.setCursor(pos(2, 0, 1, 0, 3));
            w.doBackspace();
        }));
        result.add(edit("nested fill", "A1!%zone(B1-%zone[fill=yes](" + BODY + ")-B2)-C1", w -> {
            w.setCursor(pos(2, 0, 1, 0, 3));
            w.addSign("D21");
        }));
        result.add(edit("insertion of a zone", "A1!%zone(B1-B2)-C1", w -> {
            w.setCursor(pos(2, 0, 1));
            w.insertMDC("%zone[dir=rl](D21-D21)");
        }));
        result.add(edit("tabs", "A1!%zone[tabgap=50](B1-%tab-B2-B3!C1-C2-%tab-C3)-C1", w -> {
            w.setCursor(pos(2, 0, 0));
            w.addSign("D21");
        }));
        result.add(edit("first sign of an empty zone", "A1!%zone()-C1", w -> {
            w.setCursor(pos(2, 0, 0));
            w.addSign("D21");
        }));
        return result.build();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("edits")
    void theUpdatedViewIsTheViewOfTheEditedText(String name, String mdc, Consumer<JMDCEditorWorkflow> action) {
        JMDCEditor editor = new JMDCEditor();
        editor.setMDCText(mdc);
        action.accept(editor.getWorkflow());
        MDCView expected = new ViewBuilder().buildView(editor.getHieroglyphicTextModel().getHieroglyphicText(),
                editor.getDisplayRenderContext(), editor.buildTechRenderContext());
        assertSameView(expected, editor.getView(), "root");
    }

    private static void assertSameView(MDCView expected, MDCView actual, String path) {
        assertSame(expected.getModel(), actual.getModel(), path + " model");
        assertEquals(expected.getNumberOfSubviews(), actual.getNumberOfSubviews(), path + " subviews");
        assertEquals(expected.getPosition().x, actual.getPosition().x, EPSILON, path + " x");
        assertEquals(expected.getPosition().y, actual.getPosition().y, EPSILON, path + " y");
        assertEquals(expected.getWidth(), actual.getWidth(), EPSILON, path + " width");
        assertEquals(expected.getHeight(), actual.getHeight(), EPSILON, path + " height");
        assertEquals(expected.getXScale(), actual.getXScale(), EPSILON, path + " xScale");
        assertEquals(expected.getYScale(), actual.getYScale(), EPSILON, path + " yScale");
        assertEquals(expected.getAngle(), actual.getAngle(), EPSILON, path + " angle");
        assertEquals(expected.getDeltaBaseX(), actual.getDeltaBaseX(), EPSILON, path + " deltaBaseX");
        assertEquals(expected.getDeltaBaseY(), actual.getDeltaBaseY(), EPSILON, path + " deltaBaseY");
        assertEquals(expected.getNaturalLength(), actual.getNaturalLength(), EPSILON, path + " naturalLength");
        assertEquals(expected.getDirection(), actual.getDirection(), path + " direction");
        assertEquals(expected.getOrientation(), actual.getOrientation(), path + " orientation");
        assertArrayEquals(expected.getSeparators(), actual.getSeparators(), EPSILON, path + " separators");
        for (int i = 0; i < expected.getNumberOfSubviews(); i++) {
            assertSameView(expected.getSubView(i), actual.getSubView(i),
                    path + "/" + i + ":" + expected.getSubView(i).getModel().getClass().getSimpleName());
        }
    }
}
