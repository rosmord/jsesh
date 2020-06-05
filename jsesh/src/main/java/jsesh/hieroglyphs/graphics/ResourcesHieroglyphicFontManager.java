package jsesh.hieroglyphs.graphics;

import jsesh.hieroglyphs.data.GardinerCode;
import jsesh.hieroglyphs.graphics.HieroglyphicFontManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Set;

import jsesh.graphics.glyphs.model.SVGSignSource;

/**
 * Font manager which takes its data from a resource bundle. The signs will be
 * stored as SVG files in the location given as resourcePath, and their names
 * shall be all lowercase. The list of the signs will be stored in the same
 * location, as an ASCII file, and called "list.txt"
 * 
 * @author rosmord
 * 
 */
public class ResourcesHieroglyphicFontManager extends Object implements
		HieroglyphicFontManager {

	private String resourcePath;

	public static final String LIST_FILE = "list.txt";

	HashMap signs;

	public ResourcesHieroglyphicFontManager(String resourcePath) {
		this.resourcePath = resourcePath;
		signs = new HashMap();
		try {
			InputStream ins = getClass().getResourceAsStream(
					resourcePath + "/" + LIST_FILE);
			if (ins != null) {
				BufferedReader r = new BufferedReader(new InputStreamReader(
						ins, "US-ASCII"));
				String line;
				while ((line = r.readLine()) != null) {
					String fields[] = line.split("\t");
					signs.put(fields[0], fields[1]);
				}
				r.close();
			} else {
				System.err.println("Could not load" + resourcePath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fill the resource bundle with the relevant files. should be called
	 * when building the software.
	 * 
	 * @param src
	 * @param dest
	 * @throws IOException
	 */

	static public void initDirectory(File src) throws IOException {
                //System.err.println("pos " + src.getCanonicalPath());
		FileOutputStream os = new FileOutputStream(new File(src, LIST_FILE));
		OutputStreamWriter out = new OutputStreamWriter(os, "US-ASCII");
		initDirectoryAux(src, out);
		out.close();
	}

	static private void initDirectoryAux(File src, java.io.Writer out)
			throws IOException {
		File[] svgs = src.listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.isDirectory()
						|| pathname.getName().toLowerCase().endsWith(".svg");
			}
		});

		for (int i = 0; i < svgs.length; i++) {
			if (svgs[i].isDirectory()) {
				initDirectoryAux(svgs[i], out);
			} else {
				// Add the sign code to list.txt
				String code = GardinerCode.getCodeForFileName(svgs[i]
						.getName());
				if (code != null) {
					out.write(code);
					out.write('\t');
					out.write(svgs[i].getName());
					out.write('\n');
				} else {
					out.write(svgs[i].getName().substring(0, svgs[i].getName().indexOf('.')));
					out.write('\t');
					out.write(svgs[i].getName());
					out.write('\n');
				}
			}
		}
	}

	public ShapeChar get(String code) {
		ShapeChar result = null;
		if (signs.containsKey(code)) {
			Object data = signs.get(code);
			if (data instanceof String) {
				String path = resourcePath + "/" + data;
				SVGSignSource src = new SVGSignSource(getClass().getResource(
						path));
				if (src.hasNext()) {
					src.next();
					result = src.getCurrentShape();
					// Store in data.
					signs.put(code, result);
				}
			} else
				result = (ShapeChar) data;
		}
		return result;
	}

	public ShapeChar getSmallBody(String code) {
		return get(code + "_BOLD");
	}
	
	public Set getCodes() {
		return signs.keySet();
	}

	public boolean hasNewSigns() {
		return false;
	}

	/**
	 * Fill the resource bundle. should be called when building the software.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		System.err.println(new File(args[0]));
		initDirectory(new File(args[0]));
	}
}
