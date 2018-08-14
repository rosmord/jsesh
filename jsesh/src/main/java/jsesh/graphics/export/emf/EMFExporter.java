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
 /*
 * Created on 4 nov. 2004
 */
package jsesh.graphics.export.emf;

import jsesh.graphics.export.generic.ExportData;
import jsesh.graphics.export.generic.AbstractGraphicalExporter;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.geom.Dimension2D;
import java.io.IOException;
import jsesh.graphics.export.generic.SelectionExporter;

import jsesh.i18n.I18n;
import jsesh.swing.utils.FileSaveConfirmDialog;

import org.qenherkhopeshef.graphics.emf.EMFGraphics2D;

/**
 * Expert able to export the selection to an EMF file.
 *
 * @author S. Rosmorduc
 *
 */
public class EMFExporter extends AbstractGraphicalExporter {

    private final Component parent;

    private Dimension2D scaledDimension;

    private String comment = "";

    public EMFExporter(Component parent) {
        super("emf", I18n.getString("EMFExporter.description"));
        this.parent = parent;
    }

    @Override
    public void export(ExportData exportData) {
        try {
            comment = exportData.getExportedMdC();
            SelectionExporter selectionExporter = new SelectionExporter(
                    exportData, this);
            selectionExporter.exportSelection();
        } catch (HeadlessException e1) {
            throw new RuntimeException(e1);
        } catch (IOException e1) {
            FileSaveConfirmDialog.showCantOpenDialog(parent);
        }
    }

    protected String getOptionsTitle() {
        return "type".toUpperCase() + " options";
    }

    @Override
    public void setDimension(Dimension2D scaledDimensions) {
        this.scaledDimension = scaledDimensions;
    }

    @Override
    public Graphics2D buildGraphics() throws IOException {
        return new EMFGraphics2D(getExportFile(), scaledDimension, "JSesh", comment);
    }

    @Override
    public void writeGraphics() throws IOException {
        // NO-OP. the file is written as it is created.
    }

    @Override
    public void newPage() throws IOException {
        // NO-OP.
    }

}
