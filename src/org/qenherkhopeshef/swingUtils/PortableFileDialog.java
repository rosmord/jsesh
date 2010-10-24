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
 * 
 * @author rosmord
 */

public abstract class PortableFileDialog {
    
	protected FileOperation operation;
    /**
     * Display the file dialog.
     * @return either APPROVE_OPTION or CANCEL_OPTION
     * @see JFileChooser#APPROVE_OPTION
     * @see JFileChooser#CANCEL_OPTION
     */
    public abstract FileOperationResult show();

    /**
     * Sets the currently selected file.
     * Will of course set the current directory.
     */
    public abstract void setSelectedFile(File file);

    /**
     * Selects the operation this dialog is supposed to perform.
     * @param operation
     */
    protected void setOperation(FileOperation operation) {
		this.operation = operation;
	}
    
    /**
     * Sets the current directory.
     * @param directory
     */
    public abstract void setCurrentDirectory(File directory);

    /**
     * Returns the file (or directory) selected by the user.
     * @return
     */
    public abstract File getSelectedFile();

    /**
     * Gets the selected file's potential parent directory.
     * If there is a selected file (or directory) returns its parent.
     * Else, return the directory where the selected file would be created or
     * searched for.
     * @return a directory.
     */
    public abstract File getCurrentDirectory();

    /**
     * clear used resources if needed.
     */
    public abstract void dispose();

    /**
     * Sets the list of file filters used by this Dialog.
     * @param filters the filters to use
     */
    public abstract void setFileFilters(FileFilter filters[]);
    
    /**
     * Sets the file filter to use for this dialog (iff there is only one file filter to define).
     * @param filter
     */
    public void setFileFilter(FileFilter filter) {
    		setFileFilters(new FileFilter[]{filter});
    }
    	
 
}
