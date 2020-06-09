package jsesh.utilitySoftwares;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import jsesh.graphics.glyphs.model.SVGSignSource;
import jsesh.hieroglyphs.data.GardinerCode;
import jsesh.hieroglyphs.graphics.ShapeChar;
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
			+ "java jsesh.utilitySoftwares.MakeSVGIndex [-links] DIRECTORY [DIRECTORY ...]\n"
			+ "or\n"
			+ "java jsesh.utilitySoftwares.MakeSVGIndex [-links] FILE \n"
			+ "build an index.html with png pictures of the svg files in a directory."
			+ "\nalso create a zipped version of the directory."
			+ "\nif a file README.html is available, it will be inserted at the beginning of the file."
			+ "\nThe README.html file should not contain <html> or <body> tags"
			+ "\n\tfor each sign, if a .txt file with the same name of the sign is found, it's used as a commentary."
			+ "\nThe software can also use the 'description' field of SVG files (see inkscape, document preferences)"
			+ "\n"
			+ "If a file is given as argument, it is supposed to be a text file, \n"
			+ "and each line is supposed to contain the name of a font directory.\n"
			+ " if '-links' is specified, a link to each individual SVG file will be built."
			;

	private static final String XSLFILTER = ""
			+ "<xsl:stylesheet version=\"1.0\"  xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"  xmlns:dc='http://purl.org/dc/elements/1.1/' >"
			+ "<xsl:output method='text'/>"
			// Erase text
			+ "<xsl:template match='text()|@*'>"
			+ "</xsl:template>"
			// get description
			+ "<xsl:template match='//dc:description'>"
			+ "<xsl:value-of select='.'/>" + "</xsl:template>"
			+ "</xsl:stylesheet>";

	private static boolean buildLinks= false;

	/**
	 * Read a file, containing a number of directory names.
	 * 
	 * @param file
	 */
	private static void parseDirectoryList(File file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				File dir = new File(line);
				if (dir.isDirectory()) {
					System.out.println("working on " + dir);
					makeIndex(dir);
				} else {
					System.err.println("" + dir
							+ " does not exists or is not a directory");
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println(e.getLocalizedMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void makeIndex(File directory) throws IOException {
		StringBuffer buffer = new StringBuffer();
		makeHeader(buffer);
		makeReadme(directory, buffer);
		makeBody(directory, buffer);
		makeFooter(buffer);
		FileOutputStream binary = new FileOutputStream(new File(directory,
				"index.html"));
		java.io.OutputStreamWriter index = new OutputStreamWriter(binary,
				"UTF-8");
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
		buffer
				.append("<META HTTP-EQUIV='Content-Type' CONTENT='text/html; charset=UTF-8'>");
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

		Arrays.sort(files, new Comparator() {

			public int compare(Object o1, Object o2) {
				File f1 = (File) o1;
				File f2 = (File) o2;
				String s1 = GardinerCode.getCodeForFileName(f1.getName());
				if (s1 == null)
					s1 = f1.getName();
				String s2 = GardinerCode.getCodeForFileName(f2.getName());
				if (s2 == null)
					s2 = f2.getName();
				return GardinerCode.compareCodes(s1, s2);

			}

		});
		buffer.append("<ul>\n");
		for (int i = 0; i < files.length; i++) {
			File imageFile = FileUtils.buildFileWithExtension(files[i], "png");

			try {

				buildImageFile(files[i], imageFile);

				buffer.append("<li>");
				buffer.append(files[i].getName());
				if (buildLinks) {
					buffer.append("<a href=\"");
					buffer.append(files[i].getName());
					buffer.append("\"> ");
				}
				buffer.append("<img src=\"");
				buffer.append(imageFile.getName());
				File commentFile = FileUtils.buildFileWithExtension(files[i],
						"txt");
				buffer.append("\"> ");
				if (buildLinks)
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
				byte[] bytes = new byte[(int) files[i].length()];
				// a svg file
				// too long for
				// an int can
				// not exist.
				// really.
				svgin.read(bytes);
				svgin.close();
				zipOut.write(bytes);
				zipOut.closeEntry();
			} catch (Exception e) {
				System.err.println("Could not build " + imageFile);
				e.printStackTrace();
			}
		}
		buffer.append("</ul>\n");
		zipOut.close();
		buffer
				.append("<b> directory content as <a href=\"svgsigns.zip\"> zip file </a>");
	}

	/**
	 * Extract Sign description from the sign XML file.
	 * 
	 * @param file
	 */
	private static String extractXMLInfo(File file) {
		String result = "";

		// We use XSLT to transform our file. Alas, it's awfully difficult to
		// switch out validation when the file contains a DOCTYPE declaration.
		// I know there are cases where validation is needed, but this is
		// upsetting.
		// 
		// We should probably simply use SAX to parse the file.
		TransformerFactory factory2 = TransformerFactory.newInstance();
		Source source = new StreamSource(new StringReader(XSLFILTER));
		try {
			// We start building a transformer to modify our file.
			Transformer f = factory2.newTransformer(source);
			StringWriter stringWriter = new StringWriter();
			StreamResult outputTarget = new StreamResult(stringWriter);
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			// We create a dummy resolver to replace all dtds...
			xmlReader.setEntityResolver(new EntityResolver() {
				public InputSource resolveEntity(String publicId,
						String systemId) throws SAXException, IOException {
					return new InputSource(new StringReader(
							"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));

				}
			});
			SAXSource source1 = new SAXSource(xmlReader, new InputSource(
					new FileInputStream(file)));
			// The transformer is ready. Apply it.
			f.transform(source1, outputTarget);
			result = stringWriter.toString().trim();
		} catch (Exception e) {
			System.err.println("problem reading information for"
					+ file.getAbsolutePath());
			e.printStackTrace();
			result = "";
		}
		return result;
	}

	private static void buildImageFile(File svgFile, File imageFile)
			throws IOException {
		// Do not recreate existing png...
		if (imageFile.exists() && imageFile.length() > 0
				&& imageFile.lastModified() > svgFile.lastModified())
			return;
		System.err.println("working on "+ svgFile);
		SVGSignSource source = new SVGSignSource(svgFile);
		if (source.hasNext()) {
			source.next();
			ShapeChar shapeChar = source.getCurrentShape();

			jsesh.hieroglyphs.graphics.HieroglyphicBitmapBuilder builder = new jsesh.hieroglyphs.graphics.HieroglyphicBitmapBuilder();
			builder.setSize(56);
			builder.setMaxSize(20);
			builder.setFit(true);
			builder.setTransparent(true);
			BufferedImage b = builder.buildSignBitmap(shapeChar);
			ImageIO.write(b, "png", imageFile);
		}
	}

	/**
	 * Create the Readme header.
	 * 
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

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			displayUsage();
		}
		int start = 0;
		if ("-links".equals(args[0])) {
			buildLinks= true;
			start=1;
		}
		File directories[] = new File[args.length];
		for (int i = start; i < args.length; i++) {
			directories[i] = new File(args[i]);
			if (!directories[i].isDirectory()) {
				if (!directories[i].exists()) {
					System.err
							.println(directories[i].getName()
									+ " is not a directory or a text file with a list of directories in it.");
					displayUsage();
				} else
					parseDirectoryList(directories[i]);
			} else {
				System.out.println("working on " + directories[i].getName());
				makeIndex(directories[i]);
			}
		}
	}

}
