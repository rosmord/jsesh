package jsesh.mdcDisplayer.draw;

import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

import jsesh.drawingspecifications.JSeshStyle;
import jsesh.mdc.MDCSyntaxError;

/**
 * A integration test using MDCDrawingFacade.
 * 
 * This class is not really unit testing MDCDrawingFacade, but rather all the
 * drawing capabilities of JSesh.
 * 
 * There will be two outcomes for the tests:
 * 
 * <ul>
 * <li>they will run without errors</li>
 * <li>the generated images will be visually checked.</li>
 * </ul>
 * 
 * @author Serge Rosmorduc
 */
public class MDCDrawingFacadeIntegrationTest {

    private static final String outputDir = "target/mdcDrawingFacadeIntegrationTest/";

    private static final String RA_M_PT = "i-w-r:a-C1-m-p*t:pt";

    private void buildAndSaveImage(String mdc, String fileName) throws Exception {
        buildAndSaveImage(mdc, fileName, null, null);
    }

    private void buildAndSaveImageWithHeight(String mdc, String fileName, int cadratHeight) throws Exception {
        buildAndSaveImage(mdc, fileName, null, cadratHeight);
    }

    private void buildAndSaveImage(String mdc, String fileName, JSeshStyle jSeshStyle, Integer cadratHeight)
            throws Exception {
        new File(outputDir).mkdirs();
        MDCDrawingFacade facade = new MDCDrawingFacade();
        if (jSeshStyle != null) {
            facade.setJseshStyle(jSeshStyle);
        }
        if (cadratHeight != null) {
            facade.setCadratHeight(cadratHeight);
        }
        try {
            File outputFile = new File(outputDir, fileName + ".png");
            BufferedImage img = facade.createImage(mdc);
            ImageIO.write(img, "png", outputFile);
            assertTrue(outputFile.exists());
            System.out.println("Image created : " + img.getWidth() + " x " + img.getHeight());
        } catch (MDCSyntaxError | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testA1() throws Exception {
        buildAndSaveImage("A1", "testA1");
    }

    @Test
    public void testXpr() throws Exception {
        buildAndSaveImage("xpr", "testXrp");
    }

    @Test
    public void testRaMPt() throws Exception {
        buildAndSaveImage(RA_M_PT, "testRaMPt");
    }

    @Test
    public void testRaMPtLarge() throws Exception {
        buildAndSaveImageWithHeight(RA_M_PT, "testRaMPtLarge", 64);
    }

    @Test
    public void testAbsolutePlacement() throws Exception {
        String mdc = "+lCheck advance geometry when sign dims are modified+s!"
                + "G5&&&ra-G5\\200&&&ra-!"
                + "stp&&&ra:n!"
                + "t&m-m&t-t&m&t-t^^^m-m&&&t!"
                + "t&A-A&t-t&A&t-t^^^A-A&&&t!"
                + "n**x{{967,79,100}}-+idirect placement with non-standard size+s!"
                + "+i(we expect the +sx+i sign to be just after the n+s!"
                + "t^^^A-t^^^A\\R180+i (unexpectedly --- seems to work)+s-!"
                + "F20\\R180&&&x-+i (doesn't work; it's more or less expected)+s!"
                + "t^^^A\\200\\R180+i (this one is not expected to work now)+s-!";
        JSeshStyle jSeshStyle = JSeshStyle.DEFAULT.copy()
                .geometry(g -> g.maxCadratHeight(30)
                        .standardSignHeight(30))
                .build();
        buildAndSaveImage(mdc, "testAbsolutePlacement", jSeshStyle, 14);
    }


    /**
     * This test checks if ligatures inherited from tksesh are correctly rendered by JSesh.
     * @throws Exception
     */
    @Test
    public void testTkseshLigatures() throws Exception {        
        String mdc = """
                +bLigatures defined in Tksesh+s-!
                M17&M17__-!
                Z1&Z1__-!
                F12&D28__-!
                U36&D28__-!
                E10&Z1__-!
                F20&O34__-!
                F39&Z1__-!
                V28&D36__-!
                N29&U1__-!
                N37&U1__-!
                R8&Z1__-!
                G36&X1&D21__-!
                V28&D58&T28__-!
                Xr&H&b-!
                V28&D36&D36__-!
                M27&X1&Z1__-!
                R8&M17&X1__-!
                D21&Q3&D36__-!
                I10&S43&S3__-!
                I10&S43&Z1__-!
                I10&S43&S43&S43_-!
                U21&N35&N5__-!
                S29&D52__-!
                G26&X1&Z4__
                """;

                buildAndSaveImageWithHeight(mdc, "tksesh_ligatures", 30);
    }
}
