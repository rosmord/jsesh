package jsesh.externalFonts.model;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Dictionary;

import jsesh.bzr.BzrFormatException;
import jsesh.bzr.simple.ShapeChar;
import jsesh.hieroglyphs.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.DirectoryHieroglyphicFontManager;
import jsesh.hieroglyphs.HieroglyphicDB;
import jsesh.hieroglyphs.HieroglyphicFontManager;

public class ExternalSignImporterModel {

	private SimpleSignSourceModel newSigns;

	private ShapeChar referenceShape;

	public ExternalSignImporterModel() {
			HieroglyphicFontManager db= DefaultHieroglyphicFontManager.getInstance();
			referenceShape = db.get("A1");
	}

	public boolean hasNext() {
		if (newSigns != null)
			return newSigns.hasNext();
		else
			return false;
	}

	public boolean hasPrevious() {
		if (newSigns != null)
			return newSigns.hasPrevious();
		else
			return false;
	}

	public ShapeChar getShapeChar() {
		if (newSigns != null)
			return newSigns.getCurrentShape();
		else
			return null;
	}

	public ShapeChar getReferenceShape() {
		return referenceShape;
	}

	public void insertSign(String text) {
			
			DefaultHieroglyphicFontManager.getInstance().insertNewSign(text, getShapeChar());
			
	}

	public void previousSign() {
		if (newSigns != null) {
			newSigns.previous();
		}
	}

	public void nextSign() {
		if (newSigns != null) {
			newSigns.next();
		}
	}

	public void loadSigns(File file) throws IOException {

		String fileName = file.getName().toLowerCase();
		if (fileName.endsWith(".ttf")) {
			loadTTF(file);
		} else if (fileName.endsWith(".bzr")) {
			loadBZR(file);
		} else if (fileName.endsWith(".svg")) {
			loadSVG(file);
		} else if (fileName.endsWith(".tml")) {
			loadTml(file);
		}
		else {
			throw new IOException("Unknown format");
		}
		/*
		 * Place the cursor on the first record
		 */
		if (newSigns != null && newSigns.hasNext())
			newSigns.next();

	}

	private void loadSVG(File file) throws IOException {
		newSigns = new SVGSignSource(file);
		// scale to cadrat height.
		while (newSigns.hasNext()) {
			newSigns.next();			
			newSigns.getCurrentShape().scaleToHeight(referenceShape.getBbox().getHeight());
		}
		newSigns.beforeFirst();
	}

	private void loadBZR(File file) throws IOException {
		try {
			newSigns = new BZRSignSource(file);
		} catch (BzrFormatException e) {
			throw new IOException("Bad file format");
		}
	}

	private void loadTTF(File file) throws IOException {
		newSigns = new TTFSignSource(file);
	}

	private void loadTml(File file) throws IOException {
		FileReader r = new FileReader(file);
		newSigns = new TmlSignsImporter(r);
	}

	public boolean fileFormatIsKnown(File file) {
		if (file.isDirectory())
			return true;
		String fileName = file.getName().toLowerCase();
		if (fileName.endsWith(".ttf")) {
			return true;
		} else if (fileName.endsWith(".bzr")) {
			return true;
		} else if (fileName.endsWith(".svg")) {
			return true;
		} else if (fileName.endsWith(".tml")) {
			return true;
		}
		return false;
	}

	public String getKnownFormats() {
		return "TTF (true type fonts), bzr (Gnu font util fonts), SVG (svg sign drawing)";
	}

	public void flipHorizontally() {
		Shape s = getShapeChar().getShape();
		s = new AffineTransform(-1, 0, 0, 1, (float) getShapeChar().getBbox()
				.getWidth(), 0).createTransformedShape(s);
		getShapeChar().setShape(s);
	}

	public void flipVertically() {
		Shape s = getShapeChar().getShape();
		s = new AffineTransform(1, 0, 0, -1, 0, (float) getShapeChar()
				.getBbox().getHeight()).createTransformedShape(s);
		getShapeChar().setShape(s);
	}

	public void resizeVerticallyTo(double height) {
		double currentHeight = getShapeChar().getBbox().getHeight();
		double scale = height / currentHeight;
		Shape s = AffineTransform.getScaleInstance(scale, scale)
				.createTransformedShape(getShapeChar().getShape());
		getShapeChar().setShape(s);
		getShapeChar().fixShape();
	}

	/**
	 * return the sign code proposed by the source.
	 * @return a sign code.
	 */
	public String getDefaultSignCode() {
		if (newSigns!=null)
			return newSigns.getCurrentCode();
		else
			return "";
	}
}
