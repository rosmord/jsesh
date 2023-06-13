package jsesh.hieroglyphs.graphics;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.prefs.Preferences;
import jsesh.hieroglyphs.data.HieroglyphDatabaseFactory;
import jsesh.hieroglyphs.data.coreMdC.GardinerCode;
import jsesh.hieroglyphs.data.coreMdC.ManuelDeCodage;

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

	private ResourcesHieroglyphicFontManager resourcesHieroglyphicFontManager;

	public DefaultHieroglyphicFontManager() {
		composite = new CompositeHieroglyphicFontManager();
		directoryManager = new DirectoryHieroglyphicFontManager(new File(""));
		composite.addHieroglyphicFontManager(directoryManager);
		resourcesHieroglyphicFontManager = new ResourcesHieroglyphicFontManager(
				"/jseshGlyphs");
		composite.addHieroglyphicFontManager(resourcesHieroglyphicFontManager);
		composite
				.addHieroglyphicFontManager(new MemoryHieroglyphicFontManager());
		initDirectory();
	}

	/*
	public static synchronized DefaultHieroglyphicFontManager getInstance() {
		if (instance == null)
			instance = new DefaultHieroglyphicFontManager();
		return instance;
	}
	*/

	
	public void addHieroglyphicFontManager(HieroglyphicFontManager manager) {
		composite.addHieroglyphicFontManager(manager);
	}

	@Override
	public ShapeChar get(String code) {
		String canonicalCode = ManuelDeCodage.getInstance().getCanonicalCode(code);
		return composite.get(canonicalCode);
	}

        @Override
	public ShapeChar getSmallBody(String code) {
    	String canonicalCode = ManuelDeCodage.getInstance().getCanonicalCode(code);
		return composite.getSmallBody(canonicalCode);
	}

	@Override
	public Set<String> getCodes() {
		return composite.getCodes();
	}

	@Override
	public boolean hasNewSigns() {
		return composite.hasNewSigns();
	}

	public File getDirectory() {
		return directoryManager.getDirectory();
	}

	public void setDirectory(File directory) {
		Preferences preferences = Preferences.userNodeForPackage(this
				.getClass());
		directoryManager.setDirectory(directory);
		if (directory != null) {
			String path = directory.getAbsolutePath();
			try {
				path = directory.getCanonicalPath();
			} catch (IOException e) {
				// e.printStackTrace();
			}
			preferences.put(GLYPH_DIRECTORY, path);
		} else {
			preferences.remove(GLYPH_DIRECTORY);
		}
	}

	private void initDirectory() {
		try {
			Preferences preferences = Preferences.userNodeForPackage(this
					.getClass());
			String dirPath = preferences.get(GLYPH_DIRECTORY, "");
			directoryManager.setDirectory(new File(dirPath));
		} catch (javax.xml.parsers.FactoryConfigurationError e) {
			e.printStackTrace();
		}
	}

	public void insertNewSign(String text, ShapeChar shapeChar) {
		directoryManager.insertNewSign(text, shapeChar);
	}
}
