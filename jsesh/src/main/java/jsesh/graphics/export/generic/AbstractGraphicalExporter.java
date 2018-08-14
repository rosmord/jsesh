/*
 Copyright Serge Rosmorduc
 contributor(s) : Serge J. P. Thomas for the fonts
 serge.rosmorduc@qenherkhopeshef.org

 This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

 This software is governed by the CeCILL license under French law and
 abiding by the rules of distribution of free software.  You can  use, 
 modify and/ or redistribute the software under the terms of the CeCILL
 license as circulated by CEA, CNRS and INRIA at the following URL
 "http://www.cecill.info". 

 As a counterpart to the access to the source code and  rights to copy,
 modify and redistribute granted by the license, users are provided only
 with a limited warranty  and the software's author,  the holder of the
 economic rights,  and the successive licensors  have only  limited
 liability. 

 In this respect, the user's attention is drawn to the risks associated
 with loading,  using,  modifying and/or developing or reproducing the
 software by the user in light of its specific status of free software,
 that may mean  that it is complicated to manipulate,  and  that  also
 therefore means  that it is reserved for developers  and  experienced
 professionals having in-depth computer knowledge. Users are therefore
 encouraged to load and test the software's suitability as regards their
 requirements in conditions enabling the security of their systems and/or 
 data to be ensured and,  more generally, to use and operate it in the 
 same conditions as regards security. 

 The fact that you are presently reading this means that you have had
 knowledge of the CeCILL license and that you accept its terms.
 */
package jsesh.graphics.export.generic;

import java.awt.Component;
import java.io.File;
import java.net.URI;

import javax.swing.filechooser.FileFilter;

import jsesh.i18n.I18n;
import jsesh.swing.utils.FileSaveConfirmDialog;
import org.qenherkhopeshef.swingUtils.portableFileDialog.FileOperationResult;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialog;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialogFactory;

/**
 * Simple base class for writing exporters.
 *
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 *
 */
public abstract class AbstractGraphicalExporter implements GraphicalExporter {

    private final class SpecificFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            String n = f.getName().toLowerCase();
            boolean hasCorrectSuffix = false;
            for (int i = 0; !hasCorrectSuffix && i < extensions.length; i++) {
                hasCorrectSuffix = n.endsWith("." + extensions[i]);
            }
            return hasCorrectSuffix || f.isDirectory();
        }

        @Override
        public String getDescription() {
            return description;
        }
    }

    protected String[] extensions;

    private String fileName;

    private File directory = new File("."); //$NON-NLS-1$

    /**
     * Description text displayed in the file choser for the specific format.
     */
    private String description;

    /**
     * Create an exporter
     *
     * @param extension the extension to use for the files.
     * @param description Description text displayed in the file choser for the
     * specific format
     */
    public AbstractGraphicalExporter(String extension, String description) {
        this(new String[]{extension}, description);
    }

    /**
     * Create an exporter
     *
     * @param extensions a list of possible extensions. The first one will be
     * used by default.
     * @param description Description text displayed in the file choser for the
     * specific format
     */
    public AbstractGraphicalExporter(String extensions[], String description) {
        this.extensions = extensions;
        this.description = description;
        fileName = I18n.getString("AbstractGraphicalExporter.unnamed") + "." + extensions[0]; //$NON-NLS-1$
    }

    public File getExportFile() {
        return new File(directory, fileName);
    }

    /**
     * Utility method to fix the file name.
     *
     * @param exportFile
     */
    public void setExportFile(File exportFile) {
        directory = exportFile.getParentFile();
        fileName = exportFile.getName();
    }

    @Override
    public File getDirectory() {
        return directory;
    }

    @Override
    public void setDirectory(File directory) {
        this.directory = directory;
    }

    @Override
    public void setOriginalDocumentFile(URI uri) {
        if (uri == null) {
            fileName = I18n.getString("AbstractGraphicalExporter.unnamed") + "." + extensions[0]; //$NON-NLS-1$
        } else if (uri.getScheme() == null || uri.getScheme().equals("file")) {
            fileName = new File(uri).getName();
            fileName = fileName.substring(0, fileName.lastIndexOf(".")) + "." + extensions[0];
        } else {
            fileName = uri.getScheme() + "." + extensions[0];
        }
    }

    @Override
    public FileOperationResult askUser(Component frame) {
        PortableFileDialog fileDialog = PortableFileDialogFactory.createFileSaveDialog(frame);
        fileDialog.setFileFilter(new SpecificFilter());
        fileDialog.setSelectedFile(getExportFile());
        FileOperationResult returnval = fileDialog.show();

        if (returnval == FileOperationResult.OK) {
            File chosenFile = fileDialog.getSelectedFile();
            if (! chosenFile.isDirectory()) {                
                // Fix the file ending ?
                boolean hasExtension = false;
                for (String ext : extensions) {
                    if (chosenFile.getName().toLowerCase().endsWith("." + ext)) {
                        hasExtension = true;
                        break;
                    }
                }
                if (!hasExtension) {
                    String name = chosenFile.getName();
                    name += "." + extensions[0];
                    chosenFile = new File(chosenFile.getParent(), name);
                }
                if (chosenFile.exists()) {
                    returnval = FileSaveConfirmDialog.showDialog(frame,
                            chosenFile);
                }

                if (returnval == FileOperationResult.OK) {
                    setExportFile(chosenFile);
                }
            }
        }
        return FileOperationResult.OK;
    }
}
