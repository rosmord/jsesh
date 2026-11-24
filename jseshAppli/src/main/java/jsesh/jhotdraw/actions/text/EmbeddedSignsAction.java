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

import com.formdev.flatlaf.util.UIScale;

import jsesh.jhotdraw.JSeshStartup;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.utils.AbstractCoreViewAction;
import jsesh.defaults.HieroglyphResources;
import jsesh.render.draw.MDCIconFactory;
import jsesh.ui.widgets.embeddedsigns.EmbeddedSignsDialog;

/// Opens the list of the signs embedded in the current document (`sign1`...),
/// to add, insert, edit, remove or export them.
@SuppressWarnings("serial")
public class EmbeddedSignsAction extends AbstractCoreViewAction {

    public static final String ID = "text.embeddedSigns";

    public EmbeddedSignsAction(Application app, View view) {
        super(app, view);
        BundleHelper.getInstance().configureActionWithID(this, ID);
    }

    /// Icon factory for the dialog: it draws with the shapes of the view (which
    /// include the embedded signs), at the icon height of the scaled interface.
    private static MDCIconFactory icons(HieroglyphResources resources) {
        MDCIconFactory factory = new MDCIconFactory(resources.hieroglyphShapeRepository());
        factory.setCadratHeight(UIScale.scale(JSeshStartup.ACTION_ICON_HEIGHT));
        return factory;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        viewCore().ifPresent(v -> {
            EmbeddedSignsDialog dialog = new EmbeddedSignsDialog(v.getViewComponent(), v.getEmbeddedSigns(),
                    v.getHieroglyphResources().hieroglyphShapeRepository(), v.getHieroglyphicText(),
                    code -> {
                        v.getEditor().insert(code);
                        v.getEditor().requestFocusInWindow();
                    },
                    getCurrentDirectory(), icons(v.getHieroglyphResources()));
            dialog.show();
            if (dialog.getDirectory() != null) {
                setCurrentDirectory(dialog.getDirectory());
            }
        });
    }
}
