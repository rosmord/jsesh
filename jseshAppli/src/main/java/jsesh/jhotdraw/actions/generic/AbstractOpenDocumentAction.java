package jsesh.jhotdraw.actions.generic;

/*
 * @(#)OpenFileAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 *
 * The copyright of this software is owned by the authors and  
 * contributors of the JHotDraw project ("the copyright holders").  
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * the copyright holders. For details see accompanying license terms. 
 */

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;


import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractApplicationAction;
import org.jhotdraw_7_6.app.action.file.OpenFileAction;
import org.jhotdraw_7_6.gui.JSheet;
import org.jhotdraw_7_6.gui.Worker;
import org.jhotdraw_7_6.net.URIUtil;
import org.jhotdraw_7_6.util.ResourceBundleUtil;
import org.qenherkhopeshef.jhotdrawChanges.ApplicationHelper;

/**
 * ONGOING WORK!!!!
 * 
 * Generic document opener action. loads a document into an empty view. If no
 * empty view is available, a new view is created.
 * 
 * <p>
 * Adapted from Werner Randelshofer's OpenFileAction, but without the document
 * selection part.
 * 
 * <p>
 * We take care of keeping a code which might be used in a compatible way with
 * OpenFileAction (i.e. it should be possible to retrofit OpenFileAction to use
 * this code).
 * 
 *  see {@link OpenFileAction}
 * @author Werner Randelshofer, changes by S. Rosmorduc
 */
@SuppressWarnings("serial")
public abstract class AbstractOpenDocumentAction extends
		AbstractApplicationAction {

	/** Creates a new instance. */
	public AbstractOpenDocumentAction(Application app) {
		super(app);
	}

	/**
	 * perform the actual document opening.
	 */
	public void actionPerformed(ActionEvent e) {
		final Application app = getApplication();
		final View view = ApplicationHelper.findEmptyView(app);
		URI uri = getDocumentURI();

		if (uri != null) {
			app.show(view);
			new ViewOpenerForDocument(app).openViewFromURI(view, uri);
		}
	}

	/**
	 * Returns the document which should be opened, or null if no document was
	 * selected. This is where a document can be selected, for instance. Some
	 * subclasses might have constant URI returned.
	 * 
	 * @return an URI, or null if no document should be opened after all.
	 */
	protected abstract URI getDocumentURI();
}
