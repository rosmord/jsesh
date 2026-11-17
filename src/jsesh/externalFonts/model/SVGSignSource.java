package jsesh.externalFonts.model;

import java.awt.BasicStroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import jsesh.bzr.simple.ShapeChar;
import jsesh.hieroglyphs.HieroglyphsManager;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Simple SVG importer for characters in JSesh.
 * <p>
 * The SVG parsing system used is rather basic and certainly not fullproof.
 * Currently, I would rather not include the 4megs of Batik into my software,
 * but frankly, SVG is quite complex - and chosed the way of "there's more than
 * one way to write it".
 * 
 * <p>
 * Currently, we can read the output of : potrace, autotrace, files from
 * inkscape, sketch and open office.
 * <p>
 * BTW, I'm really glad the JDK team created the Area class. It's so powerful
 * that I feel I'm cheating when I'm using it :-)
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
			// look if file names might correspond to a code.
			code = getCodeForFile(file);
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
			parser.parse(file, handler);
			shape = new ShapeChar();
			shape.setShape(handler.getGeneralPath());
			shape.fixShape();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		beforeFirst();
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
		pos= -1;
	}

	public void afterLast() {
		pos= 1;
	}

	static class Style {
		boolean black = false;

		boolean line = false;

		float strokeWidth = 0f;

		public Style() {
		}

		public Style(Style parent) {
			if (parent != null) {
				black = parent.black;
				line = parent.line;
				strokeWidth = parent.strokeWidth;
			}
		}

		public Style(Style parent, Attributes attributes) {
			this(parent);
			String styleDef = attributes.getValue("", "style");
			if (styleDef != null) {
				parseStyleDef(styleDef);
			}
			if ("#000000".equals(attributes.getValue("", "fill")))
				black = true;
			if ("black".equals(attributes.getValue("", "fill")))
				black = true;
			if ("rgb(0,0,0)".equals(attributes.getValue("", "fill")))
				black = true;
			if (!black) {
				if ("none".equals(attributes.getValue("", "stroke")))
					line = false;
				else if (attributes.getValue("", "stroke") != null) {
					line = true;
				}
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

			line = (styleDef.indexOf("fill:none") != -1);

			if (line) {
				if (styleDef.indexOf("stroke:#000000") != -1
						|| styleDef.indexOf("stroke:black") != -1
						|| styleDef.indexOf("stroke:rgb(0,0,0)") != -1)
					black = true;
			} else {
				if (styleDef.indexOf("fill:#000000") != -1
						|| styleDef.indexOf("fill:rgb(0,0,0)") != -1
						|| styleDef.indexOf("fill:black") != -1)
					black = true;
			}

			int i = styleDef.indexOf("stroke-width:");
			if (i != -1) {
				String widthDef;
				// go to the ':' after stroke-width
				i = styleDef.indexOf(':', i);
				i++;
				// find the end of this attribute.
				int end = styleDef.indexOf(';', i);
				if (end == -1)
					end = styleDef.length();
				while (!Character.isDigit(styleDef.charAt(end)))
					end--;
				widthDef = styleDef.substring(i, end + 1);
				strokeWidth = Float.parseFloat(widthDef);
			}

		}
	}

	class SVGReader extends DefaultHandler {

		private Area area;

		private Stack styles;

		public SVGReader() {
			area = new Area();
			styles = new Stack();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
		 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			Style parentStyle = null;

			if (!styles.isEmpty())
				parentStyle = (Style) styles.peek();

			Style style = new Style(parentStyle, attributes);
			styles.push(style);

			if ("path".equals(localName)) {
				String d = attributes.getValue("", "d");

				GeneralPath subPath = parsePath(d, style.black);
				if (subPath != null) {
					if (style.line) {
						area.add(new Area(new BasicStroke(style.strokeWidth)
								.createStrokedShape(subPath)));
					} else if (style.black) {
						area.add(new Area(subPath));
					} else {
						area.subtract(new Area(subPath));
					}
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
					if (!style.line) {
						subPath.closePath();
						if (style.black) {
							area.add(new Area(subPath));
						} else {
							area.subtract(new Area(subPath));
						}
					}
				}
			}
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {

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
		 * <li> M/m x y : move to x y
		 * <li> L/l x y : line to x y
		 * <li> H x : horizontal line to x
		 * <li> V y : vertical line to y
		 * <li> C/c x1 y1 x2 y2 x y : cubic Bzier spline
		 * <li> S/s x2 y2 x y : same, but x1 y1 is computed as the reflection of
		 * the previous x2 y2, or x y.
		 * <li> Q/q x1 y1 x y : quadratic Bzier.
		 * <li> T/t x y : is to Q/q what s/S is to C/c
		 * <li> A/a rx ry rot largearc sweep x y : arc.
		 * <li> Z/z : the end
		 * </ul>
		 * Uppercase means "absolute coordinates". Lower case means relative
		 * coordinates.
		 * <p>
		 * All command can be factorized. For instance, 'M' can be followed by
		 * any even number of coordinates. For relative commands, the "current
		 * point" is only updated <em>after</em> the last coordinate (i.e.
		 * when a new command is met).
		 * <p>
		 * Note that the arc commands won't be recognized.
		 * 
		 * @param pathData
		 *            a "d" attribute for an XML path tag.
		 * @param black
		 *            true if this subpath should be filled in black (direct
		 *            orientation).
		 */
		private GeneralPath parsePath(String pathData, boolean black) {
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
					fixArgs(args, currentPos, relative);
					result.moveTo(args[0], args[1]);
					pathStart.setLocation(args[0], args[1]);
				}
					break;
				case 'l':
				case 'L': {
					args = new float[] { Float.parseFloat(l[i + 1]),
							Float.parseFloat(l[i + 2]) };
					i += 2;
					fixArgs(args, currentPos, relative);
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
					fixArgs(args, currentPos, relative);
					result.curveTo(args[0], args[1], args[2], args[3], args[4],
							args[5]);
					previousCubicControlPoint = new Point2D.Float(args[2],
							args[3]);
				}
					break;
				case 's':
				case 'S': // idem 'c', but the first control point is the
				// symetric of the previous one.
				{
					args = new float[] { Float.parseFloat(l[i + 1]),
							Float.parseFloat(l[i + 2]),
							Float.parseFloat(l[i + 3]),
							Float.parseFloat(l[i + 4]) };
					i += 4;
					fixArgs(args, currentPos, relative);
					float cx, cy;
					if (previousCubicControlPoint != null) {
						cx = 2 * currentPos.x - previousCubicControlPoint.x;
						cy = 2 * currentPos.y - previousCubicControlPoint.y;
					} else {
						cx = currentPos.x;
						cy = currentPos.y;
					}
					result.curveTo(cx, cy, args[0], args[1], args[2], args[3]);
					previousCubicControlPoint = new Point2D.Float(args[1],
							args[2]);
				}
					break;
				case 'q':
				case 'Q': {
					args = new float[] { Float.parseFloat(l[i + 1]),
							Float.parseFloat(l[i + 2]),
							Float.parseFloat(l[i + 3]),
							Float.parseFloat(l[i + 4]), };
					i += 4;
					fixArgs(args, currentPos, relative);
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
					fixArgs(args, currentPos, relative);
					float cx, cy;
					if (previousCubicControlPoint != null) {
						cx = 2 * currentPos.x - previousQuadricControlPoint.x;
						cy = 2 * currentPos.y - previousQuadricControlPoint.y;
					} else {
						cx = currentPos.x;
						cy = currentPos.y;
					}
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
					float largeArc = Float.parseFloat(l[i + 4]); // boolean
																	// flag :
																	// should we
																	// choose
																	// the
																	// largest
																	// possible
																	// arc ?
					float sweep = Float.parseFloat(l[i + 5]); // boolean flag
																// : do we turn
																// in the direct
																// orientation ?

					args = new float[] { Float.parseFloat(l[i + 6]),
							Float.parseFloat(l[i + 7]) };
					i += 7;
					fixArgs(args, currentPos, relative);
					// Now, due to rot, the usual java Arc can't be used here.
					drawArc(result, startx, starty, rx, ry, rot, largeArc,
							sweep, args[0], args[1]);
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
		 * Draws an SVG specified Arc on a java path. see
		 * http://www.w3.org/TR/SVG11/implnote.html#ArcImplementationNotes for
		 * implementation details
		 * 
		 * @param result
		 * @param startx
		 * @param starty
		 * @param rx
		 * @param ry
		 * @param rot
		 * @param largeArc
		 * @param sweep
		 * @param endx
		 * @param endy
		 */
		private void drawArc(GeneralPath result, float startx, float starty,
				float rx, float ry, float rot, float largeArc, float sweep,
				float endx, float endy) {
			// TODO WRITE ME !!!!
			result.lineTo(endx, endy);

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
			Pattern p = Pattern.compile(command);
			Matcher m = p.matcher(pathData);
			ArrayList result = new ArrayList();
			while (m.find()) {
				result.add(m.group());
			}
			return (String[]) result.toArray(new String[result.size()]);
		}

		public InputSource resolveEntity(String publicId, String systemId)
				throws SAXException {
			if (publicId.indexOf(" SVG ") != -1) {
				return new InputSource(new StringReader(
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
			} else
				return super.resolveEntity(publicId, systemId);
		}

		public GeneralPath getGeneralPath() {
			GeneralPath shape = new GeneralPath();
			shape.append(area.getPathIterator(null), false);
			return shape;
		}

		/**
		 * Transform points into absolute coordinates.
		 * <p>
		 * side effect : update current point.
		 * 
		 * @param args
		 * @param current
		 * @param relative
		 */
		private void fixArgs(float[] args, Point2D.Float current,
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
	 * @see jsesh.externalFonts.model.SimpleSignSourceModel#getCurrentCode()
	 */
	public String getCurrentCode() {
		return code;
	}

	/**
	 * Returns the Manuel de codage code corresponding to a given MDC file, or
	 * null if the file name does not fit.
	 * 
	 * @param file
	 * @return a code or null.
	 */
	public static String getCodeForFile(File file) {
		String fname = file.getName().toUpperCase();
		int stopIndex = fname.indexOf('.');
		String code = fname.substring(0, stopIndex);
		
		 if (code.startsWith("US")) {
				StringBuffer buff= new StringBuffer();
				buff.append("US");
				int i=2;
				while (i < code.length() && Character.isDigit(code.charAt(i)))
					i++;
				buff.append(code.substring(2,i));
				buff.append(canonizeCode(code.substring(i)));
				code= buff.toString();
		 }
		 else
			 code= canonizeCode(code);
		
		if (!HieroglyphsManager.isCorrectGardinerCode(code))
			code = null;
		return code;
	}

	/**
	 * Given a Gardiner code in uppercase, return the correct code.
	 */
	private static String canonizeCode(String code) {
		//		 	Now, the only problem is with Aa and Ff
		if (code.length() >= 2) {
			if (code.charAt(1) == 'A' || code.charAt(1) == 'F') {
				// StringBuffer would probably be an overkill for this line.
				code = "" + code.charAt(0)
						+ Character.toLowerCase(code.charAt(1))
						+ code.substring(2);
			}
		}
		return code;
	}
}
