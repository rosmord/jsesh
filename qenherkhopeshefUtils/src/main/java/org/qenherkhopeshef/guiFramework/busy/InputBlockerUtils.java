package org.qenherkhopeshef.guiFramework.busy;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class InputBlockerUtils {
	/**
	 * See to it that a given component intercept all input.
	 * 
	 * @param component
	 */
	public static void interceptInput(Component component) {
		component.addMouseListener(new MouseAdapter() {
		});
		component.addMouseMotionListener(new MouseMotionAdapter() {
		});
		component.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
			}
		});
		component.addKeyListener(new KeyAdapter() {
		});
		component.setFocusTraversalKeysEnabled(false);
		component.addComponentListener(new FocusRequester(component));
	}

	private static class FocusRequester extends ComponentAdapter {
		private Component component;

		public FocusRequester(Component component) {
			this.component = component;
		}

		@Override
		public void componentShown(ComponentEvent evt) {
			component.requestFocusInWindow();
		}
	}
}
