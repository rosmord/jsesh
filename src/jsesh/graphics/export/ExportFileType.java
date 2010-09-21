/*
 * Created on 28 juin 2005 by rosmord
 *
 * TODO document the file ExportFileType.java
 * 
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package jsesh.graphics.export;

import java.io.File;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

/**
 * A class to encapsulate the various characteristics of exported graphical formats.
 * Very much inspired by the class of the same name in the freehep library. 
 * @author rosmord
 *
 * TODO document the type ExportFileType
 */
public interface ExportFileType {

    /**
     * Create an option panel, which should be initialized with values from properties.
     * @param properties
     * @return a jpanel
     */
    JPanel createOptionPanel(Properties properties);

    /**
     * Modify the properties according to the content of panel.
     * @param panel
     * @param properties
     */
    void applyChangedOptions(JPanel panel, Properties properties);

    /**
     * @return a file filter
     */
    FileFilter getFileFilter();

    /**
     * @param outputFile
     * @return true if the extension is valid.
     */
    boolean fileHasValidExtension(File outputFile);
    void adjustFilename(File outputFile, Properties properties);

}
