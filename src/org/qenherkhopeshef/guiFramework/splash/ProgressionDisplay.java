package org.qenherkhopeshef.guiFramework.splash;

import java.awt.Graphics2D;

/**
 * Element responsible for drawing the advancement info on the splash screen.
 * @author rosmord
 */
public interface ProgressionDisplay {
	/**
	 * Report advancement.
	 * @param g2d
	 * @param width the available width
	 * @param height the available height
	 * @param progression the progression (meaning up to you).
	 */
	void drawAdvancement(Graphics2D g2d,int width, int height, int progression);
}
