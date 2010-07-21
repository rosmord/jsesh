package jsesh.swing.preferencesEditor;

import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;



/**
 * An editor for JSesh preferences.
 * 
 * @author rosmord
 *
 */
public class PreferencesEditor {
	
	private Component parent;
	
	private FormPresenter[] formPresenters;
	
	private JTabbedPane panel;
	/*
	private FontChooserForm fontChooserPanel;
	private RTFPreferencesForm rtfPreferences1, rtfPreferences2;
	private DrawingSpecificationsForm jdrawingSpecificationEditor;
	*/
	
	public PreferencesEditor(Component parent,PreferencesFacade facade) {
		this.parent= parent;
		panel= new JTabbedPane();
		
		formPresenters= new FormPresenter[]  {
				new FontChooserPresenter("Fonts selection"),
				new RTFPreferencesPresenter("Export Preferences (large size)", facade.getRTFPreference(0)),
				new RTFPreferencesPresenter("Export Preferences (small size)", facade.getRTFPreference(1)),
				new DrawingSpecificationsPresenter("Drawing preferences"),
				new ClipboardChooserPresenter("Clipboard formats")
		};
		
		for (int i= 0; i < formPresenters.length; i++) {
			FormPresenter p= formPresenters[i];
			panel.addTab(p.getTitle(), p.getPanel());
		}
	}
	
	
	public JTabbedPane getPanel() {
		return panel;
	}
	
	
	public  void initializePreferences(PreferencesFacade facade) {
		for (int i= 0; i < formPresenters.length; i++) {
			formPresenters[i].loadPreferences(facade);
		}
	}
	
	/**
	 * 
	 * @return JOptionPanel.OK_OPTION if validated.
	 */
	public int askForPreferences() {
		int option = JOptionPane.showConfirmDialog(parent, getPanel(),
				"Edit preferences", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		return option;
	}
	
	public void updatePreferences(PreferencesFacade facade) {
		for (int i=0; i< formPresenters.length; i++)
			formPresenters[i].updatePreferences(facade);
	}
	
	
}
