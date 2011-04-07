package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;

import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.actions.BundleHelper;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.View;
import org.jhotdraw_7_4_1.app.action.AbstractApplicationAction;
import org.jhotdraw_7_4_1.app.action.AbstractViewAction;
import org.jhotdraw_7_4_1.app.action.app.OpenApplicationAction;
import org.jhotdraw_7_4_1.app.action.file.OpenFileAction;

@SuppressWarnings("serial")
public class ImportPDFAction extends AbstractApplicationAction {
	public static final String ID= "file.import.pdf";

	public ImportPDFAction(Application app) {
		super(app);
		BundleHelper.configure(this);
	}


	public void actionPerformed(ActionEvent e) {
		JSeshApplicationModel applicationModel= (JSeshApplicationModel) getApplication().getModel();
new OpenFileAction(app);

//RamsesSyntaxViewModel model= (RamsesSyntaxViewModel) getActiveView();
		//if (model != null) {
			//model.groupElements();
		//}
	}

}
