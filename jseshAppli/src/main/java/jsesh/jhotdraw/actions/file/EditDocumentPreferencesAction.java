/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 * 
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;

import jsesh.render.style.JSeshStyle;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.preferences.document.ui.DrawingSpecificationsPresenter;
import jsesh.jhotdraw.utils.AbstractCoreViewAction;

@SuppressWarnings("serial")
public class EditDocumentPreferencesAction extends AbstractCoreViewAction {

	public EditDocumentPreferencesAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configureActionWithID(this, ID);
	}

	public static final String ID = "file.documentProperties";

	public void actionPerformed(ActionEvent event) {
		viewCore().ifPresent(
				v -> {
					DrawingSpecificationsPresenter presenter = new DrawingSpecificationsPresenter();
					presenter
							.loadPreferences(v.getJSeshStyle());
					if (presenter.showDialog(v.getViewComponent()) == JOptionPane.OK_OPTION) {
						JSeshStyle newStyle = presenter.updatePreferences(v.getJSeshStyle());
						v.setJSeshStyle(newStyle);
					}
				});
	}
}
