/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.jhotdraw.actions.edit;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import jsesh.jhotdraw.ExportType;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.actions.BundleHelper;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.View;
import org.jhotdraw_7_4_1.app.action.AbstractViewAction;

/**
 * Select one of the copy and paste configuration
 * 
 * @author rosmord
 * 
 */
@SuppressWarnings("serial")
public class SelectCopyPasteConfigurationAction extends AbstractViewAction {

	private static ExportType [] selectableTypes = {
		ExportType.SMALL, ExportType.LARGE, ExportType.WYSIWYG
	};
	
	private ExportType exportType;

	/**
	 * ID for resources. Add the configuration number to this ID.
	 */
	public static final String partialID = "edit.selectConfiguration_";

	public SelectCopyPasteConfigurationAction(Application app, View view,
			ExportType exportType) {
		super(app, view);
		this.exportType = exportType;
		BundleHelper.getInstance().configure(this, getID() );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		JSeshApplicationModel model = (JSeshApplicationModel) getApplication()
				.getModel();
		model.selectCopyPasteConfiguration(exportType);
	}

	public String getID() {
		return buildActionName(exportType);
	}
	
	private static String buildActionName(ExportType exportType) {
		return partialID + exportType.toString();		
	}
	public static SelectCopyPasteConfigurationAction[] buildActions(Application app, View view) {
		SelectCopyPasteConfigurationAction actions[] = new SelectCopyPasteConfigurationAction[selectableTypes.length];
		for (int i= 0; i < selectableTypes.length; i++)
			actions[i]= new SelectCopyPasteConfigurationAction(app, view, selectableTypes[i]);
		return actions;
	}
	
	public static String[] getSelectCopyPasteConfigurationActionNames() {
		String result[]= new String[selectableTypes.length];
		for (int i= 0; i <result.length; i++) {
			result[i]= buildActionName(selectableTypes[i]);
		}
		return result;
	}
	
}
