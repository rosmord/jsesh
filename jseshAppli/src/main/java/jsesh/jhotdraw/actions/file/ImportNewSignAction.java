package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JOptionPane;

import jsesh.graphics.glyphs.ExternalSignImporter;
import jsesh.hieroglyphs.graphics.DefaultHieroglyphicFontManager;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.Messages;
import jsesh.jhotdraw.actions.BundleHelper;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.action.AbstractApplicationAction;

/**
 * Add a new sign in the user's own hieroglyphic folder.
 * 
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
		File outputFolder = DefaultHieroglyphicFontManager.getInstance()
				.getDirectory();
		if (outputFolder != null && outputFolder.isDirectory()
				&& outputFolder.canWrite()) {
			JSeshApplicationModel jSeshApplicationModel = (JSeshApplicationModel) getApplication()
					.getModel();
			ExternalSignImporter importer = new ExternalSignImporter();
			File currentHieroglyphsSource = jSeshApplicationModel
					.getCurrentDirectory();
			importer.setSourceDirectory(currentHieroglyphsSource);
			JOptionPane.showMessageDialog(getApplication().getComponent(),
					importer.getPanel(), "Import new signs",
					JOptionPane.PLAIN_MESSAGE);
			jSeshApplicationModel.setCurrentDirectory(importer
					.getSourceDirectory());
		} else {
			String message= Messages.getString("file.importNewSign.notfoundMessage");
			String messageTitle= Messages.getString("file.importNewSign.notfoundMessageTitle");
			
			JOptionPane.showMessageDialog(null, message, messageTitle, JOptionPane.WARNING_MESSAGE);
		}
	}
}
