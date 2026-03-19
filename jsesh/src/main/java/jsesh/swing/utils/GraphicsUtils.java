/*
 * Created on 30 nov. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package jsesh.swing.utils;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility class for various graphics operations.
 * (copied in multiple versions) 
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

	/// Run some code with a temporary graphics and dispose of it at the end.
	/// 
	/// The graphics2D used will be antialiased.
	/// It's important that the graphics object is **new**, as it will be distroyed at the end of the method.
	/// 
	/// @param supplier creates a **new** graphics object
	/// @param consumer	uses this graphics object.	
	public static void doWithTemporaryGraphics(Supplier<Graphics> supplier, Consumer<Graphics2D> consumer) {
		Graphics g = supplier.get();
		try {
			Graphics2D g2d = (Graphics2D) g;
			// force antialiasing
			antialias(g2d);			
			consumer.accept(g2d);
		} finally {
			g.dispose();
		}
	}
}
