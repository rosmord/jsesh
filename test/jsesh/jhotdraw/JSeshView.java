package jsesh.jhotdraw;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URI;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import jsesh.editor.JMDCEditor;
import jsesh.editor.MDCEditorKeyManager;

import org.jhotdraw.app.AbstractView;
import org.jhotdraw.app.action.edit.CopyAction;
import org.jhotdraw.app.action.edit.CutAction;
import org.jhotdraw.app.action.edit.PasteAction;
import org.jhotdraw.app.action.edit.RedoAction;
import org.jhotdraw.app.action.edit.UndoAction;
import org.jhotdraw.gui.URIChooser;

public class JSeshView extends AbstractView {

	private JMDCEditor editor;

	public JSeshView() {

	}

	@Override
	public void init() {
		setLayout(new BorderLayout());
		editor = new JMDCEditor();
		add(new JScrollPane(editor), BorderLayout.CENTER);
		editor.getHieroglyphicTextModel().addObserver(new MyObserver());
		initActions();
	}

	private void initActions() {
		// Link between jhotdraw action names conventions and JSesh's
		getActionMap().put(UndoAction.ID, editor.getActionMap().get(MDCEditorKeyManager.UNDO));
		getActionMap().put(RedoAction.ID, editor.getActionMap().get(MDCEditorKeyManager.REDO));
		getActionMap().put(CopyAction.ID, editor.getActionMap().get(MDCEditorKeyManager.COPY));
		getActionMap().put(CutAction.ID, editor.getActionMap().get(MDCEditorKeyManager.CUT));
		getActionMap().put(PasteAction.ID, editor.getActionMap().get(MDCEditorKeyManager.PASTE));

	}

	public void clear() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					editor.clearText();
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void read(URI uri, URIChooser chooser) throws IOException {
		// TODO Auto-generated method stub

	}

	public void write(URI uri, URIChooser chooser) throws IOException {
		// TODO Auto-generated method stub

	}

	private class MyObserver implements Observer {

		public void update(Observable o, Object arg) {
			setHasUnsavedChanges(!editor.getHieroglyphicTextModel().isClean());
		}
	}

	public void insertCode(String code) {
		editor.getWorkflow().addSign(code);
	}

}
