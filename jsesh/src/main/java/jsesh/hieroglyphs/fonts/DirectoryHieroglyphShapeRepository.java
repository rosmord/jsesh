package jsesh.hieroglyphs.fonts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

import jsesh.hieroglyphs.data.coremdc.CanonicalCode;
import jsesh.hieroglyphs.data.coremdc.GardinerCode;
import jsesh.hieroglyphs.data.coremdc.ManuelDeCodage;
import jsesh.hieroglyphs.signshape.ShapeChar;
import jsesh.swing.signimportdialog.model.SVGSignSource;
import org.qenherkhopeshef.observable.ObservableEventListener;
import org.qenherkhopeshef.observable.ObservableEventSupport;

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
public class DirectoryHieroglyphShapeRepository implements
		HieroglyphShapeRepository {

	private final ObservableEventSupport<HieroglyphShapeRepositoryChangedEvent> eventSupport = new ObservableEventSupport<>();

	private FolderProxy directory;

	private TreeMap<String, File> codeMap = new TreeMap<String, File>(
			ManuelDeCodage.getCodeComparator());

	private HashMap<String, ShapeChar> signsMap;

	private long lastRefreshed;

	private boolean hasNewSigns;

	/**
	 * Create a directory font manager which will take its data from the given
	 * directory.
	 * 
	 * @param directory May be null.
	 */
	public DirectoryHieroglyphShapeRepository(File directory) {
		super();
		this.directory = new FolderProxy(directory);

		signsMap = new HashMap<String, ShapeChar>();
		hasNewSigns = true;
		lastRefreshed = System.currentTimeMillis();
		refresh();
	}

	public void refresh() {
		codeMap.clear();
		signsMap.clear();
		File[] contents;

		// List all svg files.
		contents = directory.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase(Locale.ENGLISH).endsWith(".svg");
			};
		});
		// do nothing if the directory doesn't exist.
		if (contents == null)
			return;
		// get the corresponding names.
		for (int i = 0; i < contents.length; i++) {
			String code = GardinerCode
					.getCodeForFileName(contents[i].getName());
			if (code != null) {
				codeMap.put(code, contents[i]);
			} else {
				code = contents[i].getName();
				code = code.substring(0, code.indexOf('.'));
				codeMap.put(code, contents[i]);
			}
		}
		hasNewSigns = true;
		lastRefreshed = directory.lastModified();
	}

	@Override	
	public ShapeChar get(CanonicalCode code) {
		return getForStringCode(code.code());
	}

	private ShapeChar getForStringCode(String code) {
		ShapeChar result = null;
		refreshIfNeeded();
		if (signsMap.containsKey(code))
			result = signsMap.get(code);
		else if (codeMap.containsKey(code)) {
			SVGSignSource src = new SVGSignSource(codeMap.get(code));
			result = src.getCurrentShape();
			signsMap.put(code, result);
		}
		return result;
	}

	@Override
	public ShapeChar getSmallBody(CanonicalCode code) {
		return getForStringCode(code + "_BOLD");
	}

	public Set<String> getCodes() {
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
		if (directory == null || directory.lastModified() > lastRefreshed) {
			refresh();
		}
	}

	/**
	 * Returns the directory where user-defined glyphs are kept.
	 * <p> If it's not defined, may be null.
	 * @return Returns the directory (may be null)
	 */
	public File getDirectory() {
		return directory.getFolder();
	}

	public void insertNewSign(String code, ShapeChar shapeChar) {
		if (directory.getFolder() == null)
			throw new NullPointerException("Can not insert files in " + null);
		File f = new File(directory.getFolder(), code + ".svg");
		OutputStream out;
		try {
			out = new FileOutputStream(f);
			shapeChar.exportToSVG(out, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		hasNewSigns = true;
		eventSupport.fireEvent(new HieroglyphShapeRepositoryChangedEvent());
	}

	public boolean hasNewSigns() {	
		if (directory.lastModified() > lastRefreshed)
			hasNewSigns = true;
		return hasNewSigns;
	}

	public void setDirectory(File directory) {
		this.directory = new FolderProxy(directory);
		refresh();
		eventSupport.fireEvent(new HieroglyphShapeRepositoryChangedEvent());
	}

	@Override
	public void addListener(ObservableEventListener<HieroglyphShapeRepositoryChangedEvent> listener) {
		eventSupport.addListener(listener);
	}

	@Override
	public void removeListener(ObservableEventListener<HieroglyphShapeRepositoryChangedEvent> listener) {
		eventSupport.removeListener(listener);
	}

	/**
	 * Proxy class supporting "null" folder in a gracefull way.
	 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
	 *
	 */
	private static class FolderProxy {
		File folder;
		long lastModified= 0;

		public FolderProxy(File folder) {
			super();
			this.folder = folder;
			this.lastModified= System.currentTimeMillis();
		}

		public File getFolder() {
			return folder;			
		}

		public long lastModified() {
			if (folder != null)
				return folder.lastModified();
			else
				return lastModified;
		}

		public File[] listFiles(FilenameFilter filenameFilter) {
			if (folder != null)
				return folder.listFiles(filenameFilter);
			else
				return new File[0];
		}

	}
}
