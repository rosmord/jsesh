package jsesh.graphics.glyphs.svgFontsImporter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import jsesh.graphics.glyphs.model.SVGSignSource;
import jsesh.hieroglyphs.graphics.ShapeChar;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Complete import of a SVG font. TODO: transform this into a regular
 * SignSource, and then, rewrite the batch system for this sign source (or for
 * any ?)
 * 
 * @author admin
 * 
 */
public class SVGFontBatchImporter {

	public class SVGBatchHandler extends DefaultHandler {

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			try {
				if (localName.equals("glyph")) {
					// get data
					String pathDescr = attributes.getValue("", "d");
					String glyphName = attributes.getValue("", "glyph-name");
					if (pathDescr == null) {
						System.err.println("Glyph without drawing" + glyphName);
					} else if (glyphName == null || "".equals(glyphName)) {
						System.err.println("Glyph without name ");
					} else {
						if (glyphName.startsWith("J")) {
							glyphName= "Aa" + glyphName.substring(1);
						}
						
						// Write data into a byte array, because SVG reader
						// needs binary files.
						ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
						OutputStreamWriter writer = new OutputStreamWriter(
								byteArrayOutputStream, "UTF-8");
						writer.write("<svg>");
						writer.write("<path d='");
						writer.write(pathDescr);
						writer.write("'/>");
						writer.write("</svg>");
						writer.close();// just being polite.

						ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
								byteArrayOutputStream.toByteArray());
						SVGSignSource svgSource = new SVGSignSource(glyphName,
								byteArrayInputStream);
						svgSource.next(); // Go to the first (and only) shape
						ShapeChar shapeChar = svgSource.getCurrentShape(); // read
																			// it.

						// Transform it if needed.
						shapeChar.scaleGlyph(scale);
						if (vflip)
							shapeChar.flipVertically();
						// Now save it to the directory.
						File outFile = new File(directory, glyphName + ".svg");
						FileOutputStream out = new FileOutputStream(outFile);
						shapeChar.exportToSVG(out, "UTF-8");
					}
				}
			} catch (IOException e) {
				e.printStackTrace(); // Continue for other signs.
			}
		}

	}

	private File directory;

	private boolean vflip;

	private double scale;

	public void importFont(File fontFile, File directory, boolean vflip,
			double scale) throws SAXException, ParserConfigurationException,
			IOException {
		this.directory = directory;
		this.vflip = vflip;
		this.scale = scale;
		// read SVG
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		parserFactory.setValidating(false);
		parserFactory
				.setFeature("http://xml.org/sax/features/namespaces", true);
		parserFactory.setFeature(
				"http://xml.org/sax/features/namespace-prefixes", false);
		SAXParser parser = parserFactory.newSAXParser();
		parser.parse(fontFile, new SVGBatchHandler());

	}

	public static void main(String[] args) {
		if (args.length == 0 || args[0].startsWith("-h")
				|| args[0].startsWith("-?"))
			usage("");

		try {
			File fontFile = new File(args[0]);
			File directory = new File(args[1]);

			if (!fontFile.canRead())
				usage("Can't read " + args[0]);
			if (!directory.isDirectory())
				usage(args[1] + " is not a directory");
			double scale = Double.parseDouble(args[2]);
			boolean vflip = false;

			for (int i = 3; i < args.length; i++) {
				if (args[i].equals("vflip"))
					vflip = true;
			}

			new SVGFontBatchImporter().importFont(fontFile, directory, vflip,
					scale);
		} catch (ArrayIndexOutOfBoundsException e) {
			usage("wrong number of arguments");
		} catch (NumberFormatException e) {
			usage("bad number " + e.getLocalizedMessage());
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void usage(String msg) {
		if (!"".equals(msg))
			System.err.println(msg);
		System.err
				.println("Usage : SVGFontBatchImporter SVGSOURCE OUTPUTDIRECTORY SCALE [vflip]");
		System.exit(1);
	}
}
