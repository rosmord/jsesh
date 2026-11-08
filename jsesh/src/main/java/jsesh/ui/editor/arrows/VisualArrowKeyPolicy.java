/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor.arrows;

import java.util.Optional;

import jsesh.model.HieroglyphicText;
import jsesh.model.MDCPosition;
import jsesh.model.PositionNavigator;
import jsesh.model.TextScope;
import jsesh.model.Zone;
import jsesh.model.ZoneContent;
import jsesh.model.constants.TextDirection;
import jsesh.ui.editor.JMDCEditor;
import jsesh.ui.editor.JMDCEditorWorkflow;

/// Arrows follow the screen: the right arrow moves the cursor to the right.
///
/// In left-to-right text, Right means "forward"; in right-to-left text,
/// "backward"; in columns, it moves to the column on the right. The direction
/// and orientation used are the ones of the text at the cursor (innermost zone).
///
/// At the boundary of a zone whose direction differs from the one around it,
/// the move follows what is seen on screen rather than the text order: from the
/// right edge of a right-to-left zone in a left-to-right text, going right puts
/// the cursor *after* the zone.
///
/// This was the behaviour of JSesh up to version 9.0.
public final class VisualArrowKeyPolicy implements ArrowKeyPolicy {

    @Override
    public ArrowKeyPolicies kind() {
        return ArrowKeyPolicies.VISUAL;
    }

    @Override
    public void move(JMDCEditor editor, ArrowKey key) {
        JMDCEditorWorkflow workflow = editor.getWorkflow();
        boolean horizontal = editor.getTextOrientationAtCaret().isHorizontal();
        boolean ltr = editor.getTextDirectionAtCaret().isLeftToRight();
        switch (key) {
            case LEFT, RIGHT -> {
                boolean right = key == ArrowKey.RIGHT;
                if (horizontal) {
                    cursorHorizontally(workflow, right, editor.getTextDirection());
                } else if (right == ltr) {
                    workflow.cursorDown();
                } else {
                    workflow.cursorUp();
                }
            }
            case UP -> {
                if (horizontal) {
                    workflow.cursorUp();
                } else {
                    workflow.cursorPrevious();
                }
            }
            case DOWN -> {
                if (horizontal) {
                    workflow.cursorDown();
                } else {
                    workflow.cursorNext();
                }
            }
        }
    }

    @Override
    public void extendSelection(JMDCEditor editor, ArrowKey key) {
        int dir = switch (key) {
            case LEFT -> -1;
            case RIGHT -> 1;
            case UP -> -2;
            case DOWN -> 2;
        };
        boolean horizontal = editor.getTextOrientationAtCaret().isHorizontal();
        int actualDir;
        if (editor.getTextDirectionAtCaret().isLeftToRight()) {
            if (horizontal) {
                actualDir = dir;
            } else {
                actualDir = switch (dir) {
                    case 1 -> 2;
                    case -1 -> -2;
                    case 2 -> 1;
                    default -> -1;
                };
            }
        } else {
            if (horizontal) {
                actualDir = (dir == 1 || dir == -1) ? -dir : dir;
            } else {
                actualDir = switch (dir) {
                    case 1 -> -2;
                    case -1 -> 2;
                    case 2 -> 1;
                    default -> -1;
                };
            }
        }
        editor.getWorkflow().expandSelection(actualDir);
    }

    @Override
    public void moveByWord(JMDCEditor editor, boolean leftKey, boolean extend) {
        boolean forward = leftKey != editor.getTextDirectionAtCaret().isLeftToRight();
        WordMoves.move(editor.getWorkflow(), forward, extend);
    }

    /// Moves the cursor one stop to the left or to the right, in horizontal text.
    ///
    /// In text of a single direction, this is
    /// [JMDCEditorWorkflow#cursorNext()] or
    /// [JMDCEditorWorkflow#cursorPrevious()], depending on the direction of the
    /// text at the cursor. At the boundary of a zone whose direction differs from
    /// the one around it, the move follows what is seen on screen (see the class
    /// documentation).
    ///
    /// @param workflow the editor's workflow.
    /// @param toRight true to move right, false to move left.
    /// @param fallback the direction of the text outside any zone setting one.
    public static void cursorHorizontally(JMDCEditorWorkflow workflow, boolean toRight, TextDirection fallback) {
        boolean forward = toRight == workflow.getTextDirectionAtCaret(fallback).isLeftToRight();
        MDCPosition from = workflow.getCaret().getInsertExactPosition();
        HieroglyphicText text = workflow.getCaret().getModel();
        MDCPosition normal = new PositionNavigator(text).move(from, forward ? 1 : -1,
                workflow.getCaretGranularity());
        Optional<MDCPosition> target = zoneBoundaryTarget(text, from, normal, toRight, forward, fallback);
        if (target.isEmpty()) {
            if (forward) {
                workflow.cursorNext();
            } else {
                workflow.cursorPrevious();
            }
            return;
        }
        workflow.setCursor(target.get());
        workflow.getCaret().unsetMark();
    }

    /// The text direction in force at a position: the one of the innermost
    /// enclosing zone which sets one, or `fallback`.
    private static TextDirection directionAt(HieroglyphicText text, MDCPosition position, TextDirection fallback) {
        TextScope scope = TextScope.containing(text, position);
        while (!scope.isRoot()) {
            if (scope.resolve(text) instanceof ZoneContent content
                    && content.getParent() instanceof Zone zone
                    && zone.getParameters().direction() != null) {
                return zone.getParameters().direction();
            }
            scope = scope.enclosing(text);
        }
        return fallback;
    }

    /// The position the cursor must go to, when a move along the text crosses
    /// the boundary of a zone whose direction differs from the one outside.
    ///
    /// Such a zone is a visual box: the arrow keys follow the *visual*
    /// direction, in the text which surrounds the zone. Leaving the zone through
    /// its right edge puts the cursor after it (if the text around is
    /// left-to-right), and entering it from the right lands on its right edge,
    /// whatever the direction of its own content.
    ///
    /// @param text the text.
    /// @param from the current position.
    /// @param normal the position a move by one stop along the text (in the
    ///        direction of the text at `from`) leads to.
    /// @param toRight true if the key moves to the right.
    /// @param forward true if the move goes forward in the text order.
    /// @param fallback the direction of the text outside any zone setting one.
    /// @return the position to go to, or empty if `normal` is the right one.
    private static Optional<MDCPosition> zoneBoundaryTarget(HieroglyphicText text, MDCPosition from,
            MDCPosition normal, boolean toRight, boolean forward, TextDirection fallback) {
        TextScope source = TextScope.containing(text, from);
        TextScope destination = TextScope.containing(text, normal);
        if (source.equals(destination)) {
            return Optional.empty();
        }
        if (source.resolve(text) instanceof ZoneContent && !source.contains(normal)) {
            // Leaving a zone.
            TextScope outer = source.enclosing(text);
            int index = outer.localIndexOf(MDCPosition.of(source.getPath()));
            boolean innerLtr = directionAt(text, from, fallback).isLeftToRight();
            boolean outerLtr = directionAt(text, outer.positionAt(index), fallback).isLeftToRight();
            if (innerLtr == outerLtr) {
                return Optional.empty();
            }
            boolean outerForward = toRight == outerLtr;
            return Optional.of(outer.positionAt(outerForward ? index + 1 : index));
        }
        if (!destination.contains(from)) {
            // Entering a zone.
            TextScope entered = destination;
            while (!entered.enclosing(text).contains(from)) {
                entered = entered.enclosing(text);
            }
            if (entered.resolve(text) instanceof ZoneContent content) {
                boolean innerLtr = directionAt(text, entered.positionAt(0), fallback).isLeftToRight();
                boolean outerLtr = directionAt(text, from, fallback).isLeftToRight();
                if (innerLtr != outerLtr) {
                    return Optional.of(entered.positionAt(forward ? content.getNumberOfChildren() : 0));
                }
            }
        }
        return Optional.empty();
    }
}
