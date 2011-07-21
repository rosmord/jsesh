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
package jsesh.jhotdraw.documentPreferences.ui;


import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import jsesh.jhotdraw.Messages;
import jsesh.jhotdraw.utils.PanelHelper;

import net.miginfocom.swing.MigLayout;

/**
 * A Graphical editor for drawing specifications.
 * 
 * @author rosmord
 */
public class JDrawingSpecificationEditor {

	private JPanel panel= new JPanel();

	/**
	 * Creates new form JDrawingSpecificationEditor
	 **/
	public JDrawingSpecificationEditor() {
		initComponents();
		layout();
	}
	
	/**
	 * Requests the focus in one of the fields when the dialog is opened. 
	 */
	public void addFocus() {
		panel.addAncestorListener(new AncestorListener() {			
			public void ancestorRemoved(AncestorEvent event) {}
			
			public void ancestorMoved(AncestorEvent event) {}
			
			public void ancestorAdded(AncestorEvent event) {
				normalSignHeightField.requestFocusInWindow();
			}
		});

	}

	public JPanel getPanel() {
		return panel;
	}

	private void initComponents() {
		useLinesForShadingCheckBox= new JCheckBox(Messages.getString("drawingPrefs.useLinesForShadingCheckBox"));
	}

	private void layout() {
		panel.setLayout(new MigLayout());
		PanelHelper h = new PanelHelper(panel);
		h.addWithLabel("drawingPrefs.a1SignHeight", normalSignHeightField, "w 40pt, sg a, gapright unrelated");
		h.addWithLabel("drawingPrefs.units", unitField, "wrap");
		h.addWithLabel("drawingPrefs.lineSkip", lineSkipField, "sg a, wrap");
		h.addWithLabel("drawingPrefs.columnSkip", columnSkipField, "sg a, wrap");
		h.addWithLabel("drawingPrefs.maximalQuadrantHeight", maxCadratHeightField, "sg a, wrap");
		h.addWithLabel("drawingPrefs.maximalQuadrantWidth", maxCadratWidthField, "sg a, wrap");
		h.addWithLabel("drawingPrefs.smallFontBodyLimit", smallFontBodyLimitField, "sg a, wrap");
		h.add(useLinesForShadingCheckBox, "wrap");

	}

	private JFormattedTextField cartoucheLineWidthField= new JFormattedTextField();
	private JFormattedTextField columnSkipField=new JFormattedTextField();
	private JFormattedTextField lineSkipField=new JFormattedTextField();
	private JFormattedTextField maxCadratHeightField=new JFormattedTextField();
	private JFormattedTextField maxCadratWidthField=new JFormattedTextField();
	private JFormattedTextField normalSignHeightField=new JFormattedTextField();
	private JFormattedTextField smallFontBodyLimitField=new JFormattedTextField();
	private JComboBox unitField= new JComboBox();
	private JCheckBox useLinesForShadingCheckBox;

	public JFormattedTextField getCartoucheLineWidthField() {
		return cartoucheLineWidthField;
	}

	public JFormattedTextField getColumnSkipField() {
		return columnSkipField;
	}

	public JFormattedTextField getLineSkipField() {
		return lineSkipField;
	}

	public JFormattedTextField getMaxCadratHeightField() {
		return maxCadratHeightField;
	}

	public JFormattedTextField getMaxCadratWidthField() {
		return maxCadratWidthField;
	}

	public JFormattedTextField getNormalSignHeightField() {
		return normalSignHeightField;
	}

	public JFormattedTextField getSmallFontBodyLimitField() {
		return smallFontBodyLimitField;
	}

	public JComboBox getUnitField() {
		return unitField;
	}

	public JCheckBox getUseLinesForShadingCheckBox() {
		return useLinesForShadingCheckBox;
	}

}
