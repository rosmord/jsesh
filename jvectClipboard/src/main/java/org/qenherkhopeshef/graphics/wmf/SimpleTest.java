/*
 * Created on 12 oct. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package org.qenherkhopeshef.graphics.wmf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;

import org.qenherkhopeshef.graphics.generic.BaseGraphics2D;



/**
 * TODO describe type 
 *   @author rosmord
 *
 */
public class SimpleTest
{

	public static void main(String[] args) throws IOException
	{
		BaseGraphics2D g= new WMFGraphics2D(new File("/home/rosmord/testwmf.wmf"), new Dimension(640, 300));	
		g.drawString("hello, ca va ?", 20, 20);
		g.setColor(Color.RED);
		Graphics2D tmpg=  (Graphics2D) g.create();
		g.drawLine(0, 0, 300, 300);
		tmpg.scale(0.5,0.5);
		tmpg.translate(100,100);
		tmpg.drawLine(0,0,400,400);
		// Shape s= new CubicCurve2D.Double(0,0, 40, 40, 100, 50, 200, 0);
		// tmpg.fill(s);
		g.drawLine(0, 0, 300, 300);
		tmpg.dispose();
		g.dispose();
	}
}
