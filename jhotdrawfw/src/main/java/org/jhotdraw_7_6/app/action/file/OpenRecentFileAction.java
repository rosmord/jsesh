/*
 * @(#)OpenRecentFileAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package org.jhotdraw_7_6.app.action.file;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractApplicationAction;
import org.jhotdraw_7_6.gui.JSheet;
import org.jhotdraw_7_6.gui.Worker;
import org.jhotdraw_7_6.gui.event.SheetEvent;
import org.jhotdraw_7_6.gui.event.SheetListener;
import org.jhotdraw_7_6.net.URIUtil;
import org.jhotdraw_7_6.util.ResourceBundleUtil;

/**
 * Loads the specified URI into an empty view. If no empty view is available, a
 * new view is created.
 * <p>
 * This action is called when the user selects an item in the Recent Files
 * submenu of the File menu. The action and the menu item is automatically
 * created by the application, when the {@code ApplicationModel} provides a
 * {@code OpenFileAction}.
 * 
 * @author Werner Randelshofer.
 * @version $Id: OpenRecentFileAction.java 717 2010-11-21 12:30:57Z rawcoder $
 */
public class OpenRecentFileAction extends AbstractApplicationAction {

	public final static String ID = "file.openRecent";
	private URI uri;

	/** Creates a new instance. */
	public OpenRecentFileAction(Application app, URI uri) {
		super(app);
		this.uri = uri;
		putValue(Action.NAME, URIUtil.getName(uri));
	}

	public void actionPerformed(ActionEvent evt) {
		final Application app = getApplication();
		if (app.isEnabled()) {
			// A missing file must not open a window: the new empty window
			// would fight with the error sheet (and the previously active
			// window) for the focus.
			if (!fileExists()) {
				View active = app.getActiveView();
				showOpenError(active == null ? null : active.getComponent(),
						missingFileMessage(), null);
				return;
			}
			app.setEnabled(false);
			// Search for an empty view
			View emptyView = app.getActiveView();
			if (emptyView == null || !emptyView.isEmpty()
					|| !emptyView.isEnabled()) {
				emptyView = null;
			}

			final View p;
			final boolean newView = emptyView == null;
			if (newView) {
				p = app.createView();
				app.add(p);
				app.show(p);
			} else {
				p = emptyView;
			}
			openView(p, newView);
		}
	}

	/**
	 * Checks that the file exists. URIs which don't denote a file can't be
	 * checked, and are assumed to exist.
	 */
	private boolean fileExists() {
		try {
			return new File(uri).exists();
		} catch (IllegalArgumentException e) {
			return true;
		}
	}

	private String missingFileMessage() {
		ResourceBundleUtil labels = ResourceBundleUtil
				.getBundle("org.jhotdraw_7_6.app.Labels");
		return labels.getFormatted("file.open.fileDoesNotExist.message",
				URIUtil.getName(uri));
	}

	/**
	 * Shows the "could not open" error.
	 * 
	 * @param parent
	 *            the component whose window gets the sheet. If null, a plain
	 *            dialog is used.
	 * @param message
	 *            the reason.
	 * @param listener
	 *            notified when the sheet is dismissed (may be null).
	 */
	private void showOpenError(Component parent, String message,
			SheetListener listener) {
		ResourceBundleUtil labels = ResourceBundleUtil
				.getBundle("org.jhotdraw_7_6.app.Labels");
		String text = "<html>"
				+ UIManager.getString("OptionPane.css")
				+ "<b>"
				+ labels.getFormatted("file.open.couldntOpen.message",
						URIUtil.getName(uri)) + "</b><p>" + message;
		if (parent == null) {
			JOptionPane.showMessageDialog(null, text, null,
					JOptionPane.ERROR_MESSAGE);
			if (listener != null) {
				listener.optionSelected(null);
			}
		} else if (listener == null) {
			JSheet.showMessageSheet(parent, text, JOptionPane.ERROR_MESSAGE);
		} else {
			JSheet.showMessageSheet(parent, text, JOptionPane.ERROR_MESSAGE,
					listener);
		}
	}

	/**
	 * @param view
	 *            the view to load the file into.
	 * @param newView
	 *            true if the view was created for this file, and should be
	 *            discarded if loading fails.
	 */
	protected void openView(final View view, final boolean newView) {
		final Application app = getApplication();
		app.setEnabled(true);

		// If there is another view with the same URI we set the multiple open
		// id of our view to max(multiple open id) + 1.
		int multipleOpenId = 1;
		for (View aView : app.views()) {
			if (aView != view && aView.isEmpty()) {
				multipleOpenId = Math.max(multipleOpenId,
						aView.getMultipleOpenId() + 1);
			}
		}
		view.setMultipleOpenId(multipleOpenId);
		view.setEnabled(false);

		// Open the file
		view.execute(new Worker() {

			protected Object construct() throws IOException {
				// Safety net: the file may have disappeared since the check
				// in actionPerformed.
				if (!fileExists()) {
					throw new IOException(missingFileMessage());
				}
				view.read(uri, null);
				return null;
			}

			protected void done(Object value) {
				view.setURI(uri);
				Frame w = (Frame) SwingUtilities.getWindowAncestor(view
						.getComponent());
				if (w != null) {
					w.setExtendedState(w.getExtendedState() & ~Frame.ICONIFIED);
					w.toFront();
				}
				view.setEnabled(true);
				view.getComponent().requestFocus();
			}

			@Override
			protected void finished() {
				app.recomputeWindowMenu();
			}

			protected void failed(Throwable value) {
				value.printStackTrace();
				String message = value.getMessage() != null ? value
						.getMessage() : value.toString();
				showOpenError(view.getComponent(), message,
						new SheetListener() {

							public void optionSelected(SheetEvent evt) {
								if (newView) {
									// Don't leave an empty window behind.
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											app.dispose(view);
										}
									});
								} else {
									view.setEnabled(true);
								}
							}
						});
			}
		});
	}
}
