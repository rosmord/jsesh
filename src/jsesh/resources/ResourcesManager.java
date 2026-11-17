/*
 * Created on 3 oct. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.resources;

import java.util.HashMap;
import java.util.Map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.awt.Font;
import java.awt.FontFormatException;

import javax.swing.ImageIcon;

import jsesh.utils.PlatformDetection;

/**
 * A container for various resources (like fonts).
 * 
 * @author rosmord
 * 
 */
public class ResourcesManager {
	static private ResourcesManager instance;

	private Font transliterationFont;

	/**
	 * Name of the file (in the package ResourcesManager) which contains the definitions for the various icons.
	 * A global convention shall be that inactive icons will have " inactive" in their names.
	 */
	public static String ICONDEFINITIONS= "/jseshResources/iconsDefinitions.txt";
	
	/**
	 * A map linking symbolic names to icons.
	 */
	private Map iconMap = new HashMap();

	private ResourcesManager() {
		loadFonts();
		loadIcons();
	}

	private void loadIcons() {
		try {
			BufferedReader r = null;
			try {
				String s;
				r = new BufferedReader(new InputStreamReader(ResourcesManager.class
						.getResourceAsStream(ICONDEFINITIONS)));
				while ((s= r.readLine())!=  null) {
					String [] data= s.split(":");
					iconMap.put(data[0], new ImageIcon(ResourcesManager.class.getResource("/jseshResources/"+ data[1])));
				}
			} finally {
				if (r != null)
					r.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadFonts() {
		InputStream in = null;
		try {
			in = ResourcesManager.class
					.getResourceAsStream("/jseshResources/fonts/MDCTranslitLC.ttf");
			if (in == null)
				throw new FileNotFoundException("file not found");
			transliterationFont = Font.createFont(Font.TRUETYPE_FONT, in);
			transliterationFont = transliterationFont.deriveFont(12f);
		} catch (FontFormatException e) {
			System.err.println("problem with font file format " + e);
			e.printStackTrace();
			transliterationFont = new Font(null, Font.ITALIC, 12);
		} catch (IOException e) {
			System.err.println("problem with font loading " + e);
			transliterationFont = new Font(null, Font.ITALIC, 12);

			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	static public ResourcesManager getInstance() {
		if (instance == null) {
			instance = new ResourcesManager();
		}
		return instance;
	}

	/**
	 * @return the font for translitteration.
	 */

	public Font getTransliterationFont() {
		return transliterationFont;
	}

	public Reader getLigatureData() {
		Reader in;
		in = new InputStreamReader(ResourcesManager.class
				.getResourceAsStream("/jseshResources/data/ligatures.txt"));
		return in;
	}

	public Reader getDemoData() {
		Reader in;
		in = new InputStreamReader(ResourcesManager.class
				.getResourceAsStream("/jseshResources/data/allmdc.gly"));
		return in;
	}

	/**
	 * Returns the directory used for the database and other working files.
	 * 
	 * @return the directory
	 */
	public File getUserPrefsDirectory() {
		File result = new File(System.getProperty("user.home"));
		if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX) {
			result = new File(result, "Library");
			result = new File(result, "Preferences");
			result = new File(result, "JSesh");
		} else if (PlatformDetection.getPlatform() == PlatformDetection.WINDOWS) {
			result = new File(result, "JSeshData");
		} else { // Unix
			result = new File(result, ".jsesh");
		}
		if (!result.exists()) {
			result.mkdirs();
		}
		return result;
	}

	public ImageIcon getIcon(String symbolicName) {
		if (iconMap.containsKey(symbolicName)) {
			return  (ImageIcon) iconMap.get(symbolicName);
		} else {
			return  (ImageIcon) iconMap.get("Dummy");
		}
	}
}
