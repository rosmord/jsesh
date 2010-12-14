package jsesh.jhotdraw;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import jsesh.editor.JMDCEditor;
import jsesh.editor.MDCEditorKeyManager;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.file.MDCDocumentReader;

import org.jhotdraw_7_4_1.app.AbstractView;
import org.jhotdraw_7_4_1.app.action.edit.CopyAction;
import org.jhotdraw_7_4_1.app.action.edit.CutAction;
import org.jhotdraw_7_4_1.app.action.edit.PasteAction;
import org.jhotdraw_7_4_1.app.action.edit.RedoAction;
import org.jhotdraw_7_4_1.app.action.edit.UndoAction;
import org.jhotdraw_7_4_1.gui.URIChooser;
import org.qenherkhopeshef.swingUtils.errorHandler.UserMessage;

public class JSeshView extends AbstractView {

	private JSeshViewModel viewModel;

	public JSeshView() {
	}

	@Override
	public void init() {
		setFocusable(false); // Focus should go to the editor, not to the JSeshView panel itself !
		viewModel= new JSeshViewModel();
		setLayout(new BorderLayout());
		add(new JScrollPane(viewModel.getEditor()), BorderLayout.CENTER);
		observeChanges();
		initActions();
	}

	/**
	 * Sets an observer to track document changes.
	 * As the observer is linked to the document, closing the document will free the observer too.
	 */
	private void observeChanges() {
		getEditor().getHieroglyphicTextModel().addObserver(new MyObserver());
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

	/**
	 * Read a new JSesh document.
	 * <p> Note to self: we should arrange viewModel.setCurrentDocument 
	 * so that it's clear which part is performed as a background operation and which part is 
	 * performed in the ED thread.
	 * <p> We should also block the input, something which is not done by jhotdraw.
	 */
	public void read(URI uri, URIChooser chooser) throws IOException {

		if (uri != null) {
			File file = new File(uri);
			
			try {
				MDCDocumentReader mdcDocumentReader = new MDCDocumentReader();
				// mdcDocumentReader.setEncoding(encoding);
				
				viewModel.setCurrentDocument(mdcDocumentReader.loadFile(file));
				SwingUtilities.invokeAndWait(new Runnable() {					
					public void run() {
						observeChanges();						
					}
				});
			} catch (MDCSyntaxError e) {
				String msg = "error at line " + e.getLine();
				msg += " near token: " + e.getToken();
				JOptionPane.showMessageDialog(getParent(), msg, "Syntax Error",
						JOptionPane.ERROR_MESSAGE);
				System.out.println(e.getCharPos());
				// e.printStackTrace();
			} catch (IOException e) {
				throw new UserMessage(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
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

	@Override
	public void setEnabled(boolean enabled) {
		viewModel.setEnabled(enabled);
		super.setEnabled(enabled);
	}
}
