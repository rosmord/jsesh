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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

import jsesh.graphics.export.RTFExportPreferences;
import jsesh.graphics.export.RTFExportPreferences.RTFExportGranularity;
import jsesh.graphics.export.RTFExportPreferences.RTFExportGraphicFormat;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.Messages;
import jsesh.jhotdraw.applicationPreferences.model.ExportPreferences;
import jsesh.jhotdraw.utils.PanelHelper;
import jsesh.mdcDisplayer.swing.units.LengthUnit;
import jsesh.mdcDisplayer.swing.units.UnitMaintainter;
import net.miginfocom.swing.MigLayout;

/**
 * Panel for export preferences. The graphical part should be separated from the
 * rest.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
public class JExportPreferences {

	private ExportPreferences exportPreferences = new ExportPreferences();

	private LengthUnit unit = LengthUnit.POINT;

	JPanel panel;
	JComboBox exportModeCB;
	JComboBox unitCB;
	JComboBox graphicFormatCB;
	JFormattedTextField quadrantHeightLargeField;
	JFormattedTextField quadrantHeightSmallField;
	JFormattedTextField quadrantHeightFileField;
	JFormattedTextField quadrantHeightWysiwygField;

	JCheckBox respectTextLayoutCB;

	public JExportPreferences() {
		init();
		layout();
		animate();
	}

	/**
	 * Sets the control part of the dialog.
	 */
	private void animate() {
		LengthUnit.attachToCombobox(unitCB, unit);
		UnitMaintainter.linkUnitsToValueField(unitCB, quadrantHeightSmallField);
		exportModeCB.setModel(new DefaultComboBoxModel(
				RTFExportGranularity.GRANULARITIES));
		exportModeCB.setSelectedItem(RTFExportGranularity.GROUPED_CADRATS);
		setExportPreferences(exportPreferences);
	}

	public void setExportPreferences(ExportPreferences exportPreferences) {
		this.exportPreferences = exportPreferences;
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
				RTFExportPreferences.graphicFormatList));

		respectTextLayoutCB = new JCheckBox(
				Messages.getString("exportPrefs.respectTextLayout"));
		quadrantHeightLargeField = new JFormattedTextField(new Double(20));
		quadrantHeightSmallField = new JFormattedTextField(new Double(20));
		quadrantHeightFileField = new JFormattedTextField(new Double(20));
		quadrantHeightWysiwygField = new JFormattedTextField(new Double(20));

	}

	private void layout() {
		panel.setLayout(new MigLayout());
		PanelHelper helper = new PanelHelper(panel);
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

	public void updatePreferences(JSeshApplicationModel app) {
		ExportPreferences exportPreferences = new ExportPreferences();		
		exportPreferences.setRTFExportGranularity((RTFExportGranularity) this.exportModeCB.getSelectedItem());
		exportPreferences.setRTFExportGraphicFormat((RTFExportGraphicFormat) this.graphicFormatCB.getSelectedItem());
		exportPreferences.setTextLayoutRespected(this.respectTextLayoutCB.isSelected());
		exportPreferences.setQuadrantHeightSmall(unit.getValueFromTextField(quadrantHeightSmallField));
		exportPreferences.setQuadrantHeightLarge(unit.getValueFromTextField(quadrantHeightLargeField));
		exportPreferences.setQuadrantHeightFile(unit.getValueFromTextField(quadrantHeightFileField));
		exportPreferences.setQuadrantHeightWysiwyg(unit.getValueFromTextField(quadrantHeightWysiwygField));		
		app.setExportPreferences(exportPreferences);
	}
}
