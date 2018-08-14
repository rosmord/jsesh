/*
 * Created on 4 nov. 2004
 */
package jsesh.graphics.export.macpict;

import jsesh.graphics.export.generic.ExportData;
import jsesh.graphics.export.generic.AbstractGraphicalExporter;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import jsesh.graphics.export.generic.SelectionExporter;

import jsesh.i18n.I18n;
import jsesh.swing.utils.FileSaveConfirmDialog;

import org.qenherkhopeshef.graphics.pict.MacPictGraphics2D;

/**
 * Expert able to export the selection to an Mac Pict file.
 *
 * @author S. Rosmorduc
 *
 */
public class MacPictExporter extends AbstractGraphicalExporter {

    private Component frame;

	//private Dimension scaledDimension;
    private MacPictGraphics2D currentGraphics;

    public MacPictExporter() {
        super(new String[]{"pct", "pict"}, I18n.getString("MacPictExporter.description"));
        frame = null;
    }

    public void export(ExportData exportData) {
        try {
            SelectionExporter selectionExporter = new SelectionExporter(
                    exportData, this);
            selectionExporter.exportSelection();
        } catch (HeadlessException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            FileSaveConfirmDialog.showCantOpenDialog(frame);
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see jsesh.editorSoftware.PagedExporter#getOptionsTitle()
     */
    protected String getOptionsTitle() {
        return "type".toUpperCase() + " options";
    }

    public void setDimension(java.awt.geom.Dimension2D scaledDimensions) {
        //this.scaledDimension= scaledDimensions;
    }

    public Graphics2D buildGraphics()
            throws IOException {
        currentGraphics = new MacPictGraphics2D();
        return currentGraphics;
    }

    public void writeGraphics() throws IOException {
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(getExportFile()));
        currentGraphics.writeToStream(outputStream);
    }

    @Override
    public void newPage() throws IOException {
        // NO-OP.
    }

}
