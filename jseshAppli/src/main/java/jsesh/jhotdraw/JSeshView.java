package jsesh.jhotdraw;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import jsesh.editor.ActionsID;
import jsesh.editor.JMDCEditor;
import jsesh.editor.MDCModelTransferableBroker;
import jsesh.editor.caret.MDCCaret;
import jsesh.graphics.export.pdfExport.PDFExportPreferences;
import jsesh.graphics.export.pdfExport.PDFExporter;
import jsesh.io.importer.pdf.PDFImportException;
import jsesh.io.importer.pdf.PDFImporter;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.file.MDCDocument;
import jsesh.mdc.file.MDCDocumentReader;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

import org.jhotdraw_7_4_1.app.AbstractView;
import org.jhotdraw_7_4_1.app.View;
import org.jhotdraw_7_4_1.app.action.edit.CopyAction;
import org.jhotdraw_7_4_1.app.action.edit.CutAction;
import org.jhotdraw_7_4_1.app.action.edit.PasteAction;
import org.jhotdraw_7_4_1.app.action.edit.RedoAction;
import org.jhotdraw_7_4_1.app.action.edit.UndoAction;
import org.jhotdraw_7_4_1.gui.URIChooser;
import org.qenherkhopeshef.swingUtils.errorHandler.UserMessage;

@SuppressWarnings("serial")
public class JSeshView extends AbstractView {

	private JSeshViewModel viewModel;

	public JSeshView() {
		viewModel = new JSeshViewModel();
	}

	@Override
	public void init() {
		setFocusable(false); // Focus should go to the editor, not to the
								// JSeshView panel itself !
		setLayout(new BorderLayout());
		add(new JScrollPane(viewModel.getEditor()), BorderLayout.CENTER);
		observeChanges();
		initActions();
	}

	/**
	 * Sets an observer to track document changes. As the observer is linked to
	 * the document, closing the document will free the observer too.
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
				getEditor().getActionMap().get(ActionsID.UNDO));
		getActionMap().put(RedoAction.ID,
				getEditor().getActionMap().get(ActionsID.REDO));
		getActionMap().put(CopyAction.ID,
				getEditor().getActionMap().get(ActionsID.COPY));
		getActionMap().put(CutAction.ID,
				getEditor().getActionMap().get(ActionsID.CUT));
		getActionMap().put(
				PasteAction.ID,
				getEditor().getActionMap().get(
						jsesh.editor.actions.edit.PasteAction.ID));
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
	 * <p>
	 * The uri can be either:
	 * <ul>
	 * <li>a normal URL (file:)
	 * <li>"clipboard:rtf", "clipboard:pdf" (later on, "clipboard:any" will be
	 * added).
	 * </ul>
	 * <p>
	 * Note to self: we should arrange viewModel.setCurrentDocument so that it's
	 * clear which part is performed as a background operation and which part is
	 * performed in the ED thread.
	 * <p>
	 * We should also block the input, something which is not done by jhotdraw
	 * (false. See examples).
	 * 
	 * Normally <em>Not</em> called on the EDT.
	 * 
	 * @see View#read(URI, URIChooser)
	 */
	public void read(URI uri, URIChooser chooser) throws IOException {
		if (uri != null) {
			if ("clipboard".equals(uri.getScheme())) {
				readFromClipboard(uri);
			} else {
				readFromFile(uri);
			}
		}
	}

	private void readFromClipboard(URI uri) {
		if (uri.getSchemeSpecificPart().equals("rtf")) {

		} else if (uri.getSchemeSpecificPart().equals("pdf")) {
			try {
				final MDCDocument document = (PDFImporter
						.createPDFPasteImporter(new File("Unnamed.gly"))
						.getMdcDocument());
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						viewModel.setCurrentDocument(document);
					}
				});
			} catch (PDFImportException e) {
				throw new UserMessage(e.getMessage());
			}
		}
	}

	/**
	 * Read a JSesh file in the current view.
	 * TODO : improve this code, which is not correct regarding the EDT.
	 * @param uri
	 */
	private void readFromFile(URI uri) {
		File file = new File(uri);
		/*
		 * // Two possibilities : PDF files or JSesh files... if
		 * (file.getName().toLowerCase().endsWith(".pdf")) { try {
		 * FileInputStream in = new FileInputStream(file);
		 * setCurrentDocument(PDFImporter.createPDFStreamImporter( in,
		 * file).getMdcDocument()); } catch (PDFImportException e) {
		 * e.printStackTrace(); JOptionPane.showMessageDialog(frame,
		 * "Error opening pdf. Sorry", "Error", JOptionPane.ERROR_MESSAGE); }
		 */
		try {
			MDCDocumentReader mdcDocumentReader = new MDCDocumentReader();
			// mdcDocumentReader.setEncoding(encoding);
			viewModel.setCurrentDocument(mdcDocumentReader.loadFile(file));
			// Observe changes to this document in the future.
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

	public void write(URI uri, URIChooser chooser) throws IOException {
		File file = new File(uri);
		MDCDocument document = viewModel.getMdcDocument();
		document.setFile(file);

		// TODO : create a sane system for dealing with text orientation and
		// direction.
		// Currently, the "document" data is not synchronized with the
		// content of the editor.
		// TODO TEMPORARY PATCH

		document.setMainDirection(getEditor().getDrawingSpecifications()
				.getTextDirection());
		document.setMainOrientation(getEditor().getDrawingSpecifications()
				.getTextOrientation());
		document.setSmallSignCentered(getEditor().getDrawingSpecifications()
				.isSmallSignsCentered());
		// TODO END OF TEMPORARY PATCH

		if (document.getFile() != null
				&& document.getFile().getName().toLowerCase().endsWith(".pdf")) {
			// Create the prefs for this document... move the code to document ?
			// or what ?
			// more info should also be saved in the case of PDF files (pdf
			// prefs).
			// TODO save PDF prefs in pdf files...
			PDFExportPreferences prefs = new PDFExportPreferences();
			prefs.setFile(document.getFile());
			prefs.setDrawingSpecifications(getDrawingSpecifications().copy());
			prefs.getDrawingSpecifications().setTextDirection(
					document.getMainDirection());
			prefs.getDrawingSpecifications().setTextOrientation(
					document.getMainOrientation());
			prefs.getDrawingSpecifications().setSmallSignsCentered(
					document.isSmallSignsCentred());
			PDFExporter exporter = new PDFExporter();
			exporter.setPdfExportPreferences(prefs);
			TopItemList model = document.getHieroglyphicTextModel().getModel();
			exporter.exportModel(model, MDCCaret.buildWholeTextCaret(model));
		} else {
			document.save();
		}
		// currentMDCDirectory = currentDocument.getFile().getParentFile();
	}

	private class MyObserver implements Observer {

		public void update(Observable o, Object arg) {
			setHasUnsavedChanges(!getEditor().getHieroglyphicTextModel()
					.isClean());
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

	/**
	 * Returns the current caret. TODO : we should simplify this... the inner
	 * code should not use so many layers.
	 * 
	 * @return a caret.
	 */
	public MDCCaret getCaret() {
		return viewModel.getEditor().getWorkflow().getCaret();
	}

	/**
	 * Returns the current drawing specifications. TODO : we should simplify
	 * this... the inner code should not use so many layers.
	 * 
	 * @return a caret.
	 */
	public DrawingSpecification getDrawingSpecifications() {
		return viewModel.getEditor().getDrawingSpecifications();
	}

	/**
	 * Returns the inner text representation. TODO : we should probably simplify
	 * this... or return the HieroglyphicTextModel. getModel() should probably
	 * be called getText().
	 * 
	 * @return
	 */
	public TopItemList getTopItemList() {
		return viewModel.getEditor().getWorkflow().getHieroglyphicTextModel()
				.getModel();
	}

	public void setDrawingSpecifications(
			DrawingSpecification drawingSpecifications) {
		viewModel.getEditor().setDrawingSpecifications(drawingSpecifications);
	}

	/**
	 * Sets the object which will be used to generate copy/paste information.
	 * This object must handle the current copy/paste selection.
	 * 
	 * @param jseshBase
	 */
	public void setMDCModelTransferableBroker(
			MDCModelTransferableBroker mdcModelTransferableBroker) {
		viewModel.getEditor().setMdcModelTransferableBroker(
				mdcModelTransferableBroker);
	}

	public void insertMDC(String mdcText) {
		viewModel.getEditor().getWorkflow().insertMDC(mdcText);
	}

}

// list of the methods used through getEditor().getWorkflow().
// workflow.getEditor().getWorkflow().changeAngle(angle);
// this.workflow.getEditor().getWorkflow().resizeSign(size);
// getEditor().getWorkflow().ligatureElements();
// 239: getEditor().getWorkflow().ligatureElements();
// 246: getEditor().getWorkflow().ligatureBefore();
// 253: getEditor().getWorkflow().ligatureAfter();
// 261: getEditor().getWorkflow().redZone(false);
// 268: getEditor().getWorkflow().redZone(true);
// 279: getEditor().getWorkflow().reverseSign();
// 291: getEditor().getWorkflow().setMode('b');
// 298: getEditor().getWorkflow().setMode('s');
// 305: getEditor().getWorkflow().setMode('i');
// 312: getEditor().getWorkflow().setMode('l');
// 319: getEditor().getWorkflow().setMode('t');
// 326: getEditor().getWorkflow().setMode('|');
// 336: getEditor().getWorkflow().shadeZone(true);
// 342: getEditor().getWorkflow().setSignIsInsideWord();
// 348: getEditor().getWorkflow().setSignIsAtSentenceEnd();
// 354: getEditor().getWorkflow().setSignIsAtWordEnd();
// 360: getEditor().getWorkflow().toggleGrammar();
// 366: getEditor().getWorkflow().toggleIgnoredSign();
// 372: getEditor().getWorkflow().toggleRedSign();
// 378: getEditor().getWorkflow().toggleWideSign();
// 385: getEditor().getWorkflow().shadeZone(false);
// 437: AbsoluteGroup g = getEditor().getWorkflow().buildAbsoluteGroup();
// 445: getEditor().getWorkflow().replaceSelectionByAbsoluteGroup(g);
// 469: getEditor().getWorkflow().doShade(shade);
// 481: getEditor().getWorkflow().doShadeSign(shade);
// 488: getEditor().getWorkflow().addPhilologicalMarkup(code);
// 495: getEditor().getWorkflow().insertMDC("\"" + protectedText + "\"");
// 502: getEditor().getWorkflow().insertElement(element);
// 511: getEditor().getWorkflow().addCartouche(type, start, end);
// 547: getEditor().getWorkflow().selectAll();
// 554: getEditor().getWorkflow().insertPageBreak();
// 561: getEditor().getWorkflow().explodeGroup();
// 567: getEditor().getWorkflow().groupVertical();
// 573: getEditor().getWorkflow().groupHorizontally();
// 595: getEditor().getWorkflow().clear();
// 1 140: getEditor().getWorkflow().addSign(code);
// Varia :
// 1 518: caretActionManager = new
// CaretActionManager(getEditor().getWorkflow());
// 1 852: new UndoRedoActionManager(getEditor().getWorkflow(), undoAction, 