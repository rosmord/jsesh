package jsesh.jhotdraw;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import jsesh.editor.JMDCEditor;
import jsesh.editor.MDCEditorKeyManager;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.file.MDCDocument;
import jsesh.mdc.file.MDCDocumentReader;
import jsesh.swing.utils.MdcFileDialog;

import org.jhotdraw.app.AbstractView;
import org.jhotdraw.app.action.edit.CopyAction;
import org.jhotdraw.app.action.edit.CutAction;
import org.jhotdraw.app.action.edit.PasteAction;
import org.jhotdraw.app.action.edit.RedoAction;
import org.jhotdraw.app.action.edit.UndoAction;
import org.jhotdraw.gui.URIChooser;
import org.qenherkhopeshef.utils.PlatformDetection;

public class JSeshView extends AbstractView {

	private JSeshViewModel viewModel;

	public JSeshView() {
	}

	@Override
	public void init() {
		viewModel= new JSeshViewModel();
		setLayout(new BorderLayout());
		add(new JScrollPane(viewModel.getEditor()), BorderLayout.CENTER);
		getEditor().getHieroglyphicTextModel().addObserver(new MyObserver());
		initActions();
	}

	public JMDCEditor getEditor() {
		return viewModel.getEditor();
	}
	
	private void initActions() {
		// Link between jhotdraw action names conventions and JSesh's
		getActionMap().put(UndoAction.ID,
				getEditor().getActionMap().get(MDCEditorKeyManager.UNDO));
		getActionMap().put(RedoAction.ID,
				getEditor().getActionMap().get(MDCEditorKeyManager.REDO));
		getActionMap().put(CopyAction.ID,
				getEditor().getActionMap().get(MDCEditorKeyManager.COPY));
		getActionMap().put(CutAction.ID,
				getEditor().getActionMap().get(MDCEditorKeyManager.CUT));
		getActionMap().put(PasteAction.ID,
				getEditor().getActionMap().get(MDCEditorKeyManager.PASTE));

	}

	public void clear() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					getEditor().clearText();
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void read(URI uri, URIChooser chooser) throws IOException {

		if (uri != null) {
			File file = new File(uri);
			
			try {
				MDCDocumentReader mdcDocumentReader = new MDCDocumentReader();
				// mdcDocumentReader.setEncoding(encoding);
				viewModel.setCurrentDocument(mdcDocumentReader.loadFile(file));
			} catch (MDCSyntaxError e) {
				String msg = "error at line " + e.getLine();
				msg += " near token: " + e.getToken();
				JOptionPane.showMessageDialog(getParent(), msg, "Syntax Error",
						JOptionPane.ERROR_MESSAGE);
				System.out.println(e.getCharPos());
				// e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	


	public void write(URI uri, URIChooser chooser) throws IOException {
		// TODO Auto-generated method stub

	}

	private class MyObserver implements Observer {

		public void update(Observable o, Object arg) {
			setHasUnsavedChanges(!getEditor().getHieroglyphicTextModel().isClean());
		}
	}

	public void insertCode(String code) {
		getEditor().getWorkflow().addSign(code);
	}

}
