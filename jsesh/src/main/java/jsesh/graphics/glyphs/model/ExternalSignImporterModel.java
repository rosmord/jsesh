package jsesh.graphics.glyphs.model;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import jsesh.graphics.glyphs.bzr.BzrFormatException;
import jsesh.hieroglyphs.graphics.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.graphics.HieroglyphicFontManager;
import jsesh.hieroglyphs.graphics.ShapeChar;

public class ExternalSignImporterModel {

	private File sourceDirectory;

	private SimpleSignSourceModel newSigns;

	private ShapeChar referenceShape;

	private double shapeScale;

	public ExternalSignImporterModel() {
		HieroglyphicFontManager db = DefaultHieroglyphicFontManager
				.getInstance();
		referenceShape = db.get("A1");
		sourceDirectory = new File(".");
		shapeScale = 1.0;
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
		ShapeChar insertedShape = (ShapeChar) getShapeChar().clone();
		insertedShape.scaleGlyph(shapeScale);
		DefaultHieroglyphicFontManager.getInstance().insertNewSign(text,
				insertedShape);
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

	/**
	 * @return the shapeScale
	 */
	public double getShapeScale() {
		return shapeScale;
	}

	/**
	 * @param shapeScale
	 *            the shapeScale to set
	 */
	public void setShapeScale(double shapeScale) {
		this.shapeScale = shapeScale;
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
		} else {
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
			// newSigns.getCurrentShape().scaleToHeight(referenceShape.getBbox().getHeight());
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
		getShapeChar().flipHorizontally();
	}

	public void flipVertically() {
		getShapeChar().flipVertically();
	}

	/**
	 * return the sign code proposed by the source.
	 * 
	 * @return a sign code.
	 */
	public String getDefaultSignCode() {
		if (newSigns != null)
			return newSigns.getCurrentCode();
		else
			return "";
	}

	/**
	 * @return Returns the sourceDirectory.
	 */
	public File getSourceDirectory() {
		return sourceDirectory;
	}

	/**
	 * @param sourceDirectory
	 *            The sourceDirectory to set.
	 */
	public void setSourceDirectory(File sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	/**
	 * Change the scale of the signs so that the current sign height is
	 * "height".
	 * 
	 * @param height
	 */
	public void resizeVerticallyTo(double height) {
		double currentHeight = getShapeChar().getBbox().getHeight();
		shapeScale = height / currentHeight;
	}

}
