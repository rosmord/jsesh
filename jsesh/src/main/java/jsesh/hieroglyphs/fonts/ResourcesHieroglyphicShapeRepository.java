package jsesh.hieroglyphs.fonts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jsesh.hieroglyphs.data.coremdc.CanonicalCode;
import jsesh.hieroglyphs.data.coremdc.GardinerCode;
import jsesh.hieroglyphs.signshape.ShapeChar;
import jsesh.swing.signimportdialog.model.SVGSignSource;

/**
 * Font manager which takes its data from a resource bundle. The signs will be
 * stored as SVG files in the location given as resourcePath, and their names
 * shall be all lowercase. The list of the signs will be stored in the same
 * location, as an ASCII file, and called "list.txt"
 * 
 * @author rosmord
 * 
 */
public class ResourcesHieroglyphicShapeRepository implements
		HieroglyphShapeRepository {

	private final String resourcePath;

	public static final String LIST_FILE = "list.txt";

	/**
	 * Map from code to either resource path (String) or ShapeChar.
	 * <p>
	 * The idea is that we did not want to load all SVG signs in memory - which is
	 * quite reasonnable.
	 */
	private final ConcurrentHashMap<String, ShapeProxy> signs;

	public ResourcesHieroglyphicShapeRepository(String resourcePath) {
		this.resourcePath = resourcePath;
		signs = new ConcurrentHashMap<>();
		try {
			InputStream ins = getClass().getResourceAsStream(
					resourcePath + "/" + LIST_FILE);
			if (ins != null) {
				try (BufferedReader r = new BufferedReader(new InputStreamReader(
						ins, "US-ASCII"));) {
					String line;
					while ((line = r.readLine()) != null) {
						String svgFileName = line.trim();
						signs.put(fileNameToCode(svgFileName), new ShapeCodeProxy(svgFileName));
					}
				}
			} else {
				System.err.println("Could not load" + resourcePath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Takes a svg file name, and returns the corresponding code.
	 * @param svgFileName the name of the svg file
	 * @return the corresponding code
	 */
	private String fileNameToCode(String svgFileName) {
		String code = GardinerCode.getCodeForFileName(svgFileName);
		if (code == null) {
			code = svgFileName.substring(0, svgFileName.indexOf('.'));
		}
		return code;
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
						|| pathname.getName().toLowerCase(Locale.ENGLISH).endsWith(".svg");
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

	@Override	
	public ShapeChar get(CanonicalCode code) {
		return getForStringCode(code.code());
	}

	private ShapeChar getForStringCode(String code) {
		return switch (signs.compute(code, this::mapProxy)) {
			case ActualShape s -> s.shape();
			case null -> null;
			case ShapeCodeProxy c -> null; // Should not happen, because of mapProxy.
		};		
	}

	/**
	 * The map stores either :
	 * <ul>
	 * <li>a ShapeCodeProxy, which is a proxy for a resource path</li>
	 * <li>an ActualShape, which holds the final ShapeChar</li>
	 * </ul>
	 * Originally, the map will contain only ShapeCodeProxy. The first time
	 * a sign is requested, the ShapeCodeProxy will be replaced by an ActualShape, which will hold the ShapeChar.
	 * <p> all in a type-safe way!
	 * @param code
	 * @param currentValue
	 * @return
	 */
	private ShapeProxy mapProxy(String code, ShapeProxy currentValue) {
		if (currentValue == null) {
			return null;
		} else
			return (switch (currentValue) {
				case ShapeCodeProxy c -> {
					String path = resourcePath + "/" + c.filename();
					SVGSignSource src = new SVGSignSource(getClass().getResource(
							path));
					if (src.hasNext()) {
						src.next();
						yield new ActualShape(c.filename(), src.getCurrentShape());
					} else
						yield null; // should not happen. Consider throwing an exception instead.
				}
				case ActualShape s -> s;
			});
	}

	@Override
	public ShapeChar getSmallBody(CanonicalCode code) {
		return getForStringCode(code.code() + "_BOLD");
	}

	public Set<String> getCodes() {
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

	/**
	 * Just in case, let's make this class thread safe?
	 */
	private sealed interface ShapeProxy permits ShapeCodeProxy, ActualShape {
	}

	private final record ShapeCodeProxy(String filename) implements ShapeProxy {
	}

	private final record ActualShape(String filename, ShapeChar shape) implements ShapeProxy {
	}

}
