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
package jsesh.jhotdraw.applicationPreferences.ui;

import jsesh.graphics.export.rtf.RTFExportPreferences;
import jsesh.graphics.export.rtf.RTFExportGranularity;
import jsesh.graphics.export.rtf.RTFExportGraphicFormat;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.Messages;
import jsesh.jhotdraw.applicationPreferences.model.ExportPreferences;
import jsesh.jhotdraw.utils.PanelBuilder;
import jsesh.swing.units.LengthUnit;
import jsesh.swing.units.UnitMediator;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Arrays;

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
    private JFormattedTextField quadrantHeightLargeField;
    private JFormattedTextField quadrantHeightSmallField;
    private JFormattedTextField quadrantHeightFileField;
    private JFormattedTextField quadrantHeightWysiwygField;
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
                quadrantHeightFileField,
                quadrantHeightSmallField,
                quadrantHeightLargeField,
                quadrantHeightWysiwygField                
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

        quadrantHeightSmallField.setValue(exportPreferences
                .getquadrantHeightSmall());
        quadrantHeightLargeField.setValue(exportPreferences
                .getquadrantHeightLarge());
        quadrantHeightFileField.setValue(exportPreferences
                .getquadrantHeightFile());
        quadrantHeightWysiwygField.setValue(exportPreferences
                .getquadrantHeightWysiwyg());
    }

    private void init() {
        panel = new JPanel();
        exportModeCB = new JComboBox();
        unitCB = new JComboBox();
        graphicFormatCB = new JComboBox();
        graphicFormatCB.setModel(new DefaultComboBoxModel(
                RTFExportGraphicFormat.GRAPHIC_FORMATS));

        respectTextLayoutCB = new JCheckBox(
                Messages.getString("exportPrefs.respectTextLayout"));
        quadrantHeightLargeField = new JFormattedTextField(new Double(20));
        quadrantHeightSmallField = new JFormattedTextField(new Double(20));
        quadrantHeightFileField = new JFormattedTextField(new Double(20));
        quadrantHeightWysiwygField = new JFormattedTextField(new Double(20));

    }

    private void layout() {
        panel.setLayout(new MigLayout());
        PanelBuilder helper = new PanelBuilder(panel);
        helper.addLabel("exportPrefs.exportMode");
        panel.add(exportModeCB, "span, grow, wrap");
        helper.addLabel("exportPrefs.quadrantHeightLarge");
        panel.add(quadrantHeightLargeField, "w 40pt, sg a");
        panel.add(unitCB, "span, grow, wrap");
        helper.addLabel("exportPrefs.quadrantHeightSmall");
        panel.add(quadrantHeightSmallField, "sg a, wrap");
        helper.addLabel("exportPrefs.quadrantHeightWysiwyg");
        panel.add(quadrantHeightWysiwygField, "sg a, wrap");
        helper.addLabel("exportPrefs.quadrantHeightFile");
        panel.add(quadrantHeightFileField, "sg a,wrap");
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
        
        newPreferences.setQuadrantHeightSmall(lengthFromField(quadrantHeightSmallField));
        newPreferences.setQuadrantHeightLarge(lengthFromField(quadrantHeightLargeField));
        newPreferences.setQuadrantHeightFile(lengthFromField(quadrantHeightFileField));
        newPreferences.setQuadrantHeightWysiwyg(lengthFromField(quadrantHeightWysiwygField));
        app.setExportPreferences(newPreferences);
    }
}
