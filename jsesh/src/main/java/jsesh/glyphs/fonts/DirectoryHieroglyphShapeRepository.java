package jsesh.glyphs.fonts;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.qenherkhopeshef.observable.ObservableEventListener;
import org.qenherkhopeshef.observable.ObservableEventSupport;

import jsesh.glyphs.shape.ShapeChar;
import jsesh.glyphs.signsource.SVGSignSource;
import jsesh.signcodes.CanonicalCode;
import jsesh.signcodes.GardinerCode;
import jsesh.signcodes.ManuelDeCodage;
import jsesh.utils.io.DirectoryHolder;

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

	private DirectoryProxy directoryProxy;

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
	public DirectoryHieroglyphShapeRepository(DirectoryHolder directory) {
		this.directoryProxy = new DirectoryProxy(directory);

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
		contents = directoryProxy.listFiles(new FilenameFilter() {
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
		lastRefreshed = directoryProxy.lastModified();
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
		if (directoryProxy == null || directoryProxy.lastModified() > lastRefreshed) {
			refresh();
		}
	}

	/**
	 * Returns the directory where user-defined glyphs are kept.
	 * <p> If it's not defined, may be null.
	 * @return Returns the directory (may be null)
	 */
	public Optional<File> optDirectory() {
		return directoryProxy.getDirectoryHolder().optDirectory();
	}

	// Note: writing new signs is no longer a repository concern. Writing only
	// needs to know the folder, so it lives on UserFontDirectoryManager, which
	// owns the DirectoryHolder. After a write it calls holder.forceRefresh(),
	// which this repository observes and reloads from — reader and writer never
	// reference each other, they rendezvous at the DirectoryHolder.

	public boolean hasNewSigns() {
		if (directoryProxy.lastModified() > lastRefreshed)
			hasNewSigns = true;
		return hasNewSigns;
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
	 * A DirectoryHieroglyphShapeRepository with a null folder is a valid object,
	 * but it will not contain any sign. This avoids null checks in the code.
	 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
	 */

	private class DirectoryProxy {
		DirectoryHolder directoryHolder;

		long lastModified= 0;

		public DirectoryProxy(DirectoryHolder directoryHolder) {
			this.directoryHolder = directoryHolder;
			directoryHolder.addListener(e -> {
				// A holder event means the folder *identity* changed (or a
				// forceRefresh() was requested): reload unconditionally.
				// We must NOT gate this on lastModified(), because the new
				// folder may have an older mtime than the one we last read,
				// which would silently skip the reload.
				// Content-only changes *within* the same folder are handled
				// lazily by refreshIfNeeded()'s mtime poll on reads.
				this.lastModified = System.currentTimeMillis();
				refresh();
				// Propagate to our own listeners (e.g. the enclosing composite
				// and, through it, the UI) so a folder change is visible.
				eventSupport.fireEvent(new HieroglyphShapeRepositoryChangedEvent());
			});
			this.lastModified= System.currentTimeMillis();
		}

		public DirectoryHolder getDirectoryHolder() {
			return directoryHolder;			
		}

		public long lastModified() {
			return directoryHolder.optDirectory().map(File::lastModified).orElse(lastModified);
		}

		public File[] listFiles(FilenameFilter filenameFilter) {
			return directoryHolder.optDirectory().
				map(d -> d.listFiles(filenameFilter)).orElse(new File[0]);			
		}

	}
}
