package org.qenherkhopeshef.graphics.eps;

import java.awt.geom.Dimension2D;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;

/**
 * A simple EPS output system, quite tailored to our limited requirements.
 * 
 * Based on our SVG output library, as SVG and EPS share very similar logics and
 * capabilities.
 * 
 * <p>
 * As always, this class is a very thin veneer on the EPS format itself. That
 * is, it doesn't play fancy tricks. We did think about avoiding path
 * duplications in some cases (for instance a fill and then a stroke are made),
 * but
 * <ol>
 * <li>this belongs to an upper level (in fact, the Graphics2D related class is
 * concerned too)</li>
 * <li>as we don't really use this feature, it would be of dubious utility.
 * </ol>
 * 
 * <p>
 * Note for future programming:
 * 
 * Including Truetype fonts in EPSF documents works fairly well. The font should
 * be defined in the header as:
 * 
 * <pre>
 * %%DocumentNeededResources: font EgyptoSerif
 * %%IncludeResource: font EgyptoSerif
 * </pre>
 * 
 * Then, access to the glyphs is possible, either with the show command, or, for
 * those glyphs which have no encoding, with the glyphshow command. Examples:
 * 
 * <pre>
 * 
 * /EgyptoSerif findfont
 * 40 scalefont
 * setfont
 * /infinity glyphshow : displays ∞
 * /uniA722 glyphshow : displays the egyptological aleph
 * /uniEC40 glyphshow : in EgyptoSerif, used for H + macron.
 * </pre>
 * 
 * It would probably be safer to restrict this to glyphs in private areas, as
 * uniA722 is likely to be dropped as name for the Aleph glyph !
 * 
 * @author Serge Rosmorduc
 */
public class EPSLowLevel {
	private Writer writer;

	private double currentX, currentY;

	/**
	 * Ugly hidden static map for color conversions.
	 */
	private static HashMap colorMap = new HashMap();

	private static EPSColor RED= new EPSColor(255, 0, 0);
	
	static {
		// Black
		colorMap.put(new EPSColor(0, 0, 0), new EPSCMYKColor(0, 0, 0, 100));
		// White
		colorMap.put(new EPSColor(255, 255, 255), new EPSCMYKColor(0, 0, 0, 0));
		// // Red
		// colorMap.put(new EPSColor(255, 0, 0), new EPSCMYKColor(0, 52, 62,
		// 7));
		// Shades of gray
		for (int i = 1; i < 255; i++) {
			int grayLevel = (int) (Math.round((255.0 - i) / 256.0) * 100);
			colorMap.put(new EPSColor(i, i, i), new EPSCMYKColor(0, 0, 0,
					grayLevel));
		}

	}

	public EPSLowLevel(Writer writer, Dimension2D dimensions)
			throws EPSOutException {
		this.writer = writer;
		writeHeader(dimensions, "");
	}

	public EPSLowLevel(Writer writer, Dimension2D dimensions, String comment)
			throws EPSOutException {
		this.writer = writer;
		writeHeader(dimensions, comment);
	}

	private void writeHeader(Dimension2D dimensions, String comment)
			throws EPSOutException {
		try {
			int height = (int) Math.ceil(dimensions.getHeight());
			int width = (int) Math.ceil(dimensions.getWidth());
			// Generate mandatory EPSF information.
			writer.write("%!PS-Adobe-3.0 EPSF-3.0\n");
			// %%BoundingBox: llx lly urx ury
			writer.write("%%BoundingBox: ");
			writer.write("0 0 " + width + " " + height + "\n");

			writer.write("%%Pages: 1\n");
			// Red for IFAO publications
			writer.write("%%DocumentCustomColors: (ifao 187)\n");
			writer.write("%%CMYKCustomColor: 0 1 0.8 0.2 (ifao 187))");
			writer.write("%%EndComments\n");

			if (comment.length() > 0) {
				String commentList[] = comment.split("\\n|\\r\\n?");
				for (int i = 0; i < commentList.length; i++) {
					writer.write("%JSESH-source: " + commentList[i]);
					writer.write("\n");
				}
			}
			// Now red color definition
			writer.write("%%BeginSetup\n");
			writer.write("/ifao_187 { [ /Separation (ifao 187)\n");
			writer
					.write("  /DeviceCMYK {0 exch dup exch dup 0.788235 mul exch 0.2 mul}]\n");
			writer.write("  setcolorspace setcolor} bind def\n");
			writer.write("%%EndSetup\n");
			writer.write("%%Page: 1 1\n");

			sendCommand("gsave");
			sendCommand("1 -1 scale");
			sendCommand("" + 0 + " -" + (int) (dimensions.getHeight())
					+ " translate");
		} catch (IOException e) {
			throw new EPSOutException(e);
		}
	}

	public void drawLine(double x, double y, double x2, double y2)
			throws EPSOutException {
		startPath();
		moveTo(x, y);
		lineTo(x2, y2);
		strokePath();
	}

	public void startPath() throws EPSOutException {
		sendCommand("newpath");
	}

	public void cubicTo(double x1, double y1, double x2, double y2, double x,
			double y) throws EPSOutException {
		addPathElement(x1, y1);
		addPathElement(x2, y2);
		addPathElement(x, y);
		sendCommand("curveto");
	}

	private void addPathElement(double x, double y) throws EPSOutException {
		try {
			writeDouble(x);
			writer.write(' ');
			writeDouble(y);
			writer.write(' ');
			setCurrentPosition(x, y);
		} catch (IOException e) {
			throw new EPSOutException(e);
		}
	}

	private void writeDouble(double x) throws EPSOutException {
		try {
			DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
			DecimalFormat format = new DecimalFormat("#.####", symbols);

			writer.write(format.format(x));
		} catch (IOException e) {
			throw new EPSOutException(e);
		}
	}

	public void moveTo(double x, double y) throws EPSOutException {
		addPathElement(x, y);
		sendCommand("moveto");
	}

	private void setCurrentPosition(double x, double y) {
		currentX = x;
		currentY = y;
	}

	public void lineTo(double x, double y) throws EPSOutException {
		addPathElement(x, y);
		sendCommand("lineto");
	}

	public void quadTo(double x1, double y1, double x, double y)
			throws EPSOutException {
		double x0 = currentX;
		double y0 = currentY;
		// Control points for quadric splines:
		// CP1 = QP0 + 2/3 *(QP1-QP0)
		// CP2 = QP1 + 1/3 (QP2 - QP1)

		double cp1x, cp1y, cp2x, cp2y;
		cp1x = x0 + 2.0 / 3 * (x1 - x0);
		cp1y = y0 + 2.0 / 3 * (y1 - y0);
		cp2x = x1 + 1.0 / 3 * (x - x1);
		cp2y = y1 + 1.0 / 3 * (y - y1);
		addPathElement(cp1x, cp1y);
		addPathElement(cp2x, cp2y);
		addPathElement(x, y);
		sendCommand("curveto");
	}

	public void strokePath() throws EPSOutException {
		sendCommand("stroke");

	}

	public void fillPath() throws EPSOutException {
		sendCommand("fill");
	}

	public void close() throws EPSOutException {
		try {
			writer.write("");
			writer.close();
		} catch (IOException e) {
			throw new EPSOutException(e);
		}
	}

	public void closePath() throws EPSOutException {
		sendCommand("closepath");
	}

	/**
	 * Sets the color to CMYK, using very basic conversion. Really tailor-made
	 * for JSesh. Now, I could probably think about using CMYK as a reference on
	 * another level.
	 * 
	 * @param red
	 * @param green
	 * @param blue
	 * @throws EPSOutException
	 */
	public void setColor(int red, int green, int blue) throws EPSOutException {
		try {
			// Transform *some* colors into CMYK colors. Others will be kept
			// RGB.
			EPSColor col = new EPSColor(red, green, blue);
			if (col.equals(RED)) {
				writer.write("1 ifao_187\n");
			} else if (colorMap.containsKey(col)) {
				EPSCMYKColor simpleCol = (EPSCMYKColor) colorMap.get(col);
				writeDouble(scaleCMYKColor(simpleCol.getCyan()));
				writer.write(' ');
				writeDouble(scaleCMYKColor(simpleCol.getMagenta()));
				writer.write(' ');
				writeDouble(scaleCMYKColor(simpleCol.getYellow()));
				writer.write(' ');
				writeDouble(scaleCMYKColor(simpleCol.getBlack()));
				writer.write(' ');
				sendCommand("setcmykcolor");
			} else {
				writeDouble(scaleColor(red));
				writer.write(' ');
				writeDouble(scaleColor(green));
				writer.write(' ');
				writeDouble(scaleColor(blue));
				writer.write(' ');
				sendCommand("setrgbcolor");
			}
		} catch (IOException e) {
			throw new EPSOutException(e);
		}
	}

	public void setColor(EPSColor color) throws EPSOutException {
		setColor(color.getR(), color.getG(), color.getB());
	}

	private void sendCommand(String command) throws EPSOutException {
		try {
			writer.write(" " + command + "\n");
		} catch (IOException e) {
			throw new EPSOutException(e);
		}
	}

	private static double scaleColor(int component) {
		return component / 255.0;
	}

	private static double scaleCMYKColor(int component) {
		return component / 100.0;
	}

	public void gsave() {
		sendCommand("gsave");

	}

	public void grestore() {
		sendCommand("grestore");
	}

}
