/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qenherkhopeshef.swingUtils.portableFileDialog;

import java.awt.Component;
import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * A file dialog for Mac OS X, with java &gt; 1.7 (due to Oracle lack of support
 * for standard Apple Java extensions).
 *
 * @author rosmord
 */
class MacPortableFileDialog extends PortableFileDialog {

    private File file;
    private File directory;
    private FileFilter[] filters;
    private String title;
    private Component parent;

    public MacPortableFileDialog(Component parent) {
        this.parent= parent;
    }

    
    @Override
    public FileOperationResult show() {
        PortableFileDialog proxy;
     
        if (operation == FileOperation.OPEN_DIRECTORY || operation == FileOperation.SAVE_DIRECTORY) {
            proxy = new SwingFileDialog(parent);
        } else {
            proxy = new AwtPortableFileDialog(parent);
        }
        proxy.setCurrentDirectory(directory);
        if (filters != null && filters.length > 0)
            proxy.setFileFilters(filters);
        proxy.setSelectedFile(file);
        proxy.setTitle(title);
        proxy.setOperation(operation);
        FileOperationResult result = proxy.show();
        directory= proxy.getCurrentDirectory();
        file= proxy.getSelectedFile();
        proxy.dispose();
        return result;
    }

    @Override
    public void setSelectedFile(File file) {
        this.file = file;
    }

    @Override
    public void setCurrentDirectory(File directory) {
        this.directory = directory;
    }

    @Override
    public File getSelectedFile() {
        return file;
    }

    @Override
    public File getCurrentDirectory() {
        return directory;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void setFileFilters(FileFilter[] filters) {
        this.filters = filters;
    }

    @Override
    public void setTitle(String string) {
        this.title = string;
    }

    @Override
    public void setParent(Component parent) {
        this.parent = parent;
    }
}
