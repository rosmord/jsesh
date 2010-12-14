package jsesh.utilitySoftwares;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import jsesh.hieroglyphs.CompositeHieroglyphsManager;
import jsesh.hieroglyphs.ShapeChar;

public class MakeGlyphsIndex {
	static final String USAGE = "usage : " +
			"\n java " + MakeGlyphsIndex.class.getName() + "FORMAT  [FAMILY]" +
					"\n\twhere " +
					"\n\t\tFORMAT is \"png\" or \"svg\"" +
					"\n\t\tFAMILY is the name of a gardiner font family, or US + user id" +
					"\n\tif no family is provided, all known fonts will be dumped." ;
		
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length > 2 || args.length == 0)
			displayUsage();
		String format= args[0];
		String family= "";
		if (args.length == 2)
		{
			family= args[1];
		}
		if (! "png".equals(format) && ! "svg".equals(format))
			displayUsage();
				
		String codes[]= (String[]) CompositeHieroglyphsManager.getInstance().getCodesForFamily(family, false).toArray(new String[0]);
		for (int i=0; i< codes.length; i++) {
			dumpSign(codes[i], format);
		}
		
	}

	private static void dumpSign(String code, String format) throws IOException {
		jsesh.hieroglyphs.DefaultHieroglyphicFontManager manager= jsesh.hieroglyphs.DefaultHieroglyphicFontManager.getInstance();
		ShapeChar s = manager.get(code);
		if ("png".equals(format)) {
			jsesh.hieroglyphs.HieroglyphicBitmapBuilder bitmapBuilder= new jsesh.hieroglyphs.HieroglyphicBitmapBuilder();
			bitmapBuilder.setFit(true);
			BufferedImage bitmap = bitmapBuilder.buildSignBitmap(s);
			ImageIO.write(bitmap, "png", new java.io.File(code + ".png"));
		} else if ("svg".equals(format)) {
			java.io.FileOutputStream out= new java.io.FileOutputStream(code+ ".svg");
			s.exportToSVG(out, "UTF-8");
		}
		
	}

	public static void displayUsage() {
		System.err.println(USAGE);
		System.exit(1);
	}
}
