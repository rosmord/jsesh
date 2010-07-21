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
 * A Swing implementation of PortableFileDialog.
 * Fit for Unix implementation, and good in Windows for some purposes.
 * @author rosmord
 */
class SwingFileDialog extends PortableFileDialog {

    JFileChooser delegate;
    Component parent;

    public int show() {
        delegate.showOpenDialog(parent);
        delegate.showSaveDialog(parent);
        throw new UnsupportedOperationException("Not supported yet.");
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

    public void setFileFilters(FileFilter[] filters, FileFilter fallbackFilter) {
        if (filters.length == 0 && fallbackFilter != null) {
            delegate.setFileFilter(fallbackFilter);
        } else {
            for (int i = 0; i < filters.length; i++) {
                delegate.addChoosableFileFilter(filters[i]);
            }
        }
    }
}
