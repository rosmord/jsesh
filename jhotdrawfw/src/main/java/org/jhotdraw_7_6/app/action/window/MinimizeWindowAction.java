/*
 * @(#)MinimizeWindowAction.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */

package org.jhotdraw_7_6.app.action.window;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;
import org.jhotdraw_7_6.util.ResourceBundleUtil;

/**
 * Minimizes the Frame of the current view.
 * 
 * @author Werner Randelshofer
 * @version $Id$
 */
@SuppressWarnings("serial")
public class MinimizeWindowAction extends AbstractViewAction {
	public final static String ID = "window.minimize";

	/** Creates a new instance. */
	public MinimizeWindowAction(Application app, View view) {
		super(app, view);
		ResourceBundleUtil labels = ResourceBundleUtil
				.getBundle("org.jhotdraw_7_6.app.Labels");
		labels.configureAction(this, ID);
	}

	private JFrame getFrame() {
		return (JFrame) SwingUtilities.getWindowAncestor(getActiveView()
				.getComponent());
	}

	public void actionPerformed(ActionEvent evt) {
		JFrame frame = getFrame();
		System.out.println("HERE*********************");
		if (frame != null) {
			frame.setExtendedState(frame.getExtendedState() ^ Frame.ICONIFIED);
		} else {
			Toolkit.getDefaultToolkit().beep();
		}
	}
}