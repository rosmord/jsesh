package jsesh.jhotdraw.actions;

import java.awt.event.ActionEvent;

import jsesh.jhotdraw.BundleHelper;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.View;
import org.jhotdraw_7_4_1.app.action.AbstractViewAction;

@SuppressWarnings("serial")
public class ImportPDFAction extends AbstractViewAction {
	public static final String ID= "file.import.pdf";

	public ImportPDFAction(Application app, View view) {
		super(app, view);
		BundleHelper.configure(this);
	}

	public void actionPerformed(ActionEvent e) {
		
		//RamsesSyntaxViewModel model= (RamsesSyntaxViewModel) getActiveView();
		//if (model != null) {
			//model.groupElements();
		//}
	}

}
