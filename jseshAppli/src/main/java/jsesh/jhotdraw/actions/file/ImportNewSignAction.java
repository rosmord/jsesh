package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JOptionPane;

import org.jhotdraw_7_6.app.Application;

import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.utils.AbstractCoreApplicationAction;
import jsesh.resources.JSeshMessages;
import jsesh.swing.signimportdialog.ExternalSignImporter;

/**
 * Add a new sign in the user's own hieroglyphic folder.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 * 
 */
@SuppressWarnings("serial")
public class ImportNewSignAction extends AbstractCoreApplicationAction {
	public static final String ID = "file.importNewSign";

	public ImportNewSignAction(Application app) {
		super(app);
		BundleHelper.getInstance().configure(this);
	}

	public void actionPerformed(ActionEvent e) {
		// Note the use of filter to check the folder is valid before processing it.
		appCore().getHieroglyphsFolder().filter(f -> f.isDirectory() && f.canWrite())
				.ifPresentOrElse(outputFolder -> {
					JSeshApplicationModel jSeshApplicationModel = (JSeshApplicationModel) getApplication()
							.getModel();
					ExternalSignImporter importer = appCore().externalSignImporter();
					File workingFolder = jSeshApplicationModel
							.getCurrentDirectory();
					importer.setSourceDirectory(workingFolder);
					JOptionPane.showMessageDialog(getApplication().getComponent(),
							importer.getPanel(), "Import new signs",
							JOptionPane.PLAIN_MESSAGE);
					jSeshApplicationModel.setCurrentDirectory(importer
							.getSourceDirectory());
				}, () -> {
					String message = JSeshMessages.getString("file.importNewSign.notfoundMessage");
					String messageTitle = JSeshMessages.getString("file.importNewSign.notfoundMessageTitle");

					JOptionPane.showMessageDialog(null, message, messageTitle, JOptionPane.WARNING_MESSAGE);
				});
	}
}
