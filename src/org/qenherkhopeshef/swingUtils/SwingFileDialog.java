/*
 * This file is distributed under the LGPL
 * Author: Serge Rosmorduc
 */
package org.qenherkhopeshef.swingUtils;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * A Swing implementation of PortableFileDialog. Fit for Unix implementation,
 * and good in Windows for some purposes.
 * 
 * @author rosmord
 */
class SwingFileDialog extends PortableFileDialog {

	private JFileChooser delegate;
	private Component parent;

	public SwingFileDialog(Component parent) {
		delegate = new JFileChooser();
		this.parent = parent;
	}

	public FileOperationResult show() {
		int result = JFileChooser.CANCEL_OPTION;
		switch (operation) {
		case OPEN_FILE:
			result = delegate.showOpenDialog(parent);
			break;
		case SAVE_FILE:
			result = delegate.showSaveDialog(parent);
			break;
		case SAVE_DIRECTORY:
			delegate.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			result = delegate.showSaveDialog(parent);
			break;
		default:
			break;
		}
		if (result == JFileChooser.APPROVE_OPTION)
			return FileOperationResult.OK;
		else
			return FileOperationResult.CANCEL;
	}

	public void setSelectedFile(File file) {
		delegate.setSelectedFile(file);
	}

	public void setCurrentDirectory(File directory) {
		delegate.setCurrentDirectory(directory);
	}

	public File getSelectedFile() {
		return delegate.getSelectedFile();
	}

	public File getCurrentDirectory() {
		return delegate.getCurrentDirectory();
	}

	public void dispose() {
		delegate = null;
		parent = null;
	}

	public void setFileFilters(FileFilter[] filters) {
		// We suppose that filters may not be null or empty
		if (filters == null || filters.length == 0)
			throw new IllegalArgumentException("filters may not be empty or null");
		if (filters.length == 1) {
			delegate.setFileFilter(filters[0]);
		} else {
			for (int i = 0; i < filters.length; i++) {
				delegate.addChoosableFileFilter(filters[i]);
			}
		}
	}
}
