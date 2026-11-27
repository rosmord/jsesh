/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.actions.file;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.SwingUtilities;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;

import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.actions.generic.TemplateURI;
import jsesh.jhotdraw.actions.generic.ViewOpenerForDocument;
import jsesh.jhotdraw.dialogs.NewFromTemplateDialog;
import jsesh.jhotdraw.documentview.JSeshView;
import jsesh.jhotdraw.utils.AbstractCoreApplicationAction;
import jsesh.jseshtemplates.JSeshTemplateLibrary;

/// Let the user choose a template, and create a new text from it.
///
/// The new text uses the active view if it is a new, empty one; otherwise it
/// gets a view of its own.
public class NewFromTemplateAction extends AbstractCoreApplicationAction {
    public static final String ID = "file.newFromTemplate";

    public NewFromTemplateAction(Application app) {
        super(app);
        BundleHelper.getInstance().configureActionWithID(this, ID);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        View activeView = getApplication().getActiveView();
        Window owner = activeView == null ? null
                : SwingUtilities.getWindowAncestor(activeView.getComponent());
        new NewFromTemplateDialog(owner, JSeshTemplateLibrary.getInstance().getTemplates())
                .showDialog()
                .ifPresent(this::createDocument);
    }

    private void createDocument(JSeshTemplateLibrary.Template template) {
        Application app = getApplication();
        View view = app.getActiveView();
        if (!isNewEmptyView(view)) {
            view = app.createView();
            view.setMultipleOpenId(nextUntitledId(app));
            app.add(view);
        }
        int untitledId = view.getMultipleOpenId();
        app.show(view);
        new ViewOpenerForDocument(app).openViewFromURI(view, TemplateURI.forTemplatePath(template.path()));
        // Opening numbers the view according to its URI, but this one will be untitled.
        view.setMultipleOpenId(untitledId);
    }

    /// Can this view be reused without losing anything?
    private static boolean isNewEmptyView(View view) {
        return view instanceof JSeshView jseshView
                && view.isEnabled()
                && view.getURI() == null
                && !view.hasUnsavedChanges()
                && jseshView.getMdcDocument().getHieroglyphicText().asList().isEmpty();
    }

    /// The number to give to a new untitled view, as `File > New` does.
    private static int nextUntitledId(Application app) {
        int id = 1;
        for (View view : app.views()) {
            if (view.getURI() == null) {
                id = Math.max(id, view.getMultipleOpenId() + 1);
            }
        }
        return id;
    }
}
