/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor.arrows;

import jsesh.ui.editor.JMDCEditorWorkflow;

/// Word moves shared by the policies, once they know whether the move goes
/// forward.
final class WordMoves {

    private WordMoves() {
    }

    static void move(JMDCEditorWorkflow workflow, boolean forward, boolean extend) {
        if (extend) {
            workflow.expandSelectionByWord(forward ? 1 : -1);
        } else if (forward) {
            workflow.cursorNextWord();
        } else {
            workflow.cursorPreviousWord();
        }
    }
}
