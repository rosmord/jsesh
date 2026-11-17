/*
 * Created on 15 déc. 2003
 *
 * This code is distributed under the LGPL.
 *  
 */
package jsesh.easyweb;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
/**
 * Demonstration of uses of the "Dessinateur" class to draw hieroglyphs on an image.
 * voir aussi : uiswing/painting/usingImages.html
 * dans le tutorial de java.
 * 
 */

public class TestDessinateur {

	public static void main(String args[]) throws Exception {
		String text = "m-t:A-p*t:pt\\d";

		Dessinateur d = new Dessinateur();
		d.setSignHeight(100);
		d.setText(text);

		// On crée l'image.
		BufferedImage image =
			new BufferedImage(
				d.getWidth(),
				d.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		System.out.println(
			"On crée une image de " + d.getWidth() + " sur " + d.getHeight());
		Graphics2D g= image.createGraphics();
		g.setBackground(Color.WHITE);
		g.clearRect(0,0,d.getWidth(),d.getHeight());
		g.setColor(Color.BLACK);
		d.draw(g);

		String format = "png";
		File fic = new File("demoDrawer" + "." + format);
		ImageIO.write(image, format, fic);
	}
}