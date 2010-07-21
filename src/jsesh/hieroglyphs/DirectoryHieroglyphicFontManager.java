package jsesh.hieroglyphs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import jsesh.graphics.glyphs.model.SVGSignSource;

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

	TreeMap codeMap= new TreeMap(GardinerCode.getCodeComparator());

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
			String code = GardinerCode.getCodeForFileName(contents[i].getName());
			if (code != null) {
				codeMap.put(code, contents[i]);
			} else {
				code= contents[i].getName();
				code= code.substring(0,code.indexOf('.'));
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
			signsMap.put(code, result);
		}
		return result;
	}

	public ShapeChar getSmallBody(String code) {
		return get(code + "_BOLD");
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
			shapeChar.exportToSVG(out, "UTF-8");
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
