package jsesh.jhotdraw.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.JOptionPane;

import jsesh.graphics.export.BitmapExporter;
import jsesh.graphics.export.ExportData;
import jsesh.jhotdraw.BundleHelper;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.View;
import org.jhotdraw_7_4_1.app.action.AbstractViewAction;

@SuppressWarnings("serial")
public class ExportAsBitmapAction extends AbstractViewAction {
	public static final String ID= "file.export.bitmap";

	public ExportAsBitmapAction(Application app, View view) {
		super(app, view);
		BundleHelper.configure(this);
	}

	public void actionPerformed(ActionEvent e) {
		View v = getActiveView();
		getApplication();
		if (v != null) {
		if (bitmapExporter == null) {
			bitmapExporter = new BitmapExporter(frame);
		}

		boolean selectionOnly = getCaret().hasSelection();
		if (bitmapExporter.askUser(selectionOnly) == JOptionPane.OK_OPTION) {
			if (selectionOnly) {
				ExportData exportData = new ExportData(
						getDrawingSpecifications(), getCaret(),
						getHieroglyphicTextModel().getModel(), 2f);
				bitmapExporter.export(exportData);
			} else {
				ExportData exportData = new ExportData(
						getDrawingSpecifications(), getCaret(),
						getHieroglyphicTextModel().getModel(), 2f);
				try {
					bitmapExporter.exportAll(exportData);
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}

		}
		}
	}

}
