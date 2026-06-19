package jsesh.mdcDisplayer.draw;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.ValueSource;

import jsesh.drawingspecifications.JSeshStyle;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;

/**
 *
 * @author rosmord
 */
/**
 * Check an bug found by O. Goelet : when a partial cartouche is used in a
 * column, there is a NullPointerException.
 */
public class TestPartialCartoucheDrawingsWithOrientation {

    private record TestArgs(String pictureName, TextOrientation orientation, TextDirection direction) {
    };

    public static Collection<TestArgs> testArgs = List.of(
            new TestArgs("lineL2R", TextOrientation.HORIZONTAL, TextDirection.LEFT_TO_RIGHT),
            new TestArgs("lineR2l", TextOrientation.HORIZONTAL, TextDirection.RIGHT_TO_LEFT),
            new TestArgs("columnL2R", TextOrientation.VERTICAL, TextDirection.LEFT_TO_RIGHT),
            new TestArgs("columnR2l", TextOrientation.VERTICAL, TextDirection.RIGHT_TO_LEFT));

    @ParameterizedTest
    @FieldSource("testArgs")
    public void testPartialCartouchesInLines(TestArgs args) throws MDCSyntaxError, IOException {
        String mdc = "<1--0>-ra-mn:n-xpr-<0--2>";
        JSeshStyle style = JSeshStyle.DEFAULT.copy()
                .options(o -> o.textDirection(args.direction()).textOrientation(args.orientation())).build();

        TestPictureFilesHelper.drawInPicture(mdc, args.pictureName(), style);
    }

}
