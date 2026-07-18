package jsesh.render.draw;


import org.junit.jupiter.api.Test;

import jsesh.render.style.JSeshStyle;

/**
 * Checks that ntrw and nn are correctly drawn in the BZR fonts.
 */
public class BzrFontNnAndNTrwTest {

    @Test
    public void nnTest() {
        String mdc = """
                +l nn:+s-nn-!
                +l M22B:+s-M22B-!
                """;

        TestPictureFilesHelper.drawInPicture(mdc, "nntest", JSeshStyle
        .DEFAULT);
    }


    @Test
    public void nTrwTest() {
         String mdc = """
                +l nTrw:+s-nTrw-!
                +l R8A:+s-R8A-!
                """;

        TestPictureFilesHelper.drawInPicture(mdc, "nTrwtest", JSeshStyle
        .DEFAULT);
    }

    
}
