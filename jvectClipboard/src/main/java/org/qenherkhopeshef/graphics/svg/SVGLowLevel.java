package org.qenherkhopeshef.graphics.svg;

import java.io.IOException;
import java.io.Writer;

/**
 * A simple SVG output system, quite tailored to our limited requirements.
 *
 * @author rosmord
 *
 */
public class SVGLowLevel {

    private Writer writer;
    private StringBuffer currentPath = new StringBuffer();
    private String fillOpacity = null;
    private String foreground = "#000";
    // USE THIS VALUE SOMEWHERE ...
    private String background = "#FFF";
    private int strokeWidth = 2;
    /**
     * Ask for the SVG 1.2 CMYK colour profiles.
     */
    private boolean useCmyk= false;
    
    public SVGLowLevel(Writer writer, java.awt.geom.Dimension2D dimensions)
            throws IOException {
        this.writer = writer;
        writer.write("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>");

		// SVG opening tag MUST contain the following namespace declarations:
        // <svg xmlns="http://www.w3.org/2000/svg"
        // xmlns:xlink="http://www.w3.org/1999/xlink">
        double width = dimensions.getWidth();
        double height = dimensions.getHeight();
        String[][] attributes = new String[][]{
            {"xmlns", "http://www.w3.org/2000/svg"},
            {"xmlns:xlink", "http://www.w3.org/1999/xlink"},
            {"width", "" + width},
            {"height", "" + height}
        };
        generateStartTag("svg", attributes);
    }


    public void drawLine(double x, double y, double x2, double y2)
            throws IOException {
        startPath();
        moveTo(x, y);
        lineTo(x2, y2);
        drawPath();
    }

    public void startPath() {
        currentPath.setLength(0);
    }

    public void cubicTo(double x1, double y1, double x2, double y2, double x,
            double y) {
        currentPath.append("C ");
        addPathElement(x1, y1);
        addPathElement(x2, y2);
        addPathElement(x, y);
    }

    private void addPathElement(double x1, double y1) {
        currentPath.append(x1);
        currentPath.append(" ");
        currentPath.append(y1);
        currentPath.append(" ");
    }

    public void moveTo(double x, double y) {
        currentPath.append("M ");
        addPathElement(x, y);
    }

    public void lineTo(double x, double y) {
        currentPath.append("L ");
        addPathElement(x, y);
    }

    public void quadTo(double x1, double y1, double x, double y) {
        currentPath.append("Q ");
        addPathElement(x1, y1);
        addPathElement(x, y);
    }

    public void drawPath() throws IOException {
        String[][] attributes = new String[][]{
            {"d", currentPath.toString()},
            {"style", "fill:none;stroke:" + foreground + ";stroke-width:" + getStrokeWidth()
            },};
        generateClosedTag("path", attributes);
        currentPath.setLength(0);
    }

    public void fillPath() throws IOException {
        if (!currentPath.toString().endsWith("Z")) {
            currentPath.append("Z");
        }
        String[][] attributes = new String[][]{
            {"d", currentPath.toString()}, {"style", buildFillStyle()},};
        generateClosedTag("path", attributes);
        currentPath.setLength(0);
    }

    private String buildFillStyle() {
        String style = "fill:" + foreground + ";stroke:none";
        if (fillOpacity != null) {
            style += ";fill-opacity:" + fillOpacity;
        }
        return style;
    }

    public void close() throws IOException {
        generateEndTag("svg");
        writer.close();
    }

    private void generateStartTag(String name, String[][] attributes)
            throws IOException {
        generateStartTag(name, attributes, false);
    }

    private void generateClosedTag(String name, String[][] attributes)
            throws IOException {
        generateStartTag(name, attributes, true);
    }

    private void generateStartTag(String name, String[][] attributes,
            boolean closedTag) throws IOException {
        writer.write("<" + name);
        if (attributes != null && attributes.length != 0) {
            writer.write(" ");
            for (int i = 0; i < attributes.length; i++) {
                writer.write(attributes[i][0]);
                writer.write("='");
                writer.write(attributes[i][1]);
                writer.write("' ");
            }
        }
        if (closedTag) {
            writer.write("/");
        }
        writer.write(">");
    }

    private void generateEndTag(String name) throws IOException {
        writer.write("</" + name + ">");
    }

    public void closePath() {
        currentPath.append("Z");

    }

    public void setForeground(int red, int green, int blue, int alpha) {
        foreground = toColor(red, green, blue);
        if (alpha == 255) {
            fillOpacity = null;
        } else {
            fillOpacity = "" + alpha / 255.0;
        }
    }

    /**
     * Return a correct SVG String representation of a colour.
     *
     * @param red
     * @param green
     * @param blue
     * @return
     */
    private String toColor(int red, int green, int blue) {
        String color;
        if (useCmyk && red == 0 && green == 0 && blue == 0) {
            //color = "black icc-color(#CMYK, 0,0,0,100)";
            color = "black device-cmyk(0, 0, 0, 100)";
        } else if (useCmyk && red == 255 && green == 0 && blue == 0) {
            //color= "red icc-color(#CMYK, 0,100,100,0)";
            color = "red device-cmyk(0,100,100,0)";
        } else {
            color = "#" + buildColorComponent(red) + buildColorComponent(green)
                    + buildColorComponent(blue);
        }
        return color;
    }

    /**
     * Build an hexadecimal string for a SVG colour component.
     *
     * @param red
     * @return
     */
    private String buildColorComponent(int component) {
        String s = Integer.toHexString(component);
        if (s.length() < 2) {
            s = "0" + s;
        }
        return s;
    }

    public void setBackground(int red, int green, int blue, int alpha) {
        background = toColor(red, green, blue);
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public void setUseCmyk(boolean useCmyk) {
        this.useCmyk = useCmyk;
    }

    
}
