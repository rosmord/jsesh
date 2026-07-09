package jsesh.defaults;

import jsesh.hieroglyphs.fonts.HieroglyphShapeRepository;
import jsesh.utils.DirectoryHolder;
import jsesh.utils.DirectoryHolder.DirectoryEvent;

import static jsesh.hieroglyphs.fonts.Constants.GLYPH_DIRECTORY;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.prefs.Preferences;

import org.qenherkhopeshef.observable.ObservableEventListener;

/**
 * Access to the user personal font directory.
 * <p> Can then be used to call {@link HieroglyphResourcesBuilder#buildFull(DirectoryHolder)} 
 * <p> will manage auto save of the preferences when the folder is modified.
 */
public class UserFontDirectoryManager {

    public static UserFontDirectoryManager buildUserFontManager() {
        return new UserFontDirectoryManager(readFromPreferences());
    }

    private DirectoryHolder userFontHolder = new DirectoryHolder();
	private ObservableEventListener<DirectoryEvent> folderChangedListener = event -> directoryUpdated(event);

    

    UserFontDirectoryManager(Optional<File> initialDirectory) {
        userFontHolder.directory(initialDirectory);
        userFontHolder.addListener(folderChangedListener);
    }

    /**
     * Method called when the folder is modified.
     * @param event
     */
    private void directoryUpdated(DirectoryEvent event) {
        Preferences preferences = Preferences.userNodeForPackage(HieroglyphShapeRepository.class);
		File newDirectory = event.newDirectory();
        if (newDirectory == null) {
			preferences.remove(GLYPH_DIRECTORY);
        } else {
            if (newDirectory.isDirectory()) {
                try {
                    preferences.put(GLYPH_DIRECTORY, newDirectory.getCanonicalPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                preferences.remove(GLYPH_DIRECTORY);
            }
        }
	}

	/**
     * reads the initial the folder holder from the preferences.
     * @return 
     */
    private static Optional<File> readFromPreferences() {
        try {
            Preferences preferences = Preferences.userNodeForPackage(HieroglyphShapeRepository.class);
            String dirPath = preferences.get(GLYPH_DIRECTORY, "");
            Optional<File> optFile = Optional.empty();
            if (! "".equals(dirPath)) {
                File file = new File(dirPath);
                if (file.isDirectory()) {
                    optFile = Optional.of(new File(dirPath));
                }
            }
            return optFile;
        } catch (javax.xml.parsers.FactoryConfigurationError e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Returns the font holder for the user font.
     * 
     * @return
     */
    public DirectoryHolder getUserFontHolder() {
        return userFontHolder;
    }

}
