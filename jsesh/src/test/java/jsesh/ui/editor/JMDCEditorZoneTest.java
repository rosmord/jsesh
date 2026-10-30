/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import jsesh.document.HieroglyphicTextModel;
import jsesh.model.HieroglyphicText;
import jsesh.model.MDCPosition;
import jsesh.render.draw.CursorShape;
import jsesh.render.draw.TextGeometry;
import jsesh.render.draw.SelectionShape;
import jsesh.model.TextScope;
import jsesh.model.ZoneParameters;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;

/// Editing in `%zone(...)`: vertical moves, the "insert zone" command, typing,
/// and the cursor and click geometry.
class JMDCEditorZoneTest {

    private static JMDCEditorWorkflow workflow(String mdc) throws Exception {
        HieroglyphicTextModel model = new HieroglyphicTextModel();
        model.setMDCCode(mdc);
        return new JMDCEditorWorkflow(model, code -> null);
    }

    private static MDCPosition pos(int... path) {
        return MDCPosition.of(path);
    }

    private static String code(JMDCEditorWorkflow w) {
        return w.getMDCCode().replaceAll("\\s", "");
    }

    @Test
    void upAndDownOutsideZonesAreUnchanged() throws Exception {
        String mdc = "A1-A2-!B1-B2-!C1";
        HieroglyphicText text = workflow(mdc).getHieroglyphicTextModel().getHieroglyphicText();
        for (int i = 0; i <= text.getNumberOfChildren(); i++) {
            JMDCEditorWorkflow w = workflow(mdc);
            w.setCursor(pos(i));
            w.cursorDown();
            assertEquals(text.getDownPosition(pos(i)), w.getCaret().getInsertPosition(), "down from " + i);
            w.setCursor(pos(i));
            w.cursorUp();
            assertEquals(text.getUpPosition(pos(i)), w.getCaret().getInsertPosition(), "up from " + i);
        }
    }

    /// Zone content: `0 |B1| 1 |!| 2 |B2| 3 |!| 4 |B3| 5`.
    @Test
    void upAndDownFollowTheLinesOfAZone() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-%zone(B1!B2!B3)-A2!C1");
        w.setCursor(pos(1, 0, 3));
        w.cursorDown();
        assertEquals(pos(1, 0, 4), w.getCaret().getInsertExactPosition());
        w.cursorUp();
        assertEquals(pos(1, 0, 2), w.getCaret().getInsertExactPosition());
        w.cursorUp();
        assertEquals(pos(1, 0, 0), w.getCaret().getInsertExactPosition());
    }

    @Test
    void upAndDownLeaveAZoneAtItsFirstAndLastLines() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-%zone(B1!B2)-A2!C1");
        // Last line of the zone: down goes to the next line of the text.
        w.setCursor(pos(1, 0, 3));
        w.cursorDown();
        assertEquals(pos(4), w.getCaret().getInsertExactPosition());
        // First line of the zone, on the first line of the text.
        w.setCursor(pos(1, 0, 1));
        w.cursorUp();
        assertEquals(pos(0), w.getCaret().getInsertExactPosition());
    }

    @Test
    void textFlowAtCaretFollowsTheEnclosingZones() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-%zone[layout=columns](B1-%zone[dir=rl](C1))-A2");
        TextOrientation h = TextOrientation.HORIZONTAL;
        TextDirection lr = TextDirection.LEFT_TO_RIGHT;
        // Outside any zone: the fallbacks.
        w.setCursor(pos(1));
        assertEquals(h, w.getTextOrientationAtCaret(h));
        assertEquals(lr, w.getTextDirectionAtCaret(lr));
        // In the column zone.
        w.setCursor(pos(1, 0, 1));
        assertEquals(TextOrientation.VERTICAL, w.getTextOrientationAtCaret(h));
        assertEquals(lr, w.getTextDirectionAtCaret(lr));
        // In the nested zone: direction set there, orientation inherited from the outer zone.
        w.setCursor(pos(1, 0, 1, 0, 1));
        assertEquals(TextOrientation.VERTICAL, w.getTextOrientationAtCaret(h));
        assertEquals(TextDirection.RIGHT_TO_LEFT, w.getTextDirectionAtCaret(lr));
    }

    /// `A1-%zone[dir=rl](B1-B2)-A2`: zone content gaps `(1,0,0)` (right edge of the
    /// zone) to `(1,0,2)` (left edge); `(1)` is before the zone, `(2)` after it.
    @Test
    void arrowsFollowTheScreenAtTheBoundariesOfAZoneWithAnotherDirection() throws Exception {
        TextDirection lr = TextDirection.LEFT_TO_RIGHT;
        JMDCEditorWorkflow w = workflow("A1-%zone[dir=rl](B1-B2)-A2");
        // Right edge of the zone: right leaves it, to its right.
        w.setCursor(pos(1, 0, 0));
        w.cursorHorizontally(true, lr);
        assertEquals(pos(2), w.getCaret().getInsertExactPosition());
        // Left edge of the zone: left leaves it, to its left.
        w.setCursor(pos(1, 0, 2));
        w.cursorHorizontally(false, lr);
        assertEquals(pos(1), w.getCaret().getInsertExactPosition());
        // From after the zone, left enters it by its right edge.
        w.setCursor(pos(2));
        w.cursorHorizontally(false, lr);
        assertEquals(pos(1, 0, 0), w.getCaret().getInsertExactPosition());
        // From before the zone, right enters it by its left edge.
        w.setCursor(pos(1));
        w.cursorHorizontally(true, lr);
        assertEquals(pos(1, 0, 2), w.getCaret().getInsertExactPosition());
        // Inside, right goes towards the right, that is backwards in the zone.
        w.setCursor(pos(1, 0, 2));
        w.cursorHorizontally(true, lr);
        assertEquals(pos(1, 0, 1), w.getCaret().getInsertExactPosition());
    }

    @Test
    void arrowsAreUnchangedAtTheBoundariesOfAZoneWithTheSameDirection() throws Exception {
        TextDirection lr = TextDirection.LEFT_TO_RIGHT;
        JMDCEditorWorkflow w = workflow("A1-%zone(B1-B2)-A2");
        w.setCursor(pos(1));
        w.cursorHorizontally(true, lr);
        assertEquals(pos(1, 0, 0), w.getCaret().getInsertExactPosition());
        w.setCursor(pos(1, 0, 2));
        w.cursorHorizontally(true, lr);
        assertEquals(pos(2), w.getCaret().getInsertExactPosition());
    }

    @Test
    void theSelectionIsPutInAZone() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-A2-A3");
        w.setCursor(pos(1));
        w.setMark(pos(3));
        assertTrue(w.addZone());
        assertEquals("A1-%zone(A2-A3)", code(w));
        assertEquals(pos(1, 0, 2), w.getCaret().getInsertExactPosition());
        w.undo();
        assertEquals("A1-A2-A3", code(w));
    }

    @Test
    void anEmptyZoneIsInsertedWithoutSelection() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-A2");
        w.setCursor(pos(1));
        assertTrue(w.addZone());
        assertEquals("A1-%zone()-A2", code(w));
        assertEquals(pos(1, 0, 0), w.getCaret().getInsertExactPosition());
    }

    @Test
    void aZoneCanBeInsertedWithParameters() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-A2");
        w.setCursor(pos(1));
        ZoneParameters p = ZoneParameters.DEFAULT.withFrame(true).withWidth(1200);
        assertTrue(w.addZone(p));
        assertEquals(Optional.of(p), w.getZoneParametersAtCaret());
        assertEquals(pos(1, 0, 0), w.getCaret().getInsertExactPosition());
    }

    @Test
    void theParametersOfTheEnclosingZoneCanBeChanged() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-%zone(A2-A3)-A4");
        assertEquals(Optional.empty(), w.getZoneParametersAtCaret());
        w.setCursor(pos(1, 0, 1));
        ZoneParameters p = ZoneParameters.DEFAULT.withFrame(true);
        assertTrue(w.setZoneParametersAtCaret(p));
        assertEquals(Optional.of(p), w.getZoneParametersAtCaret());
        assertEquals(pos(1, 0, 1), w.getCaret().getInsertExactPosition());
        assertEquals("A1-%zone[frame](A2-A3)-A4", code(w));
        w.undo();
        w.setCursor(pos(1, 0, 1));
        assertEquals(Optional.of(ZoneParameters.DEFAULT), w.getZoneParametersAtCaret());
        assertEquals("A1-%zone(A2-A3)-A4", code(w));
    }

    @Test
    void unwrappingAZonePutsItsContentInItsParent() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-%zone[frame](A2-A3!A4)-A5");
        w.setCursor(pos(1, 0, 1));
        assertTrue(w.unwrapZone());
        assertEquals("A1-A2-A3-!A4-A5", code(w));
        // Still between A2 and A3.
        assertEquals(pos(2), w.getCaret().getInsertExactPosition());
        assertEquals(Optional.empty(), w.getZoneParametersAtCaret());
        w.undo();
        assertEquals("A1-%zone[frame](A2-A3-!A4)-A5", code(w));
    }

    @Test
    void wrappingThenUnwrappingGivesTheTextBack() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-A2-A3");
        w.setCursor(pos(1));
        w.setMark(pos(3));
        assertTrue(w.addZone());
        assertTrue(w.unwrapZone());
        assertEquals("A1-A2-A3", code(w));
        assertEquals(pos(3), w.getCaret().getInsertExactPosition());
    }

    @Test
    void onlyTheInnermostZoneIsUnwrapped() throws Exception {
        JMDCEditorWorkflow w = workflow("%zone(A1-%zone(A2-A3)-A4)");
        w.setCursor(pos(0, 0, 1, 0, 2));
        assertTrue(w.unwrapZone());
        assertEquals("%zone(A1-A2-A3-A4)", code(w));
        assertEquals(pos(0, 0, 3), w.getCaret().getInsertExactPosition());
    }

    @Test
    void anEmptyZoneIsUnwrappedToNothing() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-%zone()-A2");
        w.setCursor(pos(1, 0, 0));
        assertTrue(w.unwrapZone());
        assertEquals("A1-A2", code(w));
        assertEquals(pos(1), w.getCaret().getInsertExactPosition());
    }

    @Test
    void aZoneIsUnwrappedFromACartoucheInIt() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-%zone(B1-<-C1-C2->)");
        // In the cartouche, the second item of the zone.
        w.setCursor(pos(1, 0, 1, 0, 0, 0, 1));
        assertTrue(w.unwrapZone());
        assertEquals("A1-B1-<-C1-C2->", code(w));
        assertEquals(pos(2, 0, 0, 0, 1), w.getCaret().getInsertExactPosition());
    }

    @Test
    void outsideAZoneThereIsNothingToUnwrap() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-%zone(A2)");
        w.setCursor(pos(1));
        assertFalse(w.unwrapZone());
        assertEquals("A1-%zone(A2)", code(w));
    }

    @Test
    void zonesCanBeNested() throws Exception {
        JMDCEditorWorkflow w = workflow("%zone(A1-A2)");
        w.setCursor(pos(0, 0, 1));
        w.setMark(pos(0, 0, 2));
        assertTrue(w.addZone());
        assertEquals("%zone(A1-%zone(A2))", code(w));
    }

    @Test
    void noZoneInACartouche() throws Exception {
        JMDCEditorWorkflow w = workflow("<-A1->");
        w.setCursor(pos(0, 0, 0, 0, 0, 1));
        assertFalse(w.addZone());
        assertEquals("<-A1->", code(w));
    }

    @Test
    void typingInsideAZone() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-%zone(B1)");
        w.setCursor(pos(1, 0, 1));
        w.insertSpace();
        assertTrue(code(w).startsWith("A1-%zone(B1-"), code(w));
        assertTrue(code(w).endsWith(")"), code(w));
    }

    // ---- geometry ----

    private final JMDCEditor editor = new JMDCEditor();

    private TextGeometry geometry(String mdc) {
        editor.setMDCText(mdc);
        return new TextGeometry(editor.getView(), editor.getJSeshStyle());
    }

    @Test
    void theCursorIsDrawnInsideAZone() {
        TextGeometry geometry = geometry("A1-%zone(B1!B2)-A2");
        for (int i = 0; i <= 3; i++) {
            assertTrue(geometry.cursorAt(pos(1, 0, i)).isPresent(), "gap " + i);
        }
        // On two different lines.
        double first = geometry.cursorAt(pos(1, 0, 0)).get().bounds().getMinY();
        double last = geometry.cursorAt(pos(1, 0, 3)).get().bounds().getMinY();
        assertTrue(last > first, first + " / " + last);
    }

    @Test
    void aClickInAZoneGoesInsideIt() {
        TextGeometry geometry = geometry("A1-%zone(B1-B2!B3)-A2");
        for (int i : new int[] { 1, 3 }) {
            CursorShape cursor = geometry.cursorAt(pos(1, 0, i)).get();
            Line2D line = cursor.lines().get(0);
            Point2D middle = new Point2D.Double((line.getX1() + line.getX2()) / 2 + 1,
                    (line.getY1() + line.getY2()) / 2);
            assertEquals(pos(1, 0, i), geometry.positionAt(middle), "gap " + i);
        }
    }

    @Test
    void theViewFollowsEditsInsideAZone() {
        editor.setMDCText("A1-%zone(B1)");
        editor.getWorkflow().setCursor(pos(1, 0, 1));
        editor.getWorkflow().insertSpace();
        jsesh.render.view.MDCView zone = editor.getView().getSubView(1);
        assertEquals(2, zone.getFirstSubView().getNumberOfSubviews());
        editor.getWorkflow().undo();
        assertEquals(1, editor.getView().getSubView(1).getFirstSubView().getNumberOfSubviews());
    }

    @Test
    void aSelectionOnTwoLinesOfAZoneIsTwoAreas() {
        TextGeometry geometry = geometry("%zone(B1!B2)");
        SelectionShape shape = geometry.selection(TextScope.of(0, 0), 0, 3);
        assertEquals(2, shape.areas().size(), shape.toString());
    }
}
