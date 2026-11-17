package jsesh.mdcDisplayer.swing.preferencesEditor;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;


/**
 * An editor for JSesh preferences
 * @author rosmord
 *
 */
public class PreferencesEditor {
	private JTabbedPane panel;
	private FontChooserPanel fontChooserPanel;
	
	public PreferencesEditor() {
		panel= new JTabbedPane();
		fontChooserPanel= new FontChooserPanel();
		panel.addTab("Font selection", fontChooserPanel);		
	}
	
	
	public JTabbedPane getPanel() {
		return panel;
	}
	
	public FontChooserPanel getFontChooserPanel() {
		return fontChooserPanel;
	}
	
	
	
}
