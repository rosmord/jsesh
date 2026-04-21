package jsesh.hieroglyphs.fonts;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.prefs.Preferences;

import jsesh.hieroglyphs.data.coreMdC.ManuelDeCodage;
import jsesh.hieroglyphs.signshape.ShapeChar;
import static jsesh.hieroglyphs.fonts.Constants.GLYPH_DIRECTORY;

/**
 * The full hieroglyph shape repository, used by the JSesh software, including user defined signs and standard JSesh fonts.
 * 
 * <p> Will typically be used as a unique resource (as of today).
 * 
 * <p> it is possible to change the directory.
 */
public class JSeshFullHieroglyphShapeRepository implements HieroglyphShapeRepository {
    
	private CompositeHieroglyphShapeRepository composite;

	private DirectoryHieroglyphShapeRepository directoryManager;

	public JSeshFullHieroglyphShapeRepository() {
		composite = new CompositeHieroglyphShapeRepository();
		directoryManager = new DirectoryHieroglyphShapeRepository(new File(""));
		composite.addHieroglyphicFontManager(directoryManager);
		composite.addHieroglyphicFontManager(new ResourcesHieroglyphicShapeRepository(Constants.STANDARD_JSESH_FONT_RESOURCE_PATH));
		composite
				.addHieroglyphicFontManager(GnutraceHieroglyphShapeRepository.getInstance());
		initDirectory();
	}
	

	public void addHieroglyphicFontManager(HieroglyphShapeRepository manager) {
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
