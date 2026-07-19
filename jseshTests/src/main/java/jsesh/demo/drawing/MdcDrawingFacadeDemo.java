package jsesh.demo.drawing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.draw.MDCDrawingFacade;
import jsesh.render.style.JSeshStyle;

public class MdcDrawingFacadeDemo {
    
	public static void main(String[] args) {
		System.out.println("Test of MDCDrawingFacade");
		JSeshRenderContext ctx = new JSeshRenderContext(JSeshStyle.DEFAULT, PredefinedFonts.buildAllEmbeddedFonts());
		MDCDrawingFacade facade = new MDCDrawingFacade(ctx);
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
