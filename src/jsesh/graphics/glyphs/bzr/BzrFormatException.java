package jsesh.graphics.glyphs.bzr;

/**
 * Errors in BZR files.
 * 
 * 
 * Created: Sun Jun 2 13:31:43 2002
 * 
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC</a>
 * 
 */

public class BzrFormatException extends Exception {
    public BzrFormatException() {
    }

    public BzrFormatException(String msg) {
        super(msg);
    }

}// BzrFormatException
