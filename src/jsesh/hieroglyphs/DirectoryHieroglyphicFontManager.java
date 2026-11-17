package jsesh.hieroglyphs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import jsesh.bzr.simple.ShapeChar;
import jsesh.externalFonts.model.SVGSignSource;

/**
 * A font manager which stores the signs as files in a directory. The codes for
 * the signs are simply the names of the files ; for instance, "A320.svg" would
 * contain the code for the sign "A320". Note that, as files systems are not
 * always case-sensitive, "a320.svg" would do just the same, and "aa320.svg"
 * would be a correct file name for the sign "Aa320".
 * 
 * @author rosmord
 * 
 */
public class DirectoryHieroglyphicFontManager implements HieroglyphicFontManager {

	File directory;

	HashMap codeMap;

	HashMap signsMap;

	long lastRefreshed;

	boolean hasNewSigns;

	/**
	 * Create a directory font manager which will take its data from the given
	 * directory.
	 * 
	 * @param directory
	 */
	public DirectoryHieroglyphicFontManager(File directory) {
		super();
		this.directory = directory;
		codeMap = new HashMap();
		signsMap = new HashMap();
		hasNewSigns = true;
		lastRefreshed = System.currentTimeMillis();
		refresh();

	}

	public void refresh() {
		codeMap.clear();
		signsMap.clear();
		// List all svg files.
		File[] contents = directory.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".svg");
			};
		});
		// do nothing if the directory doesn't exist.
		if (contents == null)
			return;
		// get the corresponding names.
		for (int i = 0; i < contents.length; i++) {
			String code = SVGSignSource.getCodeForFile(contents[i]);
			if (code != null) {
				codeMap.put(code, contents[i]);
			}
		}
		hasNewSigns= true;
		lastRefreshed = directory.lastModified();
	}

	public ShapeChar get(String code) {
		ShapeChar result = null;
		refreshIfNeeded();
		if (signsMap.containsKey(code))
			result = (ShapeChar) signsMap.get(code);
		else if (codeMap.containsKey(code)) {
			SVGSignSource src = new SVGSignSource((File) codeMap.get(code));
			result = src.getCurrentShape();
		}
		return result;
	}

	public Set getCodes() {
		refreshIfNeeded();
		hasNewSigns = false;
		return codeMap.keySet();
	}

	/**
	 * Looks if the directory has changed.
	 * 
	 * @return
	 */
	private void refreshIfNeeded() {
		if (directory.lastModified() > lastRefreshed) {
			refresh();						
		}
	}

	/**
	 * @return Returns the directory.
	 */
	public File getDirectory() {
		return directory;
	}

	public void insertNewSign(String code, ShapeChar shapeChar) {
		File f = new File(directory, code + ".svg");
		OutputStream out;
		try {
			out = new FileOutputStream(f);
			shapeChar.exportToSVG(out, "US-ASCII");
		} catch (IOException e) {
			e.printStackTrace();
		}
		hasNewSigns = true;
	}

	public boolean hasNewSigns() {
		if (directory.lastModified() > lastRefreshed)
			hasNewSigns = true;
		return hasNewSigns;
	}
	
	public void setDirectory(File directory) {
		this.directory = directory;
		refresh();		
	}
}
