package org.qenherkhopeshef.guiFramework.splash;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import org.qenherkhopeshef.swingUtils.GraphicsUtils;

/**
 * Ugly (as of today) clock display.
 * <p> I'll do a classier drawing when I have the time to do so.
 * @author rosmord
 *
 */
public class ClockDisplay implements ProgressionDisplay {


	public void drawAdvancement(Graphics2D g, int width, int height,
			int progression) {
		double centerX= width/ 2.0;
		double centerY= height/ 2.0;
		double size= Math.min(centerX, centerY) *0.66;
		double angle= progression * Math.PI / 30;
		double tickX= centerX+ size* Math.cos(angle);
		double tickY= centerY+ size* Math.sin(angle);
		Graphics2D g2d= (Graphics2D) g.create();
		GraphicsUtils.antialias(g2d);
		Color clockBG= new Color(100, 100, 100, 100);
		g2d.setColor(clockBG);
		g2d.fill(new Ellipse2D.Double(centerX-size-5, centerY-size-5, size*2+10, size*2+10));
		g2d.setColor(new Color(80,80,80));
		Stroke stroke= new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		g2d.setStroke(stroke);
		g2d.draw(new Line2D.Double(centerX, centerY,tickX, tickY));
		g2d.dispose();
	}

}
