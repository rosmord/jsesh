package org.qenherkhopeshef.swingUtils;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;


/**
 * A file dialog class which is adapted to the actual system being used.
 * (it means JFileChooser on Windows/Linux and FileDialog on mac).
 *
 * The provided functionalities are not that great, which has consequences on the
 * suggested way of asking for files: if needed the file dialog should be lauched
 * from another dialog.
 * @author rosmord
 */

public abstract class PortableFileDialog {
    
    /**
     * Create a dialog for selecting a file for saving data.
     */
    public static PortableFileDialog createFileSaveDialog() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Create a dialog for selecting a directory for saving data.
     * The directory might or might not exist yet.
     * @return
     */
    public static PortableFileDialog createDirectorySaveDialog() {
        throw new UnsupportedOperationException();
    }

    /**
     * Create a dialog for selecting a file for reading data.
     */
    public static PortableFileDialog createFileLoadDialog() {
        throw new UnsupportedOperationException();
    }

    public static PortableFileDialog createDirectoryLoadDialog() {
        throw new UnsupportedOperationException();
    }

    /**
     * Display the file dialog.
     * @return either APPROVE_OPTION or CANCEL_OPTION
     * @see JFileChooser#APPROVE_OPTION
     * @see JFileChooser#CANCEL_OPTION
     */
    abstract int show();

    /**
     * Sets the currently selected file.
     * Will of course set the current directory.
     */
    abstract void setSelectedFile(File file);

    /**
     * Sets the current directory.
     * @param directory
     */
    abstract void setCurrentDirectory(File directory);

    /**
     * Returns the file (or directory) selected by the user.
     * @return
     */
    abstract File getSelectedFile();

    /**
     * Gets the selected file's potential parent directory.
     * If there is a selected file (or directory) returns its parent.
     * Else, return the directory where the selected file would be created or
     * searched for.
     * @return a directory.
     */
    abstract File getCurrentDirectory();

    /**
     * clear used resources if needed.
     */
    abstract void dispose();

    /**
     * Sets the list of file filters used by this Dialog.
     * Note that some platform won't be able to use a list of filters.
     * @param filters the filters to use
     * @param the "default" or "catch-all" filter to use if the implementation 
     *  doesn't allow a list of filters.
     *
     */
    abstract void setFileFilters(FileFilter filters[], FileFilter fallbackFilter);
}
