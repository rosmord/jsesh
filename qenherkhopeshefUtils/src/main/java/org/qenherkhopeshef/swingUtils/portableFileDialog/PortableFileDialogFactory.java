package org.qenherkhopeshef.swingUtils.portableFileDialog;

import java.awt.Component;

import org.qenherkhopeshef.utils.PlatformDetection;

/**
 * Factory for creating portable file dialogs depending on needs.
 *
 * @author rosmord
 *
 */
public class PortableFileDialogFactory {

    /**
     * Create a dialog for selecting a file for saving data.
     */
    public static PortableFileDialog createFileSaveDialog(Component parent) {
        PortableFileDialog dialog = createDialog(parent);
        dialog.setOperation(FileOperation.SAVE_FILE);
        return dialog;
    }

    private static PortableFileDialog createDialog(Component parent) {
        PortableFileDialog dialog;
        if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX) {
            if ("1.7".compareTo(System.getProperty("java.version")) > 0) {
                dialog = new AwtPortableFileDialog(parent);
            } else {
                dialog= new MacPortableFileDialog(parent);
            }
        } else {
            dialog = new SwingFileDialog(parent);
        }
        return dialog;
    }

    /**
     * Create a dialog for selecting a file for reading data.
     */
    public static PortableFileDialog createFileOpenDialog(Component parent) {
        PortableFileDialog dialog = createDialog(parent);
        dialog.setOperation(FileOperation.OPEN_FILE);
        return dialog;
    }

    /**
     * Create a dialog for selecting a directory for saving data. The directory
     * might or might not exist yet.
     *
     * @return
     */
    public static PortableFileDialog createDirectorySaveDialog(Component parent) {
        PortableFileDialog dialog = createDialog(parent);
        dialog.setOperation(FileOperation.SAVE_DIRECTORY);
        return dialog;
    }

    /**
     * Create a dialog for selecting an existing directory.
     *
     * @return
     */
    public static PortableFileDialog createDirectoryOpenDialog(Component parent) {
        PortableFileDialog dialog = createDialog(parent);
        dialog.setOperation(FileOperation.OPEN_DIRECTORY);
        return dialog;
    }
}
