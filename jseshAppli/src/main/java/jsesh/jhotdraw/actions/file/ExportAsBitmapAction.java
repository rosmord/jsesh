package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;

import jsesh.graphics.export.bitmaps.BitmapExporter;
import jsesh.graphics.export.generic.ExportData;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.documentview.JSeshViewCore;
import jsesh.jhotdraw.utils.AbstractCoreViewAction;

@SuppressWarnings("serial")
public class ExportAsBitmapAction extends AbstractCoreViewAction {
	public static final String ID = "file.export.bitmap";

	public ExportAsBitmapAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configure(this);
	}

	public void actionPerformed(ActionEvent e) {
		viewCore().ifPresent(v -> exportAsBitmap(v));
	}

	private void exportAsBitmap(JSeshViewCore v) {
		BitmapExporter bitmapExporter = new BitmapExporter(getActiveView()
				.getComponent());

		boolean selectionOnly = v.getCaret().hasSelection();
		if (bitmapExporter.askUser(selectionOnly) == JOptionPane.OK_OPTION) {
			ExportData exportData = v.createExportData(2f);
			if (selectionOnly) {
				bitmapExporter.export(exportData);
			} else {
				try {
					bitmapExporter.exportAll(exportData);
				} catch (IOException exception) {
					throw new RuntimeException(exception);
				}
			}
			bitmapExporter.savePreferences();
		}
	}

}
