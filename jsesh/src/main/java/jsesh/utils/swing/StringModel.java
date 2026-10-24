/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 * 
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 *
 * Created on 29 oct. 2004
 */
package jsesh.utils.swing;

import javax.swing.event.ChangeListener;

/// An observable text, to be displayed by a [JStringDisplayer].
///
/// @author S. Rosmorduc
public interface StringModel {

	String getText();

	/// Registers a listener to be notified each time the text changes.
	void addChangeListener(ChangeListener listener);

	/// Unregisters a listener previously added with
	/// [#addChangeListener(ChangeListener)].
	void removeChangeListener(ChangeListener listener);
}
