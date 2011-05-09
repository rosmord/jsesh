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

import jsesh.jhotdraw.generic.ApplicationHelper;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.View;
import org.jhotdraw_7_4_1.app.action.AbstractApplicationAction;
import org.jhotdraw_7_4_1.app.action.file.OpenFileAction;
import org.jhotdraw_7_4_1.gui.JSheet;
import org.jhotdraw_7_4_1.gui.Worker;
import org.jhotdraw_7_4_1.net.URIUtil;
import org.jhotdraw_7_4_1.util.ResourceBundleUtil;

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
			openViewFromURI(view, uri);
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

	protected void openViewFromURI(final View view, final URI uri) {
		final Application app = getApplication();
		app.setEnabled(true);
		view.setEnabled(false);

		// If there is another view with the same URI we set the multiple open
		// id of our view to max(multiple open id) + 1.
		int multipleOpenId = 1;
		for (View aView : app.views()) {
			if (aView != view && aView.getURI() != null
					&& aView.getURI().equals(uri)) {
				multipleOpenId = Math.max(multipleOpenId,
						aView.getMultipleOpenId() + 1);
			}
		}
		view.setMultipleOpenId(multipleOpenId);
		view.setEnabled(false);

		// Open the file
		view.execute(new Worker() {

			@Override
			public Object construct() throws IOException {
				boolean exists = true;
				try {
					exists = new File(uri).exists();
				} catch (IllegalArgumentException e) {
				}
				if (exists) {
					view.read(uri, null);
					return null;
				} else {
					ResourceBundleUtil labels = ResourceBundleUtil
							.getBundle("org.jhotdraw_7_4_1.app.Labels");
					throw new IOException(labels.getFormatted(
							"file.open.fileDoesNotExist.message",
							URIUtil.getName(uri)));
				}
			}

			@Override
			protected void done(Object value) {
				final Application app = getApplication();
				view.setURI(uri);
				view.setEnabled(true);
				Frame w = (Frame) SwingUtilities.getWindowAncestor(view
						.getComponent());
				if (w != null) {
					w.setExtendedState(w.getExtendedState() & ~Frame.ICONIFIED);
					w.toFront();
				}
				view.getComponent().requestFocus();
				app.addRecentURI(uri);
				app.setEnabled(true);
			}

			@Override
			protected void failed(Throwable value) {
				value.printStackTrace();
				view.setEnabled(true);
				app.setEnabled(true);
				String message;
				if ((value instanceof Throwable)
						&& ((Throwable) value).getMessage() != null) {
					message = ((Throwable) value).getMessage();
					((Throwable) value).printStackTrace();
				} else if ((value instanceof Throwable)) {
					message = value.toString();
					((Throwable) value).printStackTrace();
				} else {
					message = value.toString();
				}
				ResourceBundleUtil labels = ResourceBundleUtil
						.getBundle("org.jhotdraw_7_4_1.app.Labels");
				JSheet.showMessageSheet(
						view.getComponent(),
						"<html>"
								+ UIManager.getString("OptionPane.css")
								+ "<b>"
								+ labels.getFormatted(
										"file.open.couldntOpen.message",
										URIUtil.getName(uri)) + "</b><p>"
								+ ((message == null) ? "" : message),
						JOptionPane.ERROR_MESSAGE);
			}
		});
	}
}
