package jsesh.graphics.glyphs.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import jsesh.graphics.glyphs.bzr.BzrFontReader;
import jsesh.graphics.glyphs.bzr.BzrFormatException;
import jsesh.graphics.glyphs.bzr.simple.BzrSimpleFont;
import jsesh.graphics.glyphs.bzr.simple.BzrSimpleFontBuilder;
import jsesh.hieroglyphs.graphics.ShapeChar;

public class BZRSignSource implements SimpleSignSourceModel {

	int position;
	
	private BzrSimpleFont font;

	public BZRSignSource(File file) throws IOException, BzrFormatException {
		BzrSimpleFontBuilder builder= new BzrSimpleFontBuilder();
		BzrFontReader reader= new BzrFontReader(builder);
		reader.read(new FileInputStream(file));
		font= builder.getFont();
		beforeFirst();
	}

	public ShapeChar getCurrentShape() {
		return font.getChar(position);
	}

	public boolean hasNext() {
		return getNextPosition() != -1;
	}

	public boolean hasPrevious() {
		return getpreviousPosition() != -1;
	}

	public void next() {
		position= getNextPosition();
	}

	public void previous() {
		position= getpreviousPosition();
	}
	
	/**
	 * returns the next position, or -1 if none.
	 * @return
	 */
	private int getNextPosition() {
		int result= -1;
		for (int i= position+1; i < 256 && result == -1; i++) {
			if (font.getChar(i) != null)
				result= i;
		}
		return result;
	}
	
	private int getpreviousPosition() {
		int result= -1;
		for (int i= position-1; i >= 0 && result == -1; i--) {
			if (font.getChar(i) != null)
				result= i;
		}
		return result;
	}
	
	public String getCurrentCode() {
		return "";
	}

	public void afterLast() {
		position= 256;		
	}

	public void beforeFirst() {
		position= -1;
	}
}
