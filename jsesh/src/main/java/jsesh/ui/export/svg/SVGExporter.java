package jsesh.ui.export.svg;

import jsesh.ui.export.generic.ExportData;
import jsesh.ui.export.generic.AbstractGraphicalExporter;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import jsesh.ui.export.generic.SelectionExporter;
import jsesh.utils.swing.FileSaveConfirmDialog;
import jsesh.resources.JSeshMessages;

import org.qenherkhopeshef.graphics.svg.SVGGraphics2D;

/**
 * Export SVG files.
 */
public class SVGExporter extends AbstractGraphicalExporter {

    private File exportFile;

    private Component frame;

    private Dimension2D scaledDimension;

    public SVGExporter(final Component frame) {
        super("svg", JSeshMessages.getString("SVGExporter.description"));
        this.frame = frame;
    }

    @Override
    public void export(ExportData exportData) {
        try {
            SelectionExporter selectionExporter = new SelectionExporter(
                    exportData, this);
            selectionExporter.setClearBeforeDrawing(false);
            selectionExporter.exportSelection();
        } catch (HeadlessException e1) {
            throw new RuntimeException(e1);
        } catch (IOException e1) {
            FileSaveConfirmDialog.showCantOpenDialog(frame);
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see jsesh.ui.editorSoftware.PagedExporter#getOptionsTitle()
     */
    protected String getOptionsTitle() {
        return "type".toUpperCase(Locale.ENGLISH) + " options";
    }

    @Override
    public void setDimension(Dimension2D scaledDimensions) {
        this.scaledDimension = scaledDimensions;
    }

    @Override
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
