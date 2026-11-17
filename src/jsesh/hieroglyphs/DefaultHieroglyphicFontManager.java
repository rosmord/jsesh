package jsesh.hieroglyphs;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.prefs.Preferences;

import jsesh.bzr.simple.ShapeChar;

/**
 * Place holder for the default hieroglyphic font manager. The default system
 * currently uses both internal hieroglyphic fonts and a directory based system.
 * 
 * <p>
 * Could perhaps be observable.
 * 
 * @author rosmord
 * 
 */
public class DefaultHieroglyphicFontManager implements HieroglyphicFontManager {
	private static final String GLYPH_DIRECTORY = "glyphDirectory";

	static private DefaultHieroglyphicFontManager instance = null;

	private CompositeHieroglyphicFontManager composite;

	private DirectoryHieroglyphicFontManager directoryManager;

	private DefaultHieroglyphicFontManager() {
		composite = new CompositeHieroglyphicFontManager();
		directoryManager = new DirectoryHieroglyphicFontManager(new File(""));
		composite.addHieroglyphicFontManager(directoryManager);
		composite
				.addHieroglyphicFontManager(new MemoryHieroglyphicFontManager());

		initDirectory();
	}

	public static synchronized DefaultHieroglyphicFontManager getInstance() {
		if (instance == null)
			instance = new DefaultHieroglyphicFontManager();
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.hieroglyphs.CompositeHieroglyphicFontManager#addHieroglyphicFontManager(jsesh.hieroglyphs.HieroglyphicFontManager)
	 */
	public void addHieroglyphicFontManager(HieroglyphicFontManager manager) {
		composite.addHieroglyphicFontManager(manager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.hieroglyphs.CompositeHieroglyphicFontManager#get(java.lang.String)
	 */
	public ShapeChar get(String code) {
		return composite.get(code);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.hieroglyphs.CompositeHieroglyphicFontManager#getCodes()
	 */
	public Set getCodes() {
		return composite.getCodes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.hieroglyphs.CompositeHieroglyphicFontManager#hasNewSigns()
	 */
	public boolean hasNewSigns() {
		return composite.hasNewSigns();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.hieroglyphs.DirectoryHieroglyphicFontManager#getDirectory()
	 */
	public File getDirectory() {
		return directoryManager.getDirectory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.hieroglyphs.DirectoryHieroglyphicFontManager#setDirectory(java.io.File)
	 */
	public void setDirectory(File directory) {
		directoryManager.setDirectory(directory);
		String path = directory.getAbsolutePath();
		try {
			path = directory.getCanonicalPath();
		} catch (IOException e) {
			// e.printStackTrace();
		}

		Preferences preferences = Preferences.userNodeForPackage(this
				.getClass());

		preferences.put(GLYPH_DIRECTORY, path);
	}

	private void initDirectory() {
		Preferences preferences = Preferences.userNodeForPackage(this
				.getClass());
		String dirPath = preferences.get(GLYPH_DIRECTORY, "");
		directoryManager.setDirectory(new File(dirPath));
	}

	public void insertNewSign(String text, ShapeChar shapeChar) {
		directoryManager.insertNewSign(text, shapeChar);
	}
}
