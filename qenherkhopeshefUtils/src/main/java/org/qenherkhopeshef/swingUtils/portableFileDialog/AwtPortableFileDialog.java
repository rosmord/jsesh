/*
 * This file is distributed under the LGPL
 * Author: Serge Rosmorduc
 */

package org.qenherkhopeshef.swingUtils.portableFileDialog;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

/**
 * A PortableFileDialog using AWT File Dialog. Interesting for use on Mac OS
 * and, in some cases, on Windows XP.
 * 
 * @author rosmord
 */
public class AwtPortableFileDialog extends PortableFileDialog {

	private Component parent;
	//private File base;
	private FileDialog delegate;
	//private String title;

	/**
	 * Create a portable file dialog using AWT (i.e. a system dialog).
	 * @param parent must have a frame or a dialog as an ancestor.
	 */
	public AwtPortableFileDialog(Component parent) {
		this.parent = parent;
		Window ancestor = SwingUtilities.getWindowAncestor(parent);
		if (ancestor instanceof Frame)
			delegate = new FileDialog((Frame) ancestor);
		else if (ancestor instanceof Dialog)
			delegate= new FileDialog((Dialog)ancestor);
		else { // null ?
			delegate= new FileDialog((Frame)null);
		}
			
	}

	@Override
	public void dispose() {
		if (delegate != null) {
			delegate.dispose();
			setDelegate(null);
		}
	}

	@Override
	public File getCurrentDirectory() {
		return new File(delegate.getDirectory());
	}

	@Override
	public File getSelectedFile() {
		return new File(getCurrentDirectory(), delegate.getFile());
	}

	@Override
	public void setCurrentDirectory(File directory) {
		delegate.setDirectory(directory.getAbsolutePath());
	}

	/**
	 * Sets the file filter to use.
	 * 
	 * @param filters
	 *            : list of filters. Ignored, as not usable with AWT.
	 * @param fallbackFilter
	 *            : the filter used.
	 */
	@Override
	public void setFileFilters(FileFilter[] filters) {
		delegate.setFilenameFilter(new FileFilterAdapter(filters));
	}

	@Override
	public void setSelectedFile(File file) {
		if (file != null)
			delegate.setFile(file.getAbsolutePath());
		else
			delegate.setFile(null);
	}

	@Override
	public FileOperationResult show() {
		switch (operation) {
		case OPEN_FILE:
			delegate.setMode(FileDialog.LOAD);
			delegate.setVisible(true);
			break;
		case OPEN_DIRECTORY:
			System.setProperty("apple.awt.fileDialogForDirectories", "true");
			delegate.setMode(FileDialog.LOAD);
			delegate.setFilenameFilter(new FilenameFilter() {
				public boolean accept(File arg0, String arg1) {
					return arg0.isDirectory();
				}
			});
			delegate.setAlwaysOnTop(true);
			delegate.setVisible(true);
			System.setProperty("apple.awt.fileDialogForDirectories", "false");
			break;
		case SAVE_FILE:
			delegate.setMode(FileDialog.SAVE);
			delegate.setVisible(true);
			break;
		case SAVE_DIRECTORY:
			System.setProperty("apple.awt.fileDialogForDirectories", "true");
			delegate.setMode(FileDialog.SAVE);
			delegate.setFilenameFilter(new FilenameFilter() {
				public boolean accept(File arg0, String arg1) {
					return arg0.isDirectory();
				}
			});
			delegate.setMode(FileDialog.LOAD);
			delegate.setAlwaysOnTop(true);
			delegate.setVisible(true);
			System.setProperty("apple.awt.fileDialogForDirectories", "false");
			break;
		default:
			break;
		}
		if (delegate.getFile() == null)
			return FileOperationResult.CANCEL;
		else
			return FileOperationResult.OK;
	}

	private void setDelegate(FileDialog delegate) {
		this.delegate = delegate;
	}

	private static class FileFilterAdapter implements FilenameFilter {
		private FileFilter[] swingFilters;

		public FileFilterAdapter(FileFilter[] swingFilters) {
			super();
			this.swingFilters = swingFilters.clone();
		}


		public boolean accept(File dir, String name) {
			for (FileFilter filter : swingFilters) {
				if (filter.accept(new File(dir, name)))
					return true;
			}
			return false;
		}
	}

	@Override
	public void setTitle(String title) {
		delegate.setTitle(title);
	}

	@Override
	public void setParent(Component parent) {
		// Parent must be a frame for an AwtPortableFileDialog...
		try {
			this.parent = (Frame) parent;
		} catch (ClassCastException e) {
			e.printStackTrace();
			this.parent = null;
		}
	}

}
