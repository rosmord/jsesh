/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.mdcDisplayer.draw;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import javax.imageio.ImageIO;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdcDisplayer.preferences.DrawingSpecificationsImplementation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author rosmord
 */
@RunWith(Parameterized.class)
/**
 * Check an bug found by O. Goelet : when a partial cartouche is used in a
 * column, there is a NullPointerException.
 */
public class TestPartialCartoucheDrawingsWithOrientation {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Object[][] t = {
            {"lineL2R", TextOrientation.HORIZONTAL, TextDirection.LEFT_TO_RIGHT},
            {"lineR2l", TextOrientation.HORIZONTAL, TextDirection.RIGHT_TO_LEFT},
            {"columnL2R", TextOrientation.VERTICAL, TextDirection.LEFT_TO_RIGHT},
            {"columnR2l", TextOrientation.VERTICAL, TextDirection.RIGHT_TO_LEFT}
        };
        return Arrays.asList(t);
    }

    private final String testName;
    private final TextOrientation orientation;
    private final TextDirection direction;

    /**
     * Initialize the test.
     *
     * @param testName the test name (used as file name for the output).
     * @param orientation
     * @param direction
     */
    public TestPartialCartoucheDrawingsWithOrientation(String testName, TextOrientation orientation, TextDirection direction) {
        this.testName = testName;
        this.orientation = orientation;
        this.direction = direction;
        getPictureFolder().mkdirs();
    }

    @Test
    public void testPartialCartouchesInLines() throws MDCSyntaxError, IOException {
        String partial = "<1--0>-ra-mn:n-xpr-<0--2>";
        MDCDrawingFacade facade = new MDCDrawingFacade();
        DrawingSpecificationsImplementation drawingSpecifications = new DrawingSpecificationsImplementation();
        drawingSpecifications.setTextDirection(direction);
        drawingSpecifications.setTextOrientation(orientation);
        facade.setDrawingSpecifications(drawingSpecifications);
        BufferedImage image = facade.createImage(partial);
        ImageIO.write(image, "png", getFile(testName));
    }

    private static File getPictureFolder() {
        return new File(new File("target"), "testPictures");
    }

    private static File getFile(String endOfFileName) {
        String fileName = "jsesh.mdcDisplayer.draw." + "partialCartouche" + endOfFileName + ".png";
        return new File(getPictureFolder(), fileName);
    }
}
