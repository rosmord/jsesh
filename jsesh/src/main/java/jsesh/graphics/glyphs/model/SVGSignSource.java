package jsesh.graphics.glyphs.model;

import jsesh.hieroglyphs.graphics.VerticalGravity;
import jsesh.hieroglyphs.graphics.ShapeChar;
import jsesh.hieroglyphs.graphics.LigatureZone;
import jsesh.hieroglyphs.graphics.HorizontalGravity;
import jsesh.hieroglyphs.data.GardinerCode;
import java.awt.BasicStroke;
import java.awt.geom.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.*;

import javax.xml.parsers.*;


import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Simple SVG importer for characters in JSesh.
 * <p>
 * The SVG parsing system used is rather basic and certainly not fullproof.
 * Currently, I would rather not include the 4megs of Batik into my software,
 * but frankly, SVG is quite complex and chosed the way of "there's more than
 * one way to write it".
 * 
 * <p>
 * Currently, we can read the output of : potrace, autotrace, files from
 * inkscape, sketch and open office.
 * <p>
 * BTW, I'm really glad the JDK team created the Area class. It's so powerful
 * that I feel I'm cheating when I'm using it :-)
 * 
 * <p>
 * <b>Ligature zones </b> TODO : write doc.
 * <ul>
 * <li>TODO implement a larger part type filter textof SVG (static graphics,
 * ignoring spurious stuff like animation).
 * <li>TODO take viewBox into account (not a priority)
 * <li>
 * </ul>
 * 
 * 
 * @author rosmord
 * 
 */

public class SVGSignSource implements SimpleSignSourceModel {

	/*
	 * Misc. notes about SVG. numbers in svg attributes are of the form
	 * "-?(\d*\.\d+|\d+)" units are : px (pixel), pt (point), em, ex, pc, mm,
	 * cm, in
	 */
	private ShapeChar shape;

	private String code;

	/**
	 * pos is used to comply with the new ResultSet-Like interface. Possible
	 * values are -1 (beforefirst), 0 (on element), or 1 (afterlast)
	 */
	private int pos;

	public SVGSignSource(File file) {
		try {
			initSVGSignSource(file.toURI().toURL());
		} catch (Exception e) {
			String errorSource = file.getPath();
			processError(e, errorSource);
		}
	}

	/**
	 * Error processing. Do not stop the software from working, but indicate
	 * that the sign can't be read.
	 * 
	 * @param e
	 * @param errorSource
	 */
	private void processError(Exception e, String errorSource) {
		System.err.println("Error reading " + errorSource);
		e.printStackTrace();
		this.code = "Error " + errorSource;
		this.shape = new ShapeChar();
		this.shape.setShape(new Rectangle2D.Double(0, 0, 14, 14));
	}

	/*
	 * public SVGSignSource(File file) {
	 * 
	 * try { // look if file names might correspond to a code. code =
	 * getCodeForFile(file);
	 * 
	 * if (code == null) code = ""; // read SVG SAXParserFactory parserFactory =
	 * SAXParserFactory.newInstance(); parserFactory.setValidating(false);
	 * parserFactory.setFeature("http://xml.org/sax/features/namespaces", true);
	 * parserFactory.setFeature(
	 * "http://xml.org/sax/features/namespace-prefixes", false); SAXParser
	 * parser = parserFactory.newSAXParser();
	 * 
	 * SVGReader handler = new SVGReader(); parser.parse(file, handler); shape =
	 * new ShapeChar(); shape.setShape(handler.getGeneralPath());
	 * shape.fixShape(); } catch (ParserConfigurationException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } catch (SAXException e)
	 * { // TODO Auto-generated catch block e.printStackTrace(); } catch
	 * (IOException e) { // TODO Auto-generated catch block e.printStackTrace();
	 * } beforeFirst(); }
	 */
	public SVGSignSource(URL url) {
		try {
			initSVGSignSource(url);
		} catch (Exception e) {
			String errorSource = url.toExternalForm();
			processError(e, errorSource);
		}
	}

	public SVGSignSource(String signCode, InputStream in) {
		try {
			initSVGSignSource(signCode, in, signCode);
		} catch (Exception e) {
			String errorSource = signCode;
			processError(e, errorSource);
		}
	}

	private void initSVGSignSource(URL url) throws IOException {
		String ressourceName = url.toString();
		// look if file names might correspond to a code.
		InputStream in = url.openStream();
		initSVGSignSource(ressourceName, in, getCodeForURL(url));
	}

	private void initSVGSignSource(String ressourceName, InputStream in,
			String signCode) throws FactoryConfigurationError {
		try {
			code = signCode;
			if (code == null)
				code = "";
			// read SVG
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			parserFactory.setValidating(false);
			parserFactory.setFeature("http://xml.org/sax/features/namespaces",
					true);
			parserFactory.setFeature(
					"http://xml.org/sax/features/namespace-prefixes", false);
			SAXParser parser = parserFactory.newSAXParser();

			SVGReader handler = new SVGReader();
			parser.parse(in, handler);
			shape = new ShapeChar();
			if (handler.getZones() != null) {
				for (int i = 0; i < handler.getZones().length; i++) {
					shape.setZone(i, handler.getZones()[i]);
				}
			}
			shape.setShape(handler.getGeneralPath());
			// shape.fixShape();
			// A1 sign is supposed to be 1800px high.
			// however, in old versions, it was 18px high
			// Hence this conditional scaling.
			// Temporary system to use signs with height 1800px by default
			if (shape.getBbox().getHeight() > 60) {
				double newHeight = shape.getBbox().getHeight() / 100.0;
				shape.scaleToHeight(newHeight);
			}
			simplifyShape();
			shape.setAuthor(handler.author);
			shape.setDocumentation(handler.description);
		} catch (ParserConfigurationException e) {
			processError(e, ressourceName);
			// In a future life, throw some kind of "BadFontException".
		} catch (SAXException e) {
			processError(e, ressourceName);
		} catch (IOException e) {
			processError(e, ressourceName);
		}
		beforeFirst();
	}

	/**
	 * 
	 */
	private void simplifyShape() {

	}

	public ShapeChar getCurrentShape() {
		return shape;
	}

	public boolean hasNext() {
		return pos == -1;
	}

	public boolean hasPrevious() {
		return pos == 1;
	}

	public void next() {
		if (hasNext())
			pos++;
	}

	public void previous() {
		if (hasPrevious())
			pos--;
	}

	public void beforeFirst() {
		pos = -1;
	}

	public void afterLast() {
		pos = 1;
	}

	static class Style {
		// In a next version, we might have something like a fill colour.
		// As the bloody SVG standard has at least 3 ways of writing colours,
		// it will be some work.

		/**
		 * If the shaped is filled, is it in black ?
		 */
		boolean blackPaint = true;

		/**
		 * Is the shape filled, in the first place ? (both this and the previous
		 * boolean could be replaced by "Color fillColor")
		 */

		boolean filled = true;

		/**
		 * Should the outline be drawn ?
		 */
		boolean outlined = false;

		float strokeWidth = 0f;

		public Style() {
		}

		public Style(Style parent) {
			if (parent != null) {
				blackPaint = parent.blackPaint;
				outlined = parent.outlined;
				filled = parent.filled;
				strokeWidth = parent.strokeWidth;
			}
		}

		public Style(Style parent, Attributes attributes) {
			this(parent);
			String styleDef = attributes.getValue("", "style");
			if (styleDef != null) {
				parseStyleDef(styleDef);
			}
			parseAttributes(attributes);
		}

		private void parseAttributes(Attributes attributes) {
			// If a colour different from black is specified, the fill color is
			// not black!
			// We should probably do the contrary, this would be more robust.
			// colour
			String fillColorAttr = attributes.getValue("", "fill");
			if (fillColorAttr != null) {
				if ("none".equals(fillColorAttr))
					filled = false;
				else {
					// If any fill color is specified, the filling is not in
					// black...
					blackPaint = false;
					// except if the said colour is black !
					if (fillColorAttr.startsWith("#000000"))
						blackPaint = true;
					if ("black".equals(fillColorAttr))
						blackPaint = true;
					if (fillColorAttr.startsWith("rgb(0,0,0"))
						blackPaint = true;
					if (fillColorAttr.startsWith("currentColor"))
						blackPaint = true;
				}
			}

			// Now, outlines/strokes
			if ("none".equals(attributes.getValue("", "stroke")))
				outlined = false;
			else if (attributes.getValue("", "stroke") != null) {
				outlined = true;
			}

			if (attributes.getValue("stroke-width") != null) {
				String w = attributes.getValue("stroke-width");
				int end = w.length() - 1;
				if (end >= 0) {
					while (end >= 0 && !Character.isDigit(w.charAt(end)))
						end--;
					this.strokeWidth = Float
							.parseFloat(w.substring(0, end + 1));
				} else
					strokeWidth = 0f;

			}
		}

		private void parseStyleDef(String styleDef) {
			if (styleDef == null)
				return;

			// filled is inherited.
			// If filled is explicitly set to none, filled is false.
			if (styleDef.indexOf("fill:none") != -1)
				filled = false;

			else // Look if we have some fill colour information.
			if (styleDef.indexOf("fill:") != -1) {
				filled = true;
				if (styleDef.indexOf("fill:#000000") != -1
						|| styleDef.indexOf("fill:rgb(0,0,0)") != -1
						|| styleDef.indexOf("fill:black") != -1)
					blackPaint = true;
				else
					// All other colours will be considered as "unfilling"
					blackPaint = false;
			}

			/**
			 * Line colour. We assume it's always black. if
			 * (styleDef.indexOf("stroke:#000000") != -1 ||
			 * styleDef.indexOf("stroke:black") != -1 ||
			 * styleDef.indexOf("stroke:rgb(0,0,0)") != -1) blackPaint = true;
			 */

			/*
			 * Should we outline ?
			 */
			if (styleDef.indexOf("stroke:none") != -1) {
				outlined = false;
			} else if (styleDef.indexOf("stroke:") != -1) {
				outlined = true;
			}

			/**
			 * Compute stroke-width.
			 */

			int i = styleDef.indexOf("stroke-width:");
			if (i != -1) {
				String widthDef;
				// go to the ':' after stroke-width
				i = styleDef.indexOf(':', i);
				i++;
				// find the end of this attribute.
				int end = styleDef.indexOf(';', i);
				if (end == -1)
					end = styleDef.length() - 1;
				while (!Character.isDigit(styleDef.charAt(end)))
					end--;
				widthDef = styleDef.substring(i, end + 1);
				strokeWidth = Float.parseFloat(widthDef);
			}

		}
	}

	// We currently use SAX. However, as we start using the extra information
	// provided by the DUBLIN CORE,
	// We may find it useful to go to DOM one of these days.

	class SVGReader extends DefaultHandler {

		public static final String INKSCAPE_NAMESPACES = "http://www.inkscape.org/namespaces/inkscape";
		public static final String DUBLIN_CORE_NAMESPACE = "http://purl.org/dc/elements/1.1/";
		/**
		 * For ligature zones. Defined as anchor tags in the source, with title
		 * either "zone1" or "zone2".
		 * 
		 * zoneNames may also be simply the Ids of rectangles. in this case, the
		 * label may contain additionnal information (like gravity anchor
		 * point). TODO : add a "gravity" system.
		 */

		private String zoneName = null;

		private LigatureZone[] zones;

		private Area area;

		private Stack<Style> styles;

		boolean inPattern = false; // don't take into account paths in pattern
									// definition.

		private int clipPathDepth = 0; // Don't take into account path in
										// clippath either...

		String description = "";

		String author = "";

		String license;

		StringBuffer currentText = new StringBuffer();

		boolean recordText = false;

		public SVGReader() {
			area = new Area();
			styles = new Stack<Style>();
			zones = null;
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (recordText)
				currentText.append(ch, start, length);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
		 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			// Style is inherited from container.
			Style parentStyle = null;

			if (!styles.isEmpty())
				parentStyle = styles.peek();

			// Create the current style, and select it.
			Style style = new Style(parentStyle, attributes);
			styles.push(style);

			// Now, look at the element.
			// first, anchors, which are used for ligature area (in a somehow
			// unorthodox way)
			if ("a".equals(localName)) {
				readAnchor(attributes);
			} else if ("rect".equals(localName)) {
				// In anchors, rectangles are used to define ligature zones.
				// Out of them, as well, if their label is zone...
				if (attributes.getValue("id") != null
						&& attributes.getValue("id").startsWith("zone")) { // "new"
					// system:
					// zone
					// defined
					// with
					// Ids..
					zoneName = attributes.getValue("", "id");
					defineZone(attributes);
					zoneName = null;
				} else if (zoneName != null) // zone defined with links
					defineZone(attributes);
			} else if ("path".equals(localName)) {
				// Now, the usual case, paths
				// the "d" attribute contains the path data.
				String d = attributes.getValue("", "d");
				GeneralPath subPath = parsePath(d);
				// Add the path to the currently built area.
				if (subPath != null) {
					addPathToArea(style, subPath);
				}
			} else if ("polygon".equals(localName)) {

				GeneralPath subPath = new GeneralPath();

				String pointsDef = attributes.getValue("", "points");
				if (pointsDef != null) {
					String[] ps = pointsDef.split(" ");
					for (int i = 0; i < ps.length; i++) {
						String[] coords = ps[i].split(",");
						if (coords.length != 2)
							return;
						else {
							if (i == 0)
								subPath.moveTo(Float.parseFloat(coords[0]),
										java.lang.Float.parseFloat(coords[1]));
							else
								subPath.lineTo(Float.parseFloat(coords[0]),
										java.lang.Float.parseFloat(coords[1]));
						}
					}
					if (!style.outlined) {
						subPath.closePath();
					}
					addPathToArea(style, subPath);
				}
			} else if ("pattern".equals(localName)) {
				inPattern = true;
			} else if ("clipPath".equals(localName)) {
				clipPathDepth++;
			} else if (isDCDescription(uri, localName)) {
				// dc:description
				recordText = true;
				currentText.setLength(0);
			} else if (isDCCreator(uri, localName)) {
				recordText = true;
				currentText.setLength(0);
			}
		}

		private boolean isDCDescription(String uri, String localName) {
			return "description".equals(localName)
					&& DUBLIN_CORE_NAMESPACE.equals(uri);
		}

		private boolean isDCCreator(String uri, String localName) {
			return "creator".equals(localName)
					&& DUBLIN_CORE_NAMESPACE.equals(uri);
		}

		private void addPathToArea(Style style, GeneralPath subPath) {
			// No path defined inside a pattern should be taken into account
			if (inPattern || clipPathDepth > 0)
				return;
			if (style.outlined) {
				area.add(new Area(new BasicStroke(style.strokeWidth)
						.createStrokedShape(subPath)));
			}
			if (style.filled) {
				if (style.blackPaint) {
					area.add(new Area(subPath));
				} else {
					area.subtract(new Area(subPath));
				}
			}
		}

		/**
		 * Define a ligature zone for this sign. Zones have a name (currently
		 * zone1 and zone2). They will also have a gravity, which will explain
		 * how group will be fitted in them.
		 * 
		 * The label field can contain gravity information (ok, it would be
		 * cleaner to use a proper XML attribute with some kind of extended DTD,
		 * but then, we want the user to be able to set this easily).
		 * 
		 * Zone1 and Zone2 have some kind of "default" gravity (which is
		 * center). Form of the label field: gravity:... example: "gravity:st".
		 * 
		 * possible values s,e,t,b,st,sb,et,eb
		 * 
		 * s and e are horizontal positions start and end. b and t are vertical
		 * positions top and bottom.
		 * 
		 * (this is taken from RES).
		 * 
		 * @param attributes
		 */
		private void defineZone(Attributes attributes) {
			int zoneId = 0; // Default : zone1
			if ("zone2".equals(zoneName)) {
				zoneId = 1;
			}
			if (zones == null)
				zones = new LigatureZone[2];
			double values[] = new double[4];
			String attrNames[] = { "x", "y", "width", "height" };
			for (int i = 0; i < 4; i++) {
				try {
					values[i] = java.lang.Double.parseDouble(attributes
							.getValue("", attrNames[i]));
				} catch (NumberFormatException exception) {
					exception.printStackTrace();
				}
			}

			zones[zoneId] = new LigatureZone(new Rectangle2D.Double(values[0],
					values[1], values[2], values[3]));

			// Deal with gravity (and possibly other additional features).
			String label = attributes.getValue("", "label");
			if (label == null)
				label = attributes.getValue(INKSCAPE_NAMESPACES, "label");
			if (label != null) {
				String[] additionnalData = label.split("; *");
				for (int i = 0; i < additionnalData.length; i++) {
					String args[] = additionnalData[i].split(":");
					if (args != null && args.length == 2) {
						if (args[0].equals("gravity")) {
							for (int k = 0; k < args[1].length(); k++) {
								switch (args[1].charAt(k)) {
								case 't':
									zones[zoneId]
											.setVerticalGravity(VerticalGravity.TOP);
									break;
								case 'b':
									zones[zoneId]
											.setVerticalGravity(VerticalGravity.BOTTOM);
									break;
								case 's':
									zones[zoneId]
											.setHorizontalGravity(HorizontalGravity.START);
									break;
								case 'e':
									zones[zoneId]
											.setHorizontalGravity(HorizontalGravity.END);
									break;
								}

							}
						}

					}
				}
			}
		}

		/**
		 * @param attributes
		 */
		private void readAnchor(Attributes attributes) {
			zoneName = null;
			// We could simply use http://www.w3.org/1999/xlink as URI for
			// xlink, but I'm not that confident.
			for (int i = 0; zoneName == null && i < attributes.getLength(); i++) {
				if ("title".equals(attributes.getLocalName(i)))
					zoneName = attributes.getValue(i);
			}
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			// End of anchor.
			if ("a".equals(localName))
				this.zoneName = null;
			else if ("pattern".equals(localName)) {
				inPattern = false;
			} else if ("clipPath".equals(localName)) {
				clipPathDepth--;
			} else if (isDCDescription(uri, localName)) {
				recordText = false;
				description = currentText.toString().trim();
				currentText.setLength(0);

			} else if (isDCCreator(uri, localName)) {
				recordText = false;
				author = currentText.toString().trim();
				currentText.setLength(0);

			}
			styles.pop();

		}

		/**
		 * Returns a standardized representation of a path. the generalPath will
		 * be a list, which each element representing a path element : command +
		 * arguments.
		 * 
		 * <p>
		 * SVG path commands are : M x y ; L x y ; Z ; H x; V x;
		 * <ul>
		 * <li>M/m x y : move to x y
		 * <li>L/l x y : line to x y
		 * <li>H x : horizontal line to x
		 * <li>V y : vertical line to y
		 * <li>C/c x1 y1 x2 y2 x y : cubic B&eacute;zier spline
		 * <li>S/s x2 y2 x y : same, but x1 y1 is computed as the reflection of
		 * the previous x2 y2, or x y.
		 * <li>Q/q x1 y1 x y : quadratic B&eacute;zier.
		 * <li>T/t x y : is to Q/q what s/S is to C/c
		 * <li>A/a rx ry rot largearc sweep x y : arc.
		 * <li>Z/z : the end
		 * </ul>
		 * Uppercase means "absolute coordinates". Lower case means relative
		 * coordinates.
		 * <p>
		 * All command can be factorized. For instance, 'M' can be followed by
		 * any even number of coordinates. For relative commands, the "current
		 * point" is only updated <em>after</em> the last coordinate (i.e. when
		 * a new command is met).
		 * <p>
		 * Note that the arc commands won't be recognized.
		 * 
		 * @param pathData
		 *            a "d" attribute for an XML path tag.
		 */
		private GeneralPath parsePath(String pathData) {
			GeneralPath result = new GeneralPath();
			String[] l = pathToList(pathData);
			Point2D.Float currentPos = new Point2D.Float();
			// Symetric point for s and t commands
			Point2D.Float previousCubicControlPoint = null;
			Point2D.Float previousQuadricControlPoint = null;
			Point2D.Float pathStart = new Point2D.Float();
			float[] args = null;

			char command = 'M';

			for (int i = 0; i < l.length; i++) {
				// When two segments of the same kind occur successively, SVG
				// allows the
				// command not to be repeated.
				char firstChar = l[i].charAt(0);
				if (Character.isLetter(firstChar)) {
					command = firstChar;
				} else {
					// l[i] is a number. go back one item :
					i--;
				}

				boolean relative = Character.isLowerCase(command);

				// boolean nextIsCommand = false;
				// if (i == l.length - 1)
				// nextIsCommand = true;
				// else if (Character.isLetter(l[i + 1].charAt(0)))
				// nextIsCommand = true;

				// Deal with relative commands
				switch (command) {
				case 'm':
				case 'M': {
					args = new float[] { Float.parseFloat(l[++i]),
							Float.parseFloat(l[++i]) };
					fixArgsAndCurrentPos(args, currentPos, relative);
					result.moveTo(args[0], args[1]);
					pathStart.setLocation(args[0], args[1]);
					// It is said in the documentation that
					// If a m or M is followed by more than two coords,
					// those are implicit "lineto" commands. Hence :
					if (command == 'm')
						command = 'l';
					else
						command = 'L';
					// (fixes I had in JSesh 2.13.5 with W1).
				}
					break;
				case 'l':
				case 'L': {
					args = new float[] { Float.parseFloat(l[i + 1]),
							Float.parseFloat(l[i + 2]) };
					i += 2;
					fixArgsAndCurrentPos(args, currentPos, relative);
					result.lineTo(args[0], args[1]);
				}
					break;
				case 'c':
				case 'C': {
					args = new float[] { Float.parseFloat(l[i + 1]),
							Float.parseFloat(l[i + 2]),
							Float.parseFloat(l[i + 3]),
							Float.parseFloat(l[i + 4]),
							Float.parseFloat(l[i + 5]),
							Float.parseFloat(l[i + 6]) };
					i += 6;
					fixArgsAndCurrentPos(args, currentPos, relative);
					result.curveTo(args[0], args[1], args[2], args[3], args[4],
							args[5]);
					previousCubicControlPoint = new Point2D.Float(args[2],
							args[3]);
				}
					break;
				case 's':
				case 'S': // idem 'c', but the first control point is the
					// Symmetric of the previous one.
					// If the previous command was not C or S, the first
					// control point is set to currentPos.
				{
					args = new float[] { Float.parseFloat(l[i + 1]),
							Float.parseFloat(l[i + 2]),
							Float.parseFloat(l[i + 3]),
							Float.parseFloat(l[i + 4]) };
					i += 4;
					float cx, cy;
					if (previousCubicControlPoint != null) {
						cx = 2 * currentPos.x - previousCubicControlPoint.x;
						cy = 2 * currentPos.y - previousCubicControlPoint.y;
					} else {
						cx = currentPos.x;
						cy = currentPos.y;
					}
					// Called AFTER computing cx and cy.
					// See the current documentation of fixArgsAndCurrentPos
					// for a comment about the BAD idea I had there.
					fixArgsAndCurrentPos(args, currentPos, relative);
					result.curveTo(cx, cy, args[0], args[1], args[2], args[3]);
					previousCubicControlPoint = new Point2D.Float(args[0],
							args[1]);
				}
					break;
				case 'q':
				case 'Q': {
					args = new float[] { Float.parseFloat(l[i + 1]),
							Float.parseFloat(l[i + 2]),
							Float.parseFloat(l[i + 3]),
							Float.parseFloat(l[i + 4]), };
					i += 4;
					fixArgsAndCurrentPos(args, currentPos, relative);
					result.quadTo(args[0], args[1], args[2], args[3]);
					previousQuadricControlPoint = new Point2D.Float(args[0],
							args[1]);
				}
					break;
				case 't':
				case 'T': {
					args = new float[] { Float.parseFloat(l[i + 1]),
							Float.parseFloat(l[i + 2]) };
					i += 2;
					float cx, cy;
					if (previousQuadricControlPoint != null) {
						cx = 2 * currentPos.x - previousQuadricControlPoint.x;
						cy = 2 * currentPos.y - previousQuadricControlPoint.y;
					} else {
						cx = currentPos.x;
						cy = currentPos.y;
					}
					// See doc. for a comment (and 's' code too)
					fixArgsAndCurrentPos(args, currentPos, relative);
					result.quadTo(cx, cy, args[0], args[1]);
					previousQuadricControlPoint = new Point2D.Float(cx, cy);
				}
					break;
				case 'a':
				case 'A': {
					// A/a rx ry rot largearc sweep x y : arc.
					// See
					// http://www.w3.org/TR/SVG11/implnote.html#ArcImplementationNotes
					// for more data
					float startx = currentPos.x;
					float starty = currentPos.y;
					float rx = Float.parseFloat(l[i + 1]); // x-radius of the
					// ellipse
					float ry = Float.parseFloat(l[i + 2]);
					float rot = Float.parseFloat(l[i + 3]); // x-axis rotation
					int largeArc = Integer.parseInt(l[i + 4]); // boolean
					// flag :
					// should we
					// choose
					// the
					// largest
					// possible
					// arc ?
					int sweep = Integer.parseInt(l[i + 5]); // boolean flag
					// : do we turn
					// in the direct
					// orientation ?

					args = new float[] { Float.parseFloat(l[i + 6]),
							Float.parseFloat(l[i + 7]) };
					i += 7;
					fixArgsAndCurrentPos(args, currentPos, relative);
					// Now, due to rot, the usual java Arc can't be used here.
					drawArc(result, startx, starty, rx, ry, rot, largeArc != 0,
							sweep != 0, args[0], args[1]);
				}
					break;
				case 'z':
				case 'Z': {
					result.closePath();
					currentPos.setLocation(pathStart);
				}
					break;
				case 'h': {
					float dx = Float.parseFloat(l[++i]);
					result.lineTo(dx + currentPos.x, currentPos.y);
					currentPos.x += dx;
				}
					break;
				case 'H': {
					float newx = Float.parseFloat(l[++i]);
					result.lineTo(newx, currentPos.y);
					currentPos.x = newx;
				}
					break;
				case 'v': {
					float dy = Float.parseFloat(l[++i]);
					result.lineTo(currentPos.x, dy + currentPos.y);
					currentPos.y += dy;

				}
					break;
				case 'V': {
					float newy = Float.parseFloat(l[++i]);
					result.lineTo(currentPos.x, newy);
					currentPos.y = newy;
				}
					break;
				default:
					System.err.println("unknown code" + l[i]);
					return null;
				}
				if (Character.toLowerCase(command) != 'c'
						&& Character.toLowerCase(command) != 's')
					previousCubicControlPoint = null;
				if (Character.toLowerCase(command) != 'q'
						&& Character.toLowerCase(command) != 't')
					previousQuadricControlPoint = null;

			}
			return result;
		}

		/**
		 * Approximate an SVG specified Arc on a java path. see
		 * http://www.w3.org/TR/SVG11/implnote.html#ArcImplementationNotes for
		 * implementation details
		 * 
		 * <p>
		 * There are two candidate ellipse for this, and two different parts of
		 * each ellipse. Hence, four solutions.
		 * <p>
		 * See http://www.spaceroots.org/documents/ellipse/elliptical-arc.pdf
		 * for a controlled solution. Our approximation uses a simple set of
		 * four Bezier curves. (two are probably good enough in fact).
		 * 
		 * In case of problem, the arc is degraded in a line segment.
		 * @param result
		 *            the path the arc should be added to.
		 * @param x1
		 *            starting x for the arc
		 * @param y1
		 *            starting y for the arc
		 * @param rx
		 *            'major' radius
		 * @param ry
		 *            'minor' radius
		 * @param rot
		 *            angle from the current system x axis to the ellipse x
		 *            axis.
		 * @param largeArc
		 *            do we use the larger arc (>180) or the smaller one.
		 * @param sweep
		 * @param x2
		 *            ending x for the arc
		 * @param y2
		 *            ending y for the arc
		 */
		private void drawArc(GeneralPath result, double x1, double y1,
				double rx, double ry, double rot, boolean largeArc,
				boolean sweep, double x2, double y2) {
			if (rx == 0.0 || ry == 0.0 || (x1 == x2 && y1 == y2)) {
				// As per F6.6 in spec.
				result.lineTo(x2, y2);
			} else {
				rx = Math.abs(rx);
				ry = Math.abs(ry); // Apply F 6 6
				// OK. We use compute according to the specs.
				double dx = (x1 - x2) / 2.0;
				double dy = (y1 - y2) / 2.0;
				double x1p = Math.cos(rot) * dx + Math.sin(rot) * dy;
				double y1p = -Math.sin(rot) * dx + Math.cos(rot) * dy;
				double sign = largeArc == sweep ? -1 : 1;
				double rx2 = rx * rx;
				double ry2 = ry * ry;
				// Ensure radii are large enough
				double lambda = x1p * x1p / rx2 + y1p * y1p / ry2;
				if (lambda > 1) {
					rx = rx * Math.sqrt(lambda);
					ry = ry * Math.sqrt(lambda);
					rx2 = rx * rx;
					ry2 = ry * ry;
				}
				double d= (rx2 * ry2)
						/ (rx2 * y1p * y1p + ry2 * x1p * x1p) - 1.0;				
				double root =0;
				// Secure against small computation errors (we met cases where root was -1.0E-16, hence error).
				if (d >0)
					root= Math.sqrt(d);
				double cxp = sign * root * rx * y1p / ry;
				double cyp = -sign * root * ry * x1p / rx;
				double cx = Math.cos(rot) * cxp - Math.sin(rot) * cyp
						+ (x1 + x2) / 2.0;
				double cy = Math.sin(rot) * cxp + Math.cos(rot) * cyp
						+ (y1 + y2) / 2.0;
				double theta1 = angle(1, 0, (x1p - cxp) / rx, (y1p - cyp) / ry);
				double deltaTheta = angle((x1p - cxp) / rx, (y1p - cyp) / ry,
						-(x1p + cxp) / rx, -(y1p + cyp) / ry);
				if (!sweep && deltaTheta > 0)
					deltaTheta -= Math.PI * 2.0;
				if (sweep && deltaTheta < 0)
					deltaTheta += 2 * Math.PI;
				// We approximate the ellipse by four bezier splines.

				double[][] onCurve = new double[5][2];
				onCurve[0][0] = x1;
				onCurve[0][1] = y1;
				for (int i = 1; i <= 3; i++) {
					double a = theta1 + deltaTheta * i / 4.0;
					onCurve[i][0] = cx + rx * Math.cos(rot) * Math.cos(a) - ry
							* Math.sin(rot) * Math.sin(a);
					onCurve[i][1] = cy + ry * Math.sin(rot) * Math.cos(a) + ry
							* Math.cos(rot) * Math.sin(a);
					if (	onCurve[i][0] == Double.NaN || onCurve[i][1] == Double.NaN) {
						// Emergency exit !!!
						System.err.println("Problem with arc "+ x1+ "," + y1+ " "+ rx+ " "+ ry+ " rot "+ rot+ " flags "+ largeArc+ " "+ sweep+ " end "+ x2+ ", "+ y2);
						result.lineTo(x2, y2);
						return;
					}						
				}
				onCurve[4][0] = x2;
				onCurve[4][1] = y2;

				double[][] derivate = new double[5][2];
				for (int i = 0; i <= 4; i++) {
					double a = theta1 + deltaTheta * i / 4.0;
					double factor= 1/(2*Math.PI);
					factor= factor* Math.abs(deltaTheta)/4.0;
					derivate[i][0] = (-rx * Math.cos(rot) * Math.sin(a) - ry
							* Math.sin(rot) * Math.cos(a))*factor;
					derivate[i][1] = (-rx * Math.sin(rot) * Math.sin(a) + ry
							* Math.cos(rot) * Math.cos(a))*factor;
					if (deltaTheta < 0) {
						derivate[i][0] = -derivate[i][0];
						derivate[i][1] = -derivate[i][1];
					}
				}

				// Compute all curves. Note that When we point to the last
				// point, we are done (hence the -1).
				// The approximation is not very good. 
				for (int i = 0; i < onCurve.length - 1; i++) {
					double c1x, c1y, c2x, c2y;
					c1x = onCurve[i][0] + derivate[i][0];
					c1y = onCurve[i][1] + derivate[i][1];
					c2x = onCurve[i + 1][0] - derivate[i + 1][0];
					c2y = onCurve[i + 1][1] - derivate[i + 1][1];
					result.curveTo(c1x, c1y, c2x, c2y, onCurve[i + 1][0],
							onCurve[i + 1][1]);
				}
			}
		}

		/**
		 * Transform a SVG path data into a nice list. Conforms to SVG 1.0
		 * specification (02 November 2000)
		 * 
		 * <p>
		 * The parsing process is *not* simple, as the *#!&% !!! designers of
		 * svg systematically favored compacity over regularity. We can't just
		 * separate elements. We have to RECOGNISE them, because such things as
		 * 3.4.4 are possible (and mean 3.4 0.4).
		 * 
		 * <p>
		 * real numbers : ([-+]?(?:\d*\.\d+|\d+\.?)(?:[eE][-+]?\d+)?)
		 * <p>
		 * command [aAtTQqSsCcVvHhLlZzMm]
		 */

		private String[] pathToList(String pathData) {
			String real = "[-+]?(?:\\d*\\.\\d+|\\d+\\.?)(?:[eE][-+]?\\d+)?";
			String keys = "[aAtTQqSsCcVvHhLlZzMm]";
			String command = "(" + real + "|" + keys + ")";
			// According to profilers, compiling the pattern costs nothing
			// compared to using it...
			// So we don't bother to keep it static.
			Pattern p = Pattern.compile(command);
			Matcher m = p.matcher(pathData);
			ArrayList<String> result = new ArrayList<String>();
			// here is the complex part.
			while (m.find()) {
				result.add(m.group());
			}
			return result.toArray(new String[result.size()]);
		}

		public InputSource resolveEntity(String publicId, String systemId)
				throws SAXException {
			if (publicId.indexOf(" SVG ") != -1) {
				return new InputSource(new StringReader(
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
			} else {
				// Temporary solution for a problem with libraries.
				// Normally,resolveEntity is declared in jdk 1.5 as throwing
				// IOException.
				// This was not the case in 1.4 (and it was probably an error).
				// Hence, we catch the exception so that the code compiles in
				// both 1.4 and 1.5.
				try {
					return super.resolveEntity(publicId, systemId);
				} catch (Exception e) {
					return null;
				}
			}
		}

		public GeneralPath getGeneralPath() {
			GeneralPath shape = new GeneralPath();
			shape.append(area.getPathIterator(null), false);
			return shape;
		}

		/**
		 * @return Returns the zones.
		 */
		public LigatureZone[] getZones() {
			return zones;
		}

		/**
		 * Transform points into absolute coordinates.
		 * <p>
		 * side effect : update current point. This is probably NOT the best
		 * idea I have had, as it caused a nasty bug when parsing "s" tags...
		 * Yet another bug due to side effects. (the reason for the bug was that
		 * I called fixargs and then did computation which intended to involve
		 * the "old" currentPoint.)
		 * 
		 * @param args
		 * @param current
		 * @param relative
		 */
		private void fixArgsAndCurrentPos(float[] args, Point2D.Float current,
				boolean relative) {
			int i = 0;
			for (i = 0; i < args.length; i += 2) {
				if (relative) {
					args[i] = args[i] + current.x;
					args[i + 1] = args[i + 1] + current.y;
				}
			}
			// if (nextIsCommand)
			current.setLocation(args[i - 2], args[i - 1]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.graphics.glyphs.model.SimpleSignSourceModel#getCurrentCode()
	 */
	public String getCurrentCode() {
		return code;
	}

	private String getCodeForURL(URL url) {
		String path = url.getPath();
		int id = path.lastIndexOf('/');
		String code = GardinerCode.getCodeForFileName(path.substring(id + 1));
		if (code == null) {
			code = path.substring(id + 1);
			code = code.substring(0, code.indexOf('.'));
		}
		return code;
	}

	/**
	 * Compute the angle between two vectors.
	 * <p>
	 * precondition : none of the vectors shall be null.
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	private static double angle(double x1, double y1, double x2, double y2) {
		double n1 = Math.sqrt(x1 * x1 + y1 * y1);
		double n2 = Math.sqrt(x2 * x2 + y2 * y2);
		double cos = (x1 * x2 + y1 * y2) / (n1 * n2);
		// make for rounding errors :
		if (cos < -1.0)
			cos = -1.0;
		else if (cos > 1.0)
			cos = 1.0;
		double angle = Math.acos(cos);
		if (x1 * y2 - y1 * x2 < 0)
			angle = -angle;
		return angle;
	}
}
