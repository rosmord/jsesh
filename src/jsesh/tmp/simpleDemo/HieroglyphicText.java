package jsesh.tmp.simpleDemo;

/**
 * Text Represents a simple basic hieroglyphic text; this is definitly
 * not the base for a full system.
 *
 * A Text is made of lines, themselves mades of cadrats, made of Horizontal lines, made of Glyphs
 * a glyph is simply represented by a String.
 *
 * Created: Fri Jun  7 16:25:10 2002
 *
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC</a>
 */

import java.util.LinkedList;

public class HieroglyphicText {

    public LinkedList lines;
    
    public static class Hieroglyph {
	String code;
	public Hieroglyph(String code) {
	    this.code= code;
	}
    }

    public HieroglyphicText (){
	lines= new LinkedList();
    }

    public void add(LinkedList line) {
	lines.add(line);
    }
    
}// Text
