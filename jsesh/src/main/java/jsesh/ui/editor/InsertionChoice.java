/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor;

import jsesh.model.Insert;

/// One of the ways of inserting a group into another, as proposed to the user.
///
/// @param insert the insertion (a model element which is not part of the text).
/// @param scale the scale of the inserted element, relative to its natural size.
/// @param fits false if the inserted element overlaps the main one.
/// @see JMDCEditorWorkflow#prepareChoiceOfInsertion(jsesh.render.layout.insert.InsertionRanker)
public record InsertionChoice(Insert insert, double scale, boolean fits) {
}
