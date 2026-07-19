package jsesh.defaults;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.prefs.Preferences;

import jsesh.glyphs.shape.ShapeChar;
import jsesh.utils.io.DirectoryHolder;
import jsesh.utils.preferences.JSeshPreferenceKeys;
import jsesh.utils.preferences.JSeshPreferencesRoot;

/**
 * Access to the user personal font directory.
 * <p>
 * Can then be used to call
 * {@link HieroglyphResourcesBuilder#buildFull(DirectoryHolder, jsesh.glossary.Glossary)}.
 * <p>
 * will manage auto save of the preferences when the folder is modified.
 *
 * <p>
 * <b>Preference-node migration note:</b> the folder is persisted under
 * {@code userNodeForPackage(HieroglyphShapeRepository.class)} with key
 * {@link jsesh.utils.preferences.JSeshPreferenceKeys#GLYPH_DIRECTORY}. This resolves
 * to
 * the <em>same</em> node the retired {@code JSeshFullHieroglyphShapeRepository}
 * used (both classes live in {@code jsesh.glyphs.fonts}), so existing
 * users
 * keep their configured folder. Do not move {@code GLYPH_DIRECTORY} to another
 * package or the setting will be silently orphaned.
 */
public class UserFontDirectoryManager {

    /**
     * Creates a new instance of User font manager.
     * <p>
     * It will point to the folder defined in user preferences if any.
     * 
     * @return
     */
    public static UserFontDirectoryManager buildUserFontManager() {
        UserFontDirectoryManager res = new UserFontDirectoryManager(Optional.empty());
        res.initialiseFromPreferences();
        return res;
    }

    private DirectoryHolder userFontHolder = new DirectoryHolder();

    UserFontDirectoryManager(Optional<File> initialDirectory) {
        userFontHolder.directory(initialDirectory);
    }

    /**
     * Returns the font holder for the user font.
     *
     * @return
     */
    public DirectoryHolder getUserFontHolder() {
        return userFontHolder;
    }

    /**
     * Writes a new user sign into the user font folder.
     *
     * <p>
     * Writing a sign only needs to know the folder, so it lives here,
     * rather than on the read-only repository. The sign is
     * stored as {@code <code>.svg}. After writing we call
     * {@link DirectoryHolder#forceRefresh()}, which the
     * {@code DirectoryHieroglyphShapeRepository} built on this holder observes,
     * so the new sign becomes visible without reader and writer knowing each
     * other.
     *
     * @param code  the Manuel de Codage code for the new sign.
     * @param shape the shape to store.
     * @throws IllegalStateException if no user font folder is currently configured.
     */
    public void insertNewSign(String code, ShapeChar shape) {
        File folder = userFontHolder.optDirectory()
                .orElseThrow(() -> new IllegalStateException(
                        "No user font folder configured; cannot insert sign " + code));
        File f = new File(folder, code + ".svg");
        try (OutputStream out = new FileOutputStream(f)) {
            shape.exportToSVG(out, "UTF-8");
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write sign " + code + " to " + f, e);
        }
        userFontHolder.forceRefresh();
    }

    /**
     * Sets the folder to the one defined in user preferences if any.
     */
    public void initialiseFromPreferences() {
        Optional<File> optFile = Optional.empty();
        try {
            Preferences preferences = JSeshPreferencesRoot.getPreferences();
            String dirPath = preferences.get(JSeshPreferenceKeys.GLYPH_DIRECTORY, "");
            if (!"".equals(dirPath)) {
                File file = new File(dirPath);
                if (file.isDirectory()) {
                    optFile = Optional.of(new File(dirPath));
                }
            }
        } catch (javax.xml.parsers.FactoryConfigurationError e) {
            e.printStackTrace();
            // Let optfile be empty and that's it.
        }
        userFontHolder.directory(optFile);
    }

    /**
     * Saves the current folder to user preferences.
     */
    public void saveToPreferences() {
        Preferences preferences = JSeshPreferencesRoot.getPreferences();
        File newDirectory = userFontHolder.optDirectory().orElse(null);
        if (newDirectory == null) {
            preferences.remove(JSeshPreferenceKeys.GLYPH_DIRECTORY);
        } else {
            if (newDirectory.isDirectory()) {
                try {
                    preferences.put(JSeshPreferenceKeys.GLYPH_DIRECTORY, newDirectory.getCanonicalPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                preferences.remove(JSeshPreferenceKeys.GLYPH_DIRECTORY);
            }
        }
    }

}
