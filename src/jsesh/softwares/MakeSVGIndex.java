package jsesh.softwares;

import java.util.Arrays;
import java.util.zip.ZipOutputStream;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import jsesh.bzr.simple.ShapeChar;
import jsesh.externalFonts.model.SVGSignSource;
import jsesh.utils.FileUtils;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Build an index of the SVG files contained in a directory. Useful for web
 * publication.
 * 
 * @author Serge Rosmorduc
 * 
 */
public class MakeSVGIndex {

	static final String USAGE = "usage : \n"
			+ "java jsesh.softwares.MakeSVGIndex DIRECTORY \n"
			+ "build an index.html with png pictures of the svg files in a directory."
			+ "\nalso create a zipped version of the directory."
			+ "\nif a file README.html is available, it will be inserted at the beginning of the file."
			+ "\nThe README.html file should not contain <html> or <body> tags"
			+ "\n\tfor each sign, if a .txt file with the same name of the sign is found, it's used as a commentary."
			+ "\nThe software can also use the 'description' field of SVG files (see inkscape, document preferences)";

	private static final String XSLFILTER = ""
			+ "<xsl:stylesheet version=\"1.0\"  xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"  xmlns:dc='http://purl.org/dc/elements/1.1/' >"
			+ "<xsl:output method='text'/>"
			// Erase text
			+ "<xsl:template match='text()|@*'>" + "</xsl:template>"
			// get description
			+ "<xsl:template match='//dc:description'>"
			+ "<xsl:value-of select='.'/>" + "</xsl:template>"
			+ "</xsl:stylesheet>";

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			displayUsage();
		}
		File directories[] = new File[args.length];
		for (int i = 0; i < args.length; i++) {
			directories[i] = new File(args[i]);
			if (!directories[i].isDirectory()) {
				System.err.println(directories[i].getName() +" is not a directory");
				displayUsage();
			}
		}
		for (int i = 0; i < args.length; i++) {
			System.out.println("working on "+ directories[i].getName());
			makeIndex(directories[i]);
		}
	}

	private static void makeIndex(File directory) throws IOException {
		StringBuffer buffer = new StringBuffer();
		makeHeader(buffer);
		makeReadme(directory, buffer);
		makeBody(directory, buffer);
		makeFooter(buffer);
		java.io.FileWriter index = new java.io.FileWriter(new File(directory,
				"index.html"));
		index.write(buffer.toString());
		index.close();
	}

	private static void makeFooter(StringBuffer buffer) {
		buffer.append("</body>\n");
		buffer.append("</html>\n");

	}

	private static void makeHeader(StringBuffer buffer) {
		buffer.append("<html>\n");
		buffer.append("<head>");
		buffer.append("<title>");
		buffer.append("SVG hieroglyphs");
		buffer.append("</title>");
		buffer.append("</head>");
		buffer.append("<body>\n");
	}

	private static void makeBody(File directory, StringBuffer buffer)
			throws IOException {
		File[] files = directory.listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.getName().toLowerCase().endsWith(".svg");
			}
		});

		ZipOutputStream zipOut = new ZipOutputStream(
				new java.io.FileOutputStream(
						new File(directory, "svgsigns.zip")));

		Arrays.sort(files);
		buffer.append("<ul>\n");
		for (int i = 0; i < files.length; i++) {
			buffer.append("<li>");
			buffer.append(files[i].getName());
			buffer.append("<a href=\"");
			buffer.append(files[i].getName());
			buffer.append("\"> ");
			buffer.append("<img src=\"");
			File imageFile = FileUtils.buildFileWithExtension(files[i], "png");
			buffer.append(imageFile.getName());
			buildImageFile(files[i], imageFile);
			File commentFile = FileUtils
					.buildFileWithExtension(files[i], "txt");
			buffer.append("\"> ");
			buffer.append("</a>\n");
			buffer.append(extractXMLInfo(files[i]));
			if (commentFile.exists()) {
				Reader commentReader = new FileReader(commentFile);
				int c;
				while ((c = commentReader.read()) != -1) {
					buffer.append((char) c);
				}
				commentReader.close();
			}
			java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(
					"svgsigns/" + files[i].getName());
			zipOut.putNextEntry(entry);
			java.io.FileInputStream svgin = new java.io.FileInputStream(
					files[i]);
			byte[] bytes = new byte[(int) files[i].length()]; // a svg file
			// too long for
			// an int can
			// not exist.
			// really.
			svgin.read(bytes);
			svgin.close();
			zipOut.write(bytes);
			zipOut.closeEntry();
		}
		buffer.append("</ul>\n");
		zipOut.close();
		buffer
				.append("<b> directory content as <a href=\"svgsigns.zip\"> zip file </a>");
	}

	/**
	 * @param file
	 */
	private static String extractXMLInfo(File file) {
		String result = "";
		
		// We use XSLT to transform our file. Alas, it's awfully difficult to 
		// switch out validation when the file contains a DOCTYPE declaration.
		// I know there are cases where validation is needed, but this is upsetting.
		// 
		// We should probably simply use SAX to parse the file.
		TransformerFactory factory2 = TransformerFactory.newInstance();
	
		Source source = new StreamSource(new StringReader(XSLFILTER));
		try {
			Transformer f = factory2.newTransformer(source);
			StringWriter stringWriter = new StringWriter();
			StreamResult outputTarget = new StreamResult(stringWriter);
			
			
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			xmlReader.setEntityResolver(new EntityResolver() {

				public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
						return new InputSource(new StringReader(
								"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
					
				}
			});
			SAXSource source1= new SAXSource(xmlReader, new InputSource(new FileInputStream(file)));
			f.transform(source1, outputTarget);
			
			result = stringWriter.toString().trim();
		} catch (TransformerConfigurationException exception) {
			exception.printStackTrace();
		} catch (TransformerException exception) {		
			exception.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	private static void buildImageFile(File svgFile, File imageFile)
			throws IOException {
		SVGSignSource source = new SVGSignSource(svgFile);
		if (source.hasNext()) {
			source.next();
			ShapeChar shapeChar = source.getCurrentShape();

			jsesh.hieroglyphs.HieroglyphicBitmapBuilder builder = new jsesh.hieroglyphs.HieroglyphicBitmapBuilder();
			builder.setSize(56);
			builder.setMaxSize(20);
			builder.setFit(true);
			builder.setTransparent(true);
			BufferedImage b = builder.buildSignBitmap(shapeChar);
			/*
			 * // On crée l'image. final int HEIGHT = 56; final double SCALE=
			 * 2.5;
			 * 
			 * BufferedImage image = new BufferedImage(HEIGHT, HEIGHT,
			 * BufferedImage.TYPE_INT_ARGB); Graphics2D g =
			 * image.createGraphics(); g.setBackground(new Color(0, 0, 0, 1));
			 * g.clearRect(0, 0, HEIGHT, HEIGHT); GraphicsUtils.antialias(g);
			 * g.scale(SCALE, SCALE);
			 * 
			 * double h = shapeChar.getBbox().getHeight(); double w =
			 * shapeChar.getBbox().getWidth();
			 * 
			 * g.translate((HEIGHT/SCALE)/2.0-w/2.0, HEIGHT/SCALE-h-2);
			 * g.setColor(Color.BLACK); g.fill(shapeChar.getShape());
			 * g.dispose(); String format = "png";
			 */
			ImageIO.write(b, "png", imageFile);
		}
	}

	/**
	 * @param directory
	 * @param buffer
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void makeReadme(File directory, StringBuffer buffer)
			throws FileNotFoundException, IOException {
		File readme = new File(directory, "README.html");
		if (readme.exists()) {
			Reader in = new FileReader(readme);
			int c;
			while ((c = in.read()) != -1) {
				buffer.append((char) c);
			}
			in.close();
		}
	}

	private static void displayUsage() {
		System.err.println(USAGE);
		System.exit(1);
	}

}
