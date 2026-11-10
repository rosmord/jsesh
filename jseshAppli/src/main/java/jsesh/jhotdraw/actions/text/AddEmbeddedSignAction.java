/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.actions.text;

import java.awt.event.ActionEvent;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;

import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.utils.AbstractCoreViewAction;
import jsesh.ui.widgets.embeddedsigns.EmbeddedSignOperations;

/// Adds a sign drawn by the user (an SVG file) to the current document, and
/// inserts it at the cursor.
///
/// The sign is stored in the document, with the code `SIGN1`, `SIGN2`...
@SuppressWarnings("serial")
public class AddEmbeddedSignAction extends AbstractCoreViewAction {

    public static final String ID = "text.addEmbeddedSign";

    public AddEmbeddedSignAction(Application app, View view) {
        super(app, view);
        BundleHelper.getInstance().configureActionWithID(this, ID);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        viewCore().ifPresent(v -> EmbeddedSignOperations
                .addFromSvgFile(v.getViewComponent(), v.getEmbeddedSigns(), getCurrentDirectory())
                .ifPresent(sign -> {
                    v.getEditor().insert(sign.code());
                    v.getEditor().requestFocusInWindow();
                }));
    }
}
