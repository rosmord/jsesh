package jsesh.graphics.export;

import java.awt.Component;
import java.io.File;
import java.net.URI;
import org.qenherkhopeshef.swingUtils.portableFileDialog.FileOperationResult;

public interface GraphicalExporter extends BaseGraphics2DFactory {

    /**
     * Ask the user for a file name, and keep it for later use.
     *
     * @param frame the parent component for this dialog
     * @return OK or CANCEL
     */
    FileOperationResult askUser(Component frame);

    void export(ExportData exportData);

    /**
     * Sets the original document file, which will be used to automatically
     * propose a file name.
     *
     * @param uri
     */
    void setOriginalDocumentFile(URI uri);

    /**
     * Sets the directory where the picture will be saved.
     *
     * @param currentDirectory
     */
    void setDirectory(File currentDirectory);

    /**
     * Gets the directory (usually after calling askuser).
     *
     * @return
     */
    File getDirectory();

}
