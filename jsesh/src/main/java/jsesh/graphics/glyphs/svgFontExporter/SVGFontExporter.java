package jsesh.graphics.glyphs.svgFontExporter;

import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Iterator;
import java.util.TreeMap;

import jsesh.hieroglyphs.data.HieroglyphDatabaseRepository;
import jsesh.hieroglyphs.graphics.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.data.GardinerCode;
import jsesh.hieroglyphs.data.HieroglyphDatabaseInterface;
import jsesh.hieroglyphs.graphics.HieroglyphicFontManager;
import jsesh.hieroglyphs.graphics.ShapeChar;

/**
 * Software to create an Unicode and EGPZ compliant SVG font (which can be made
 * into a TTF Font through fontforge).
 * <p>
 * Some modifications are needed, as the Manuel de Codage codes and the Unicode
 * or EGPZ names are not <em>always</em> the same.
 * <p>
 * Could be moved to jsesh.utilitySoftware ??
 *
 * @author rosmord
 */
public class SVGFontExporter {

    enum Area {
        EGZP, UNICODE, PRIVATE
    };

    private class GlyphData {

        Area table = Area.UNICODE;
        String mdcCode;
        String UnicodeName;
        String position;

        public GlyphData(String mdcCode, String position, Area table) {
            super();
            this.mdcCode = mdcCode;
            this.position = position;
            this.table = table;
        }

        public GlyphData(String mdcCode, String unicodeName, String position, Area table) {
            super();
            this.mdcCode = mdcCode;
            this.position = position;
            this.table = table;
            this.UnicodeName = unicodeName;
        }

        void outputGlyph(Writer writer, String path, double width)
                throws IOException {
            writer.write("<glyph ");
            switch (table) {
                case EGZP:
                    writer.write("glyph-name='" + "EGPZ_" + mdcCode + "' ");
                    break;
                case PRIVATE:
                    writer.write("glyph-name='PRIV_" + mdcCode + "' ");
                    break;
                case UNICODE:
                    writer.write("glyph-name='" + UnicodeName + "' ");
            }
            writer.write("unicode='&#x" + position + ";' ");
            writer.write(" horiz-adv-x='" + width + "'");
            writer.write(" d='");
            writer.write(path);
            writer.write("'/>\n");
        }

        public void generateFontGlyph(Writer writer,
                HieroglyphicFontManager fontManager) throws IOException {
            // If the glyph is not a ligature, gets the sign.
            // Ligature will be dealt with later...
            if (mdcCode.indexOf('_') == -1) {
                String glyphCode = null;

                if (table == Area.EGZP) {
                    // System.err.print("for "+ glyph);
                    // Trick: we use the file name conversion provided by the
                    // GardinerCode class:
                    glyphCode = GardinerCode.getCodeForFileName(mdcCode);
                } else {
                    HieroglyphDatabaseInterface manager = HieroglyphDatabaseRepository.getHieroglyphDatabase();
                    glyphCode = manager.getCanonicalCode(mdcCode);
                }
                // End of code = capital letters
                ShapeChar sign = fontManager.get(glyphCode);
                if (sign == null) {
                    System.err.println("glyph " + mdcCode + " (" + table
                            + ") not found, was " + glyphCode);
                    return;
                }
                sign = (ShapeChar) sign.clone();
                // Ensure the glyph dimensions are limited:
                // height no more than 18, width no more than 36.
                double scale = 1.0;
                Rectangle2D box = sign.getBbox();
                if (box.getHeight() > 18) {
                    scale = 18 / box.getHeight();
                }
                if (box.getWidth() > 36) {
                    double hscale = 36 / box.getWidth();
                    if (hscale < scale) {
                        scale = hscale;
                    }
                }

                sign.flipVertically();
                sign.scaleGlyph(scale * 100 * 20.0 / 18.0);
                StringWriter stringWriter = new StringWriter();
                sign.writeSVGPath(stringWriter, 0);
                double width = sign.getBbox().getWidth();
                outputGlyph(writer, stringWriter.toString(), width);
                // System.err.println("Exporting "+ glyph+ " to "+ codePoint);
            }

        }

    }

    // maps code points to sign codes..
    private TreeMap<String, GlyphData> codeMap = new TreeMap<String, GlyphData>();

    public SVGFontExporter() {
        try {
            //System.err.println("reading EGPZ");
            //readEGPZDef();
            System.err.println("reading Unicode");
            readUnicode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //
    // Format : for each sign, we have the following attributes for the "glyph"
    // element
    // glyph-name: the glyph name as per EGPZ
    // unicode: a string of the form "&#xE014;"
    // d: the drawing of the sign.
    /**
     * Reads the egpz.h file.
     *
     * @param r
     * @throws IOException
     */
    private void readEGPZDef() throws IOException {
        InputStream in = SVGFontExporter.class.getResourceAsStream("egpz.h");
        BufferedReader r = new BufferedReader(
                new InputStreamReader(in, "ASCII"));
        String line;
        while ((line = r.readLine()) != null) {
            if (line.startsWith("#define EGPZ")) {
                // Extract the code...
                // Note: "_" (except the first one) in codes mean ligature:
                // EGPZ_N21_G25_Z1
                // is N21&G25&Z1
                int startGlyphName = line.indexOf('_');
                int endGlyphName = line.indexOf('\t', startGlyphName);
                String glyphName = line.substring(startGlyphName + 1,
                        endGlyphName);

                // extract the codePoint
                int startCodePoint = line.indexOf("0x");
                String pos = line.substring(startCodePoint + 2); // Suppresses
                // the "0x"
                // System.err.print(glyphName+ "=>");
                // System.err.print(pos);

                // Ensure the codepoint is a correct hex number.
                Integer.parseInt(pos, 16);
                // If there is an error, the software fail. Normally, we then
                // need to correct egpz.h

                // Ok, now we fill the map:
                codeMap.put(pos, new GlyphData(glyphName, pos, Area.EGZP));
            }
        }
    }

    /**
     * Read the Gardiner-code-to-Unicode list.
     *
     * <p>
     * Note : some codes are DIFFERENT between standard MdC and Unicode.
     * <p>
     * JSesh follows the de facto standard as we want portability with existing
     * texts.
     * <p>
     * (even though we criticized the original Unicode proposal based on it...
     * scientific sanity and pratical issues are two different things !).
     *
     * @throws IOException
     * @throws NumberFormatException
     */
    public void readUnicode() throws NumberFormatException, IOException {
        InputStream in = SVGFontExporter.class
                .getResourceAsStream("MdC2Unicode-table.txt");
        BufferedReader r = new BufferedReader(
                new InputStreamReader(in, "ASCII"));
        String line;
        while ((line = r.readLine()) != null) {
            if (line.startsWith("#") || line.trim().length() == 0) {
                continue;
            }
            String[] fields = line.split("\t");
            String mdcName = fields[0];
            String UnicodeName = fields[1];
            int codePoint = Integer.parseInt(fields[2].substring(2), 16);

            // Ok, now we fill the map:
            //codeMap.put(codePoint, new GlyphData(glyphName, pos, Area.PRIVATE));
            codeMap.put("" + codePoint,
                    new GlyphData(mdcName, UnicodeName, Integer.toHexString(codePoint)
                            .toUpperCase(), Area.UNICODE));
        }
    }

    public void generateFont(Writer writer) throws IOException {
        writeHeader(writer);
        DefaultHieroglyphicFontManager fontManager = DefaultHieroglyphicFontManager
                .getInstance();
        Iterator<String> it = codeMap.keySet().iterator();
        while (it.hasNext()) {
            String codePoint = it.next();
            GlyphData glyph = codeMap.get(codePoint);
            glyph.generateFontGlyph(writer, fontManager);

        }
        writeFooter(writer);
        writer.close();
    }

    private void writeFooter(Writer writer) throws IOException {
        writer.write("</font>\n</svg>\n");
    }

    private void writeHeader(Writer writer) throws IOException {
        writer.write("<?xml version='1.0' standalone='no'?>\n");
        writer.write("<!DOCTYPE svg PUBLIC '-//W3C//DTD SVG 1.1//EN' 'http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd' >\n");
        writer.write("<svg version='1.0'>\n");
        writer.write("<font id='JSeshFont'>\n");
        writer.write("<font-face font-family='JSesh font' units-per-em='2048'"
                + " font-weight='400'" + " ascent='2000'" + " descent='-48'"
                + " x-height='2000'" + " cap-height='2000'"
                + " underline-position='-55'" + " bbox='0 0 4000 2000'" + ">\n");
        writer.write("</font-face>\n");
        // glyphs to generate:
        writer.write("<missing-glyph horiz-adv-x='2000' />\n");
        // writer.write("<glyph glyph-name='.notdef' horiz-adv-x='2000' />\n");
        // writer.write("<glyph glyph-name='.null' horiz-adv-x='2000' />\n");
        writer.write("<glyph glyph-name='space' unicode=' ' horiz-adv-x='2000' />\n");
    }

    public static void main(String[] args) throws UnsupportedEncodingException,
            IOException {
        SVGFontExporter s = new SVGFontExporter();
        s.generateFont(new File("jseshFont.svg"));
    }

    private void generateFont(File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        Writer writer = new OutputStreamWriter(out, "UTF8");
        generateFont(writer);
    }
}
