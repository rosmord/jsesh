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

import javax.swing.JTextField;
import javax.swing.event.ChangeListener;

/// A read-only text field showing the text of a [StringModel].
///
/// The field follows the model: each time the model fires a change event,
/// the displayed text is refreshed. The field listens to the model only while
/// it is displayed (between `addNotify` and `removeNotify`), so that it
/// doesn't prevent a long-lived model from releasing it.
///
/// @author S. Rosmorduc
public class JStringDisplayer extends JTextField {

	private final StringModel model;

	private final ChangeListener modelListener = e -> setText(getModel().getText());

	public JStringDisplayer(StringModel model) {
		this(model, 0);
	}

	public JStringDisplayer(StringModel model, int columns) {
		super(model.getText(), columns);
		this.model = model;
		setEditable(false);
		setFocusable(false);
	}

	/// The model whose text is displayed.
	public StringModel getModel() {
		return model;
	}

	@Override
	public void addNotify() {
		super.addNotify();
		model.addChangeListener(modelListener);
		// The model may have changed while we weren't listening.
		setText(model.getText());
	}

	@Override
	public void removeNotify() {
		model.removeChangeListener(modelListener);
		super.removeNotify();
	}
}
