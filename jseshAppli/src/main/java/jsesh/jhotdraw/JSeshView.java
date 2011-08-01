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

import jsesh.editor.ActionsID;
import jsesh.editor.JMDCEditor;
import jsesh.editor.MDCModelTransferableBroker;
import jsesh.editor.caret.MDCCaret;
import jsesh.graphics.export.ExportData;
import jsesh.graphics.export.pdfExport.PDFExportPreferences;
import jsesh.graphics.export.pdfExport.PDFExporter;
import jsesh.io.importer.pdf.PDFImportException;
import jsesh.io.importer.pdf.PDFImporter;
import jsesh.io.importer.rtf.RTFImportException;
import jsesh.io.importer.rtf.RTFImporter;
import jsesh.jhotdraw.applicationPreferences.model.FontInfo;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.constants.ScriptCodes;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.file.MDCDocument;
import jsesh.mdc.file.MDCDocumentReader;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.DrawingSpecificationsImplementation;

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

	/**
	 * Name of the property fired when document information change.
	 */
	public static final String DOCUMENT_INFO_PROPERTY = "documentInfo";

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
		add(viewModel.getBottomPanel(), BorderLayout.PAGE_END);
		add(viewModel.getTopPanel(), BorderLayout.PAGE_START);
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

	/**
	 * @return
	 * @see jsesh.jhotdraw.JSeshViewModel#getMdcDocument()
	 */
	public MDCDocument getMdcDocument() {
		return viewModel.getMdcDocument();
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
		// Fetch actions from the view editor.
		for (Object actionIDa : getEditor().getActionMap().keys()) {
			if (getActionMap().get(actionIDa) == null)
				getActionMap().put(actionIDa, getEditor().getActionMap().get(actionIDa));
		}
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

	/**
	 * Read a document from the clipboard. This is not done in the EDT.
	 * 
	 * @param uri
	 */
	private void readFromClipboard(URI uri) {
		if (uri.getSchemeSpecificPart().equals("rtf")) {
			try {
				final MDCDocument document = RTFImporter
						.createRTFPasteImporter(new File("Unnamed.gly"))
						.getMdcDocument();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						viewModel.setCurrentDocument(document);
					}
				});
			} catch (RTFImportException e) {
				throw new UserMessage(e.getMessage());
			}
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
	 * Read a JSesh file in the current view. TODO : improve this code, which is
	 * not correct regarding the EDT.
	 * 
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
					// Fire the corresponding event, with dummy properties...
					// We might decide to use "real" property at some point.
					firePropertyChange(DOCUMENT_INFO_PROPERTY, false, true);
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

	@Override
	public boolean canSaveTo(URI uri) {
		if (uri != null && !"file".equals(uri.getScheme()))
			return false;
		return super.canSaveTo(uri);
	}

	/**
	 * Returns true if a selection is available.
	 * 
	 * @return
	 */
	public boolean hasSelection() {
		return viewModel.getEditor().hasSelection();
	}

	/**
	 * Returns the data needed for the graphical export of a selection.
	 * 
	 * @return
	 */
	public ExportData getExportData() {
		// Note : there is some doubt over which drawing specifications should
		// be used ?
		return new ExportData(getDrawingSpecifications(), getCaret(), viewModel
				.getMdcDocument().getHieroglyphicTextModel().getModel(), 1f);
	}

	/**
	 * Returns a <em>name</em> suitable for use as basis for export files (as
	 * pictures). For instance, if the original file name is Sinuhe.gly, it will
	 * be "Sinuhe". If there is no file, the name will be equals to the scheme,
	 * or, in despair, to "untitled".
	 * 
	 * <p>
	 * This method could be moved (or delegated to JSeshViewModel)
	 * 
	 * @return a string
	 */
	public String getBaseFileName() {
		if (uri == null) {
			return "untitled";
		} else if (uri.getScheme() == null || "file".equals(uri.getScheme())) {
			File f = new File(uri);
			String name = f.getName();
			int pos = name.lastIndexOf(".");
			if (pos != -1) {
				name = name.substring(0, pos);
			}
			return name;
		} else
			return uri.getScheme();

	}

	/**
	 * Create a File object suitable for saving (parts of) the current document
	 * in a certain format.
	 * 
	 * <p>
	 * This method could be moved (or delegated to JSeshViewModel)
	 * 
	 * @param extension
	 *            the extension to use (without "."). Exemply gratia : "rtf" or
	 *            "png".
	 * @return
	 */
	public File buildDefaultExportFile(String extension) {
		String name = getBaseFileName();
		JSeshApplicationModel applicationModel = (jsesh.jhotdraw.JSeshApplicationModel) getApplication()
				.getModel();
		return new File(applicationModel.getCurrentDirectory(), name + "."
				+ extension);
	}

	/**
	 * Message display system... to do.
	 * 
	 * @param message
	 *            the message to display.
	 */
	public void setMessage(String message) {
		viewModel.setMessage(message);
	}

	public void setOrientation(TextOrientation orientation) {
		viewModel.getEditor().setTextOrientation(orientation);
		firePropertyChange(DOCUMENT_INFO_PROPERTY, false, true);
	}
	
	public void setSmallSignsCentered(boolean selected) {
		// Rather bad design: the info is kept both in drawingspecs
		// and in the document.
		/*
		 * TODO CLEANUP THIS MESS (well we do have this mess since we introduced
		 * this capability in JSesh and we still have it now, including the
		 * "bad design" comment...
		 * 
		 * There should be some kind of "document event" system there... (better
		 * still, have a look at Buoy, and propose something on the lines of...
		 * document.addEventLink(FormatEvent.class, menuManager,
		 * updateMenuItems); )
		 */
		getEditor().setSmallSignsCentered(selected);
		getMdcDocument().setSmallSignCentered(selected);
		getEditor().invalidateView();
		firePropertyChange(DOCUMENT_INFO_PROPERTY, false, true);
	}

	public void setTextOrientation(TextOrientation textOrientation) {
		getEditor().setTextOrientation(textOrientation);
		firePropertyChange(DOCUMENT_INFO_PROPERTY, false, true);
	}
	
	public void setTextDirection(TextDirection textDirection) {
		getEditor().setTextDirection(textDirection);
		firePropertyChange(DOCUMENT_INFO_PROPERTY, false, true);		
	}

	/**
	 * Change the fonts JSesh uses.
	 * @param fontInfo
	 */
	public void setFontInfo(FontInfo fontInfo) {
		DrawingSpecification drawingSpecification= getDrawingSpecifications().copy();
		fontInfo.applyToDrawingSpecifications(drawingSpecification);
		setDrawingSpecifications(drawingSpecification);
	}
}