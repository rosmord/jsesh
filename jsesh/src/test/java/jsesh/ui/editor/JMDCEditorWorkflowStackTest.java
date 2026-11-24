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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jsesh.document.HieroglyphicTextModel;
import jsesh.parser.MDCSyntaxError;

/// Building stacks (`%stack`) from the editor.
class JMDCEditorWorkflowStackTest {

    private JMDCEditorWorkflow workflow;

    @BeforeEach
    void setUp() {
        workflow = new JMDCEditorWorkflow(new HieroglyphicTextModel(), code -> null);
    }

    private void text(String mdc) throws MDCSyntaxError {
        workflow.setMDCCode(mdc);
        workflow.cursorToEndOfLine();
    }

    @Test
    void stacksTheTwoItemsBeforeTheCursor() throws MDCSyntaxError {
        text("A1-D36-t");
        assertTrue(workflow.groupAsStack());
        assertEquals("A1-%stack(D36,t)", workflow.getMDCCode());
    }

    @Test
    void needsTwoItems() throws MDCSyntaxError {
        text("D36");
        assertFalse(workflow.groupAsStack());
        assertEquals("D36", workflow.getMDCCode());
    }

    @Test
    void canStackGroups() throws MDCSyntaxError {
        text("D36:n-t*w");
        assertTrue(workflow.groupAsStack());
        assertEquals("%stack(D36:n,t*w)", workflow.getMDCCode());
    }
}
