package jsesh.graphics.glyphs.model;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import jsesh.hieroglyphs.graphics.ShapeChar;

public class TTFSignSource implements SimpleSignSourceModel {

	int currentPos= 0;
	ArrayList shapes;
	
	public TTFSignSource(File file) throws IOException {
		try {
			InputStream in = new FileInputStream(file);
			Font f = java.awt.Font.createFont(Font.TRUETYPE_FONT, in);
			f= f.deriveFont(14f);
	        in.close();
	
	        shapes= new ArrayList();
                // Maximum number of signs in this font.
                // 1.4 seems to get it wrong.
	        int max= Math.max(256,f.getNumGlyphs());
                // Number of usable glyphs detected.
                int currentlyKnown= 0;
	        // DO SOMETHING TO HAVE A "REASONABLE" SYSTEM HERE.
	        for (int i = 0; i <= 0xFFFF; i++) {
	            if (f.canDisplay((char)i)) {
                        currentlyKnown++;
                        if (currentlyKnown> max)
                            break;
	            	ShapeChar s= new ShapeChar();
	            	Shape shape= f.createGlyphVector(new FontRenderContext(null,false,true), ""+(char)i).getOutline();
	            	s.setShape(shape);
	            	//s.fixShape();
	            	Rectangle2D bbox= s.getBbox();
	            	if (bbox.getWidth() != 0 || bbox.getHeight() != 0)
	            		shapes.add(s);
	            }            
	        }			
		} catch (FontFormatException e) {
			throw new IOException(e.getMessage());
		}
		beforeFirst();
	}

	public ShapeChar getCurrentShape() {		
		return (ShapeChar) shapes.get(currentPos);
	}

	public boolean hasNext() {
		return currentPos + 1 < shapes.size();
	}

	public boolean hasPrevious() {
		return currentPos > 0;
	}

	public void next() {
		currentPos++;
	}

	public void previous() {
		currentPos--;
	}
	
	public String getCurrentCode() {
		return "";
	}

	public void afterLast() {
		currentPos= shapes.size();
		
	}

	public void beforeFirst() {
		currentPos= -1;
	}
}
