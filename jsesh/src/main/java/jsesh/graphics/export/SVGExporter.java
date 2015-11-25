package jsesh.graphics.export;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.IOException;

import jsesh.i18n.I18n;
import jsesh.swing.utils.FileSaveConfirmDialog;

import org.qenherkhopeshef.graphics.svg.SVGGraphics2D;

/**
 * Export SVG files.
 */
public class SVGExporter extends AbstractGraphicalExporter {

    private File exportFile;

    private Component frame;

    private Dimension2D scaledDimension;

    public SVGExporter() {
        super("svg", I18n.getString("SVGExporter.description"));
        frame = null;
    }

    public void export(ExportData exportData) {
        try {
            SelectionExporter selectionExporter = new SelectionExporter(
                    exportData, this);
            selectionExporter.setClearBeforeDrawing(false);
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

    public void setDimension(Dimension2D scaledDimensions) {
        this.scaledDimension = scaledDimensions;
    }

    public Graphics2D buildGraphics()
            throws IOException {
        return new SVGGraphics2D(getExportFile(), scaledDimension);
    }

    public void writeGraphics() throws IOException {
        // NO OP.
    }

    @Override
    public void newPage() throws IOException {
        // NO OP.
    }

}
