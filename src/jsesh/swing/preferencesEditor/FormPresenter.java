/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.swing.preferencesEditor;


import javax.swing.JComponent;

/**
 * @author rosmord
 *
 */
public abstract class FormPresenter {

	/**
	 * Title for the associated panel.
	 */
	private String title;
	
	public FormPresenter(String title) {
		this.title= title;
	}
	
	/**
	 * @return
	 */
	public final String getTitle() {
		return title;
	}

	/**
	 * @return
	 */
	public abstract JComponent getPanel();

	/**
	 * Update the preference data, using the entered data.
	 * @param facade 
	 * 
	 */
	public abstract void updatePreferences(PreferencesFacade facade);

	/**
	 * Sets the displayed data, taken from facade.
	 * @param facade
	 */
	public abstract void loadPreferences(PreferencesFacade facade);

}
