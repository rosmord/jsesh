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

import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.preferences.document.ui.PageSpecificationPresenter;
import jsesh.jhotdraw.utils.AbstractCoreViewAction;

/// Edits the page size and margins of the document, or removes the page.
@SuppressWarnings("serial")
public class EditPaginationAction extends AbstractCoreViewAction {

	public static final String ID = "file.pagination";

	public EditPaginationAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configureActionWithID(this, ID);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		viewCore().ifPresent(v -> {
			PageSpecificationPresenter presenter = new PageSpecificationPresenter();
			presenter.loadPreferences(v.getJSeshStyle());
			if (presenter.showDialog(v.getViewComponent()) == JOptionPane.OK_OPTION) {
				v.setJSeshStyle(presenter.updatePreferences(v.getJSeshStyle()));
			}
		});
	}
}
