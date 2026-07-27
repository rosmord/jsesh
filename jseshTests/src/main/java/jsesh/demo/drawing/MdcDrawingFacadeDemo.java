package jsesh.demo.drawing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import jsesh.parser.MDCSyntaxError;
import jsesh.render.draw.MDCDrawingFacade;

public class MdcDrawingFacadeDemo {

	public static void main(String[] args) {
		System.out.println("Test of MDCDrawingFacade");
		MDCDrawingFacade facade = MDCDrawingFacade.builder().build();
		String mdc = "i-w-r:a-ra-m-p*t:pt";
		try {
			BufferedImage img = facade.createImage(mdc);
			ImageIO.write(img, "png", new File("testPict.png"));
			System.out.println("Image created : " + img.getWidth() + " x " + img.getHeight());
		} catch (MDCSyntaxError | IOException e) {
			e.printStackTrace();
		}
	}

}
