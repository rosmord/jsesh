package jsesh.jhotdraw.preferences.ui;

import java.awt.Label;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jsesh.graphics.export.RTFExportPreferences;
import jsesh.graphics.export.RTFExportPreferences.RTFExportGranularity;
import jsesh.jhotdraw.Messages;
import jsesh.jhotdraw.preferences.model.ExportPreferences;
import jsesh.mdcDisplayer.swing.units.LengthUnit;
import jsesh.mdcDisplayer.swing.units.UnitMaintainter;
import net.miginfocom.swing.MigLayout;

/**
 * Panel for export preferences.
 * The graphical part could be moved.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
@SuppressWarnings("serial")
public class JExportPreferences {

	private ExportPreferences exportPreferences= new ExportPreferences();
	
	private LengthUnit unit= LengthUnit.POINT;

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
		exportModeCB.setModel(new DefaultComboBoxModel(RTFExportGranularity.GRANULARITIES));
		exportModeCB.setSelectedItem(RTFExportGranularity.GROUPED_CADRATS);
	
		setExportPreferences(exportPreferences);
	}

	public void setExportPreferences(ExportPreferences exportPreferences) {
		this.exportPreferences = exportPreferences;
	}
	
	private void init() {
		panel= new JPanel();
		exportModeCB= new JComboBox();
		unitCB= new JComboBox();
		graphicFormatCB= new JComboBox();
		graphicFormatCB.setModel(new DefaultComboBoxModel(RTFExportPreferences.graphicFormatList));

		respectTextLayoutCB= new JCheckBox(Messages.getString("exportPrefs.respectTextLayout"));
		quadrantHeightLargeField= new JFormattedTextField(new Double(20));
		quadrantHeightSmallField= new JFormattedTextField(new Double(20));
		quadrantHeightFileField= new JFormattedTextField(new Double(20));
		quadrantHeightWysiwygField= new JFormattedTextField(new Double(20));
		
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
	

	
}
