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
package jsesh.jhotdraw.preferences.application.ui;

import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

import jsesh.graphics.export.rtf.RTFExportGranularity;
import jsesh.graphics.export.rtf.RTFExportGraphicFormat;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.preferences.application.model.ExportPreferences;
import jsesh.jhotdraw.utils.PanelBuilder;
import jsesh.resources.JSeshMessages;
import jsesh.swing.units.LengthUnit;
import jsesh.swing.units.UnitMediator;
import net.miginfocom.swing.MigLayout;

/**
 * Panel for export preferences. The graphical part should be separated from the
 * rest.
 *
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
public class JExportPreferences {

    private ExportPreferences exportPreferences = new ExportPreferences();

    private JPanel panel;
    private JComboBox exportModeCB;
    private JComboBox unitCB;
    private JComboBox graphicFormatCB;
    private JFormattedTextField quadratHeightLargeField;
    private JFormattedTextField quadratHeightSmallField;
    private JFormattedTextField quadratHeightFileField;
    private JFormattedTextField quadratHeightWysiwygField;
    private JCheckBox respectTextLayoutCB;

    private UnitMediator unitMediator = new UnitMediator();

    public JExportPreferences() {
        init();
        layout();
        animate();
    }

    /**
     * Sets the control part of the dialog.
     */
    private void animate() {
        unitMediator.attachToComboBox(unitCB);
        for (JFormattedTextField f: Arrays.asList(
                quadratHeightFileField,
                quadratHeightSmallField,
                quadratHeightLargeField,
                quadratHeightWysiwygField                
                )) {
            unitMediator.managedTextField(f);
        }
        exportModeCB.setModel(new DefaultComboBoxModel(
                RTFExportGranularity.GRANULARITIES));
        exportModeCB.setSelectedItem(RTFExportGranularity.GROUPED_CADRATS);
        setExportPreferences(exportPreferences);
    }

    /**
     * Update user interface from data.
     * @param exportPreferences 
     */
    public void setExportPreferences(ExportPreferences exportPreferences) {
        this.exportPreferences = exportPreferences;
        // Reset unit (would be nicer if we had a model for this. 
        // not that simple in Swing.)
        unitMediator.setCurrentUnit(LengthUnit.POINT);
        exportModeCB.setSelectedItem(exportPreferences.getGranularity());
        graphicFormatCB.setSelectedItem(exportPreferences.getGraphicFormat());
        respectTextLayoutCB.setSelected(exportPreferences
                .isTextLayoutRespected());

        quadratHeightSmallField.setValue(exportPreferences
                .getquadratHeightSmall());
        quadratHeightLargeField.setValue(exportPreferences
                .getquadratHeightLarge());
        quadratHeightFileField.setValue(exportPreferences
                .getquadratHeightFile());
        quadratHeightWysiwygField.setValue(exportPreferences
                .getquadratHeightWysiwyg());
    }

    private void init() {
        panel = new JPanel();
        exportModeCB = new JComboBox();
        unitCB = new JComboBox();
        graphicFormatCB = new JComboBox();
        graphicFormatCB.setModel(new DefaultComboBoxModel(
                RTFExportGraphicFormat.GRAPHIC_FORMATS));

        respectTextLayoutCB = new JCheckBox(
                JSeshMessages.getString("exportPrefs.respectTextLayout"));
        quadratHeightLargeField = new JFormattedTextField(20d);
        quadratHeightSmallField = new JFormattedTextField(20d);
        quadratHeightFileField = new JFormattedTextField(20d);
        quadratHeightWysiwygField = new JFormattedTextField(20d);

    }

    private void layout() {
        panel.setLayout(new MigLayout());
        PanelBuilder helper = new PanelBuilder(panel);
        helper.addLabel("exportPrefs.exportMode");
        panel.add(exportModeCB, "span, grow, wrap");
        helper.addLabel("exportPrefs.quadratHeightLarge");
        panel.add(quadratHeightLargeField, "w 40pt, sg a");
        panel.add(unitCB, "span, grow, wrap");
        helper.addLabel("exportPrefs.quadratHeightSmall");
        panel.add(quadratHeightSmallField, "sg a, wrap");
        helper.addLabel("exportPrefs.quadratHeightWysiwyg");
        panel.add(quadratHeightWysiwygField, "sg a, wrap");
        helper.addLabel("exportPrefs.quadratHeightFile");
        panel.add(quadratHeightFileField, "sg a,wrap");
        helper.addLabel("exportPrefs.graphicFormat");
        panel.add(graphicFormatCB, "span, grow, wrap");
        panel.add(respectTextLayoutCB, "span,grow, wrap");
    }

    public JPanel getPanel() {
        return panel;
    }

    public void loadPreferences(JSeshApplicationModel app) {
        setExportPreferences(app.getExportPreferences());
    }

    private double lengthFromField(JFormattedTextField field) {
        return unitMediator.getManagedFieldInPoints(field);
    }
    
    public void updatePreferences(JSeshApplicationModel app) {
        ExportPreferences newPreferences = new ExportPreferences();
        newPreferences.setRTFExportGranularity((RTFExportGranularity) this.exportModeCB.getSelectedItem());
        newPreferences.setRTFExportGraphicFormat((RTFExportGraphicFormat) this.graphicFormatCB.getSelectedItem());
        newPreferences.setTextLayoutRespected(this.respectTextLayoutCB.isSelected());
        
        newPreferences.setQuadratHeightSmall(lengthFromField(quadratHeightSmallField));
        newPreferences.setQuadratHeightLarge(lengthFromField(quadratHeightLargeField));
        newPreferences.setQuadratHeightFile(lengthFromField(quadratHeightFileField));
        newPreferences.setQuadratHeightWysiwyg(lengthFromField(quadratHeightWysiwygField));
        app.setExportPreferences(newPreferences);
    }
}
