package jsesh.utilitySoftwares;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import jsesh.hieroglyphs.data.HieroglyphDatabaseRepository;
import jsesh.hieroglyphs.graphics.ShapeChar;

public class MakeGlyphsIndex {

    static final String USAGE = "usage : "
            + "\n java " + MakeGlyphsIndex.class.getName() + "[-sizes] FORMAT  [FAMILY]"
            + "\n\twhere "
            + "\n\t\tFORMAT is \"png\" or \"svg\""
            + "\n\t\tFAMILY is the name of a gardiner font family, or US + user id"
            + "\n\tif no family is provided, all known fonts will be dumped."
            + "\n\tif -sizes is specified, a text file sizes.csv will be produced.";

    // Dirty trick.
    static StringBuilder signSizes= new StringBuilder();
    
    static boolean sizes= false;
    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {        
        int pos= 0;
        if (args.length == 0) {
            displayUsage();            
        }
        if ("-sizes".equals(args[0])) {
            pos++;
            sizes= true;
        }
        if (pos >= args.length)
            displayUsage();        
        String format = args[pos++];
        String family = "";
        if (args.length > pos) {
            family = args[pos++];
        }
        if (args.length > pos) {
            displayUsage();
        }
        if (!"png".equals(format) && !"svg".equals(format)) {
            displayUsage();
        }

        String codes[] = (String[]) HieroglyphDatabaseRepository.getHieroglyphDatabase().getCodesForFamily(family, true).toArray(new String[0]);
        for (String code : codes) {
            dumpSign(code, format);
        }
        if (sizes) {
            FileWriter w= new FileWriter("sizes.csv");
            w.write(signSizes.toString());
            w.close();
        }

    }

    private static void dumpSign(String code, String format) throws IOException {
        jsesh.hieroglyphs.graphics.DefaultHieroglyphicFontManager manager = jsesh.hieroglyphs.graphics.DefaultHieroglyphicFontManager.getInstance();
        ShapeChar s = manager.get(code);
        if ("png".equals(format)) {
            jsesh.hieroglyphs.graphics.HieroglyphicBitmapBuilder bitmapBuilder = new jsesh.hieroglyphs.graphics.HieroglyphicBitmapBuilder();
            bitmapBuilder.setFit(true);
            BufferedImage bitmap = bitmapBuilder.buildSignBitmap(s);
            ImageIO.write(bitmap, "png", new java.io.File(code + ".png"));
        } else if ("svg".equals(format)) {
            java.io.FileOutputStream out = new java.io.FileOutputStream(code + ".svg");
            s.exportToSVG(out, "UTF-8", true);
        }
        if (sizes) {
            Rectangle2D box = s.getBbox();
            signSizes.append(code).append(';')
                    .append(box.getMinX()).append(" ")
                    .append(box.getMinY()).append(' ')
                    .append(box.getWidth()).append(' ')
                    .append(box.getHeight());
            signSizes.append('\n');
        }

    }

    public static void displayUsage() {
        System.err.println(USAGE);
        System.exit(1);
    }
}
