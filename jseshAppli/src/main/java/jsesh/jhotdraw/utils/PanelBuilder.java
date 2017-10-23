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
package jsesh.jhotdraw.utils;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;

import jsesh.jhotdraw.Messages;

/**
 * Helps building a JPanel with labelled components. 
 * Uses jsesh.jhotdraw.labels as resource.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 *
 */
public class PanelBuilder {

	JPanel panel;

	public PanelBuilder(JPanel panel) {
		super();
		this.panel = panel;
	}

	/**
	 * Add a multilingual label.
	 * The label is defined in jsesh.jhotdraw.labels
	 * @param labelCode the code for the multilingual label.
	 */
	public void addLabel(String labelCode) {
		panel.add(new JLabel(Messages.getString(labelCode)));
	}

	public void add(Component component, String constraints) {
		panel.add(component, constraints);
	}

	public void add(Component component) {
		panel.add(component);
	}

	/**
	 * Add a component with a specific label.
	 * @param labelCode the code for the multilingual label.
	 * @param component
	 * @param constraints
	 */
	public void addWithLabel(String labelCode, Component component,
			String constraints) {
		addLabel(labelCode);
		add(component, constraints);
	}

	/**
	 * Add a component with a specific label.
	 * @param labelCode the code for the multilingual label.
	 * @param component
	 */
	public void addWithLabel(String labelCode, Component component) {
		addLabel(labelCode);
		add(component);
	}
}
