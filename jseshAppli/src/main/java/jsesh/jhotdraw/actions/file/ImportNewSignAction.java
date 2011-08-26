package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JOptionPane;

import jsesh.graphics.glyphs.ExternalSignImporter;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.actions.BundleHelper;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.action.AbstractApplicationAction;

/**
 * Add a new sign in the user's own hieroglyphic folder.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 *
 */
@SuppressWarnings("serial")
public class ImportNewSignAction extends AbstractApplicationAction {
	public static final String ID = "file.importNewSign";

	public ImportNewSignAction(Application app) {
		super(app);
		BundleHelper.getInstance().configure(this);
	}
	
	public void actionPerformed(ActionEvent e) {
		JSeshApplicationModel jSeshApplicationModel= (JSeshApplicationModel) getApplication().getModel();
		ExternalSignImporter importer = new ExternalSignImporter();
		File currentHieroglyphsSource= jSeshApplicationModel.getCurrentDirectory();
		importer.setSourceDirectory(currentHieroglyphsSource);
		JOptionPane.showMessageDialog(getApplication().getComponent(), importer.getPanel(),
				"Import new signs", JOptionPane.PLAIN_MESSAGE);
		jSeshApplicationModel.setCurrentDirectory(importer.getSourceDirectory());
	}

}
