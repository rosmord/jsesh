/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.qenherkhopeshef.jhotdrawChanges.ApplicationHelper;

import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.actions.generic.TextLibraryURI;
import jsesh.jhotdraw.actions.generic.ViewOpenerForDocument;
import jsesh.jhotdraw.dialogs.TextLibraryFrame;
import jsesh.jhotdraw.utils.AbstractCoreApplicationAction;
import jsesh.jseshtexts.JSeshTextLibrary;

/// Show the text library browser. Choosing a text opens it in an empty (or new) view.
public class TextLibraryAction extends AbstractCoreApplicationAction {
    public static final String ID = "file.textLibrary";

    private TextLibraryFrame frame;

    public TextLibraryAction(Application app) {
        super(app);
        BundleHelper.getInstance().configureActionWithID(this, ID);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (frame == null) {
            frame = new TextLibraryFrame(JSeshTextLibrary.getInstance().getEntries(), this::open);
        }
        frame.setVisible(true);
        frame.toFront();
        frame.focusSearchField();
    }

    private void open(JSeshTextLibrary.Entry entry) {
        Application app = getApplication();
        View view = ApplicationHelper.findEmptyView(app);
        app.show(view);
        new ViewOpenerForDocument(app).openViewFromURI(view, TextLibraryURI.forLibraryPath(entry.path()));
    }
}
