/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.preferencesEditor;

import javax.swing.JComponent;

/**
 * @author rosmord
 *
 */
public class DrawingSpecificationsPresenter extends FormPresenter {

	DrawingSpecificationsForm form;
	/**
	 * @param string
	 */
	public DrawingSpecificationsPresenter(String title) {
		super(title);
		form= new DrawingSpecificationsForm();
	}

	

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.swing.preferencesEditor.FormPresenter#getPanel()
	 */
	public JComponent getPanel() {
		return form;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.swing.preferencesEditor.FormPresenter#updatePreferences()
	 */
	public void updatePreferences(PreferencesFacade facade) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.swing.preferencesEditor.FormPresenter#loadPreferences(jsesh.mdcDisplayer.swing.preferencesEditor.PreferencesFacade)
	 */
	public void loadPreferences(PreferencesFacade facade) {
		// TODO Auto-generated method stub
		
	}

}
