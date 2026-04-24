package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JOptionPane;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;

import jsesh.graphics.export.html.HTMLExporter;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.documentview.JSeshViewCore;
import jsesh.jhotdraw.utils.AbstractCoreViewAction;

@SuppressWarnings("serial")
public class ExportAsHTMLAction extends AbstractCoreViewAction {

	public static final String ID = "file.exportHTML";

	public ExportAsHTMLAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configure(this);
	}

	public void actionPerformed(ActionEvent arg0) {
		viewCore().ifPresent(v -> exportAsHTML(v));
	}

	private void exportAsHTML(JSeshViewCore v) {
		HTMLExporter htmlExporter = appCore().getHTMLExporter();

		htmlExporter.setDirectory(new File(appCore()
				.getCurrentDirectory(), baseFileName()));
		htmlExporter.setBaseName(baseFileName());
		if (htmlExporter.getOptionPanel(getActiveView().getComponent(), "Export as HTML")
				.askAndSet() == JOptionPane.OK_OPTION) {
			htmlExporter.setJSeshStyle(v
					.getJSeshStyle());
			htmlExporter.exportModel(v.getTopItemList());
		}
	}

}
