/*
 * This file is distributed under the LGPL
 * Author: Serge Rosmorduc
 */

package org.qenherkhopeshef.swingUtils;

/**
 * A PortableFileDialog using AWT File Dialog.
 * Interesting for use on Mac OS and, in some cases, on Windows XP.
 * @author rosmord
 */
public class AwtPortableFileDialog {
    // Implementation notes :
    // For Mac, System.setProperty("apple.awt.fileDialogForDirectories", "true");
    // Allows to create a directory oriented file dialog. It must be set to false
    // after uses.


    // The AWT file dialog is not suitable for uses with windows, as file name filters
    // don't work.

    // FileDialog are created with:
    // a parent window
    // a title
    // a mode which is either LOAD or SAVE

    // The working directory can be set (as a String)
    // and the current file too.
    // a FileNameFilter can be installed. it has a method accept() which
    // takes a dir and a filename as parameters. The dir is a File
    
}
