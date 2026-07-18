package jsesh.glyphs.shape;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.jupiter.api.Test;
import org.xml.sax.helpers.DefaultHandler;

import jsesh.glyphs.signsource.SVGSignSource;

/**
 * Tests that a ShapeChar exported to SVG can be re-read without errors.
 *
 * Regression test for the bug where importing quail.svg and inserting it
 * produced a malformed SVG (missing space between XML attributes, duplicate
 * version attribute), causing JSesh to display a black square instead of the
 * sign when re-loading, and Inkscape to show an empty canvas.
 */
public class ShapeCharSVGRoundTripTest {

    private static final String QUAIL_SVG = "/quail.svg";

    /** Load quail.svg from test resources and return a ShapeChar. */
    private ShapeChar loadQuail() throws Exception {
        URL url = getClass().getResource(QUAIL_SVG);
        assertNotNull(url, "quail.svg not found in test resources");
        SVGSignSource src = new SVGSignSource(url);
        src.next();
        ShapeChar shape = src.getCurrentShape();
        assertNotNull(shape, "SVGSignSource failed to parse quail.svg");
        return shape;
    }

    /**
     * The exported SVG must be well-formed XML.
     * Before the fix this test fails because exportToSVG writes
     * {@code version='1.0'xmlns='...'} — two attributes without a separating
     * space, which is a fatal XML error.
     */
    @Test
    public void exportedSVGIsWellFormedXML() throws Exception {
        ShapeChar shape = loadQuail();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        shape.exportToSVG(out, "UTF-8");
        byte[] svgBytes = out.toByteArray();

        // Parse the output as XML — must not throw.
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();
        try {
            parser.parse(new ByteArrayInputStream(svgBytes), new DefaultHandler());
        } catch (Exception e) {
            fail("exportToSVG produced malformed XML: " + e.getMessage()
                    + "\n\nGenerated SVG:\n" + new String(svgBytes, "UTF-8"));
        }
    }

    /**
     * After export the sign can be re-read and produces a non-trivial ShapeChar.
     * Before the fix, re-reading the exported file returns an error placeholder
     * (14×14 black square) because the SAX parser rejects the malformed XML.
     */
    @Test
    public void exportedSVGCanBeReReadByJSesh() throws Exception {
        ShapeChar original = loadQuail();

        // Export to bytes
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        original.exportToSVG(out, "UTF-8");
        byte[] svgBytes = out.toByteArray();

        // Re-read via the same parser that JSesh uses when loading saved signs
        SVGSignSource reRead = new SVGSignSource("A999", new ByteArrayInputStream(svgBytes));
        reRead.next();
        ShapeChar roundTripped = reRead.getCurrentShape();

        assertNotNull(roundTripped, "Re-read ShapeChar is null");
        assertNotNull(roundTripped.getBbox(), "Re-read ShapeChar has no bounding box");

        // The error placeholder is exactly 14×14; the real sign should be larger.
        double h = roundTripped.getBbox().getHeight();
        assertTrue(
                h > 14.01,
                "Re-read sign looks like the error placeholder (height=" + h
                        + "). exportToSVG likely produced malformed XML.");

        // Shape must have some content
        assertNotNull(roundTripped.getShape(), "Re-read ShapeChar has no shape");
        assertFalse(
                roundTripped.getShape().getBounds2D().isEmpty(),
                "Re-read shape is empty");
    }
}
