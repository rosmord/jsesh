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
package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import jsesh.jhotdraw.viewClass.JSeshView;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.documentPreferences.ui.DrawingSpecificationsPresenter;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;

@SuppressWarnings("serial")
public class EditDocumentPreferencesAction extends AbstractViewAction {

	public EditDocumentPreferencesAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configure(this);
	}

	public static final String ID = "file.documentProperties";

	public void actionPerformed(ActionEvent arg0) {
		JSeshView v = (JSeshView) getActiveView();
		if (v != null) {
			// TODO : ok, enought with current drawing specifications system...
			// we are going to use the old, simpler system with only a class...
			DrawingSpecification d = (DrawingSpecification) v
					.getDrawingSpecifications();
			DrawingSpecificationsPresenter presenter = new DrawingSpecificationsPresenter();
			presenter
					.loadPreferences(v.getDrawingSpecifications());
			if (presenter.showDialog(v) == JOptionPane.OK_OPTION) {
				presenter.updatePreferences(d);
				v.setDrawingSpecifications(d);
			}
		}
	}
}
