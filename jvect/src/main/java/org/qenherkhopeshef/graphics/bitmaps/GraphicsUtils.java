/*
 * Created on 30 nov. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.qenherkhopeshef.graphics.bitmaps;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Utility class for various graphics operations.
 */
public class GraphicsUtils {



	/**
	 * Sets antialiasing on a graphics context.
	 * @param g
	 */
	public static void antialias(Graphics g) {
		Graphics2D g2d= (Graphics2D) g;
		//RenderingHints h = new RenderingHints(g2d.getRenderingHints());
		RenderingHints h =g2d.getRenderingHints();
		h.put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		h.put(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHints(h);
	}
}
