package jsesh.jhotdraw.viewClass;

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
import javax.swing.filechooser.FileFilter;

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
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.Messages;
import jsesh.jhotdraw.applicationPreferences.model.FontInfo;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.file.DocumentPreferences;
import jsesh.mdc.file.MDCDocument;
import jsesh.mdc.file.MDCDocumentReader;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.search.MdCSearchQuery;
import jsesh.utils.JSeshWorkingDirectory;

import org.jhotdraw_7_6.app.AbstractView;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.edit.CopyAction;
import org.jhotdraw_7_6.app.action.edit.CutAction;
import org.jhotdraw_7_6.app.action.edit.PasteAction;
import org.jhotdraw_7_6.app.action.edit.RedoAction;
import org.jhotdraw_7_6.app.action.edit.UndoAction;
import org.jhotdraw_7_6.gui.JFileURIChooser;
import org.jhotdraw_7_6.gui.URIChooser;
import org.qenherkhopeshef.swingUtils.errorHandler.UserMessage;

/**
 * A view of a JSesh editor instance, as used by the jhotdraw framework.
 * <p>
 * NOTE : I have tried to isolate JHotdraw code and generic Swing code by
 * creating a JSeshViewModel class. However my current code has many
 * architectural problems dealing with DEMETER law.</p>
 * <p>
 * The problems might be: a) outright violation of DEMETER law (often quite
 * deep...) or b) attempts to solve it which results in endless duplications of
 * delegating methods.</p>
 * <p>
 * As a result, I'm not always secure about <strong>where</strong>
 * the code should go.
 *
 * @author rosmord
 */
@SuppressWarnings("serial")
public class JSeshView extends AbstractView {

    /**
     * Name of the property fired when document information change.
     */
    public static final String DOCUMENT_INFO_PROPERTY = "documentInfo";

    private final JSeshViewModel viewModel;

    public JSeshView() {
        viewModel = new JSeshViewModel();
    }

    @Override
    public void init() {
        setFocusable(false); // Focus should go to the editor, not to the view.
        setLayout(new BorderLayout());
        add(viewModel.getViewComponent(), BorderLayout.CENTER);
        viewModel.setObserver(new MyObserver());
        initActions();
    }

    @Override
    public void start() {
        super.start();
        viewModel.getEditor().requestFocusInWindow();
    }

    public JMDCEditor getEditor() {
        return viewModel.getEditor();
    }

    @Override
    public void dispose() {
        // The document model might be disposable, it would be cleaner.
        // anyway:
        viewModel.getEditor().clearText();
        viewModel.getEditor().setCached(false);
        super.dispose();

    }

    /**
     * @return @see jsesh.jhotdraw.JSeshViewModel#getMdcDocument()
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
            if (getActionMap().get(actionIDa) == null) {
                getActionMap().put(actionIDa,
                        getEditor().getActionMap().get(actionIDa));
            }
        }
    }

    @Override
    public void clear() {
        try {
            SwingUtilities.invokeAndWait(()
                    -> getEditor().clearText());
        } catch (RuntimeException |InterruptedException | InvocationTargetException e) {
            throw new UserMessage(e.getMessage());
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
     * @param uri uri for the file ; may be null.
     * @param chooser chooser to find an uri if uri is null.
     * @throws java.io.IOException
     * @see View#read(URI, URIChooser)
     */
    @Override
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
                SwingUtilities.invokeLater(()
                        -> viewModel.setCurrentDocument(document)
                );
            } catch (RTFImportException e) {
                throw new UserMessage(e.getMessage());
            }
        } else if (uri.getSchemeSpecificPart().equals("pdf")) {
            try {
                final MDCDocument document = (PDFImporter
                        .createPDFPasteImporter(new File("Unnamed.gly"))
                        .getMdcDocument());
                SwingUtilities.invokeLater(()
                        -> viewModel.setCurrentDocument(document));
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
        try {
            if (file.getName().toLowerCase().endsWith(".pdf")) {
                FileInputStream in = new FileInputStream(file);
                PDFImporter importer = PDFImporter.createPDFStreamImporter(in,
                        file);
                final MDCDocument document = importer.getMdcDocument();
                document.setFile(new File(JSeshWorkingDirectory
                        .getWorkingDirectory(), "Unnamed.gly"));
                SwingUtilities.invokeLater(
                        ()->viewModel.setCurrentDocument(document));
            } else {
                MDCDocumentReader mdcDocumentReader = new MDCDocumentReader();
                final MDCDocument document = mdcDocumentReader.loadFile(file);
                // Observe changes to this document in the future.
                SwingUtilities.invokeLater(() -> {
                    viewModel.setCurrentDocument(document);
                    // Fire the corresponding event, with dummy
                    // properties...
                    // We might decide to use "real" property at some point.
                    firePropertyChange(DOCUMENT_INFO_PROPERTY, false, true);
                });
            }
        } catch (MDCSyntaxError e) {
            String msg = "error at line " + e.getLine();
            msg += " near token: " + e.getToken();
            displayErrorInEdt(Messages.getString("syntaxError.title"), msg);

            System.err.println(e.getCharPos());
        } catch (IOException | PDFImportException e) {
            throw new UserMessage(e);
        }

    }

    private void displayErrorInEdt(final String title, final String message) {
        SwingUtilities.invokeLater(()
                -> JOptionPane.showMessageDialog(getParent(), message, title,
                        JOptionPane.ERROR_MESSAGE)
        );
    }

    @Override
    public void write(URI uri, URIChooser chooser) throws IOException {
        File file = new File(uri);
        MDCDocument document = viewModel.getMdcDocument();
        document.setFile(file);

        // TODO : create a sane system for dealing with text orientation and
        // direction.
        // Currently, the "document" data is not synchronized with the
        // content of the editor.
        // CHECK IF THIS IS TRUE.... I DON'T BELIEVE IT ?
        // NOW, MOST METHODS LIKE THE ACTION TO CENTER DO CHANGE THE DOCUMENT.
        // HOWEVER,
        // THERE ARE SMALL PROBLEMS... SOLUTION : store document preferences in
        // the drawing specifications ?
        DocumentPreferences documentPreferences = document
                .getDocumentPreferences();

        documentPreferences = documentPreferences
                .withTextDirection(
                        getEditor().getDrawingSpecifications()
                                .getTextDirection())
                .withTextOrientation(
                        getEditor().getDrawingSpecifications()
                                .getTextOrientation())
                .withSmallSignCentered(
                        getEditor().getDrawingSpecifications()
                                .isSmallSignsCentered());

        // TODO END OF TEMPORARY PATCH
        // Check if the file is PDF or MdC
        boolean isPdfFile = false;
        if (document.getFile() != null) {
            String fileName = document.getFile().getName().toLowerCase();
            if (fileName.endsWith(".pdf")) {
                isPdfFile = true;
            } else if (!fileName.endsWith(".gly") && !fileName.endsWith(".hie")
                    && chooser != null && chooser instanceof JFileURIChooser) {
                FileFilter filter = ((JFileURIChooser) chooser).getFileFilter();
                if (filter != null && filter.accept(new File("toto.pdf"))
                        && !filter.accept(new File("toto.gly"))) {
                    isPdfFile = true;
                }
            }
        }
        if (isPdfFile) {
            // Create the prefs for this document... move the code to document ?
            // or what ?
            // more info should also be saved in the case of PDF files (pdf
            // prefs).
            // TODO save PDF prefs in pdf files...
            PDFExportPreferences prefs = new PDFExportPreferences();
            prefs.setFile(document.getFile());
            prefs.setDrawingSpecifications(getDrawingSpecifications().copy());
            prefs.getDrawingSpecifications().setTextDirection(
                    documentPreferences.getTextDirection());
            prefs.getDrawingSpecifications().setTextOrientation(
                    documentPreferences.getTextOrientation());
            prefs.getDrawingSpecifications().setSmallSignsCentered(
                    documentPreferences.isSmallSignCentered());
            PDFExporter exporter = new PDFExporter();
            exporter.setPdfExportPreferences(prefs);
            TopItemList model = document.getHieroglyphicTextModel().getModel();
            exporter.exportModel(model, MDCCaret.buildWholeTextCaret(model));
        } else {
            document.save();
        }
        // currentMDCDirectory = currentDocument.getFile().getParentFile();
    }

    public void doSearch(MdCSearchQuery query) {
        viewModel.doSearch(query);
    }

    public void nextSearch() {
        viewModel.nextSearch();
    }

    /**
     * This observer keeps tracks of unsaved changes.
     */
    private class MyObserver implements Observer {

        @Override
        public void update(Observable o, Object arg) {
            setHasUnsavedChanges(!getEditor().getHieroglyphicTextModel()
                    .isClean());
        }
    }

    public void insertCode(String code) {
        getEditor().insert(code);
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
        viewModel.setDrawingSpecifications(drawingSpecifications);
    }

    /**
     * Sets the object which will be used to generate copy/paste information.
     * This object must handle the current copy/paste selection.
     *
     * @param mdcModelTransferableBroker
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
        if (uri != null && !"file".equals(uri.getScheme())) {
            return false;
        }
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
        } else {
            return uri.getScheme();
        }

    }

    /**
     * Create a File object suitable for saving (parts of) the current document
     * in a certain format.
     *
     * <p>
     * This method could be moved (or delegated to JSeshViewModel)
     *
     * @param extension the extension to use (without "."). Exemply gratia :
     * "rtf" or "png".
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
     * @param message the message to display.
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
        DrawingSpecification specs = getDrawingSpecifications().copy();
        specs.setSmallSignsCentered(selected);
        viewModel.setDrawingSpecifications(specs);
        // getEditor().setSmallSignsCentered(selected);
        /*
         * getMdcDocument().setDocumentPreferences(
         * getMdcDocument().getDocumentPreferences()
         * .withSmallSignCentered(selected)); getEditor().invalidateView();
         */
        firePropertyChange(DOCUMENT_INFO_PROPERTY, false, true);
    }

    /**
     * Temporary method to control lines justification.
     *
     * @param selected
     */
    public void setJustify(boolean selected) {
        DrawingSpecification specs = getDrawingSpecifications().copy();
        specs.setJustified(selected);
        viewModel.setDrawingSpecifications(specs);
        firePropertyChange(DOCUMENT_INFO_PROPERTY, false, true);
    }

    public void setTextOrientation(TextOrientation textOrientation) {
        DrawingSpecification specs = getDrawingSpecifications().copy();
        specs.setTextOrientation(textOrientation);
        viewModel.setDrawingSpecifications(specs);
        // getEditor().setTextOrientation(textOrientation);
        firePropertyChange(DOCUMENT_INFO_PROPERTY, false, true);
    }

    public void setTextDirection(TextDirection textDirection) {
        DrawingSpecification specs = getDrawingSpecifications().copy();
        specs.setTextDirection(textDirection);
        viewModel.setDrawingSpecifications(specs);
        // getEditor().setTextDirection(textDirection);
        firePropertyChange(DOCUMENT_INFO_PROPERTY, false, true);
    }

    /**
     * Change the fonts JSesh uses.
     *
     * @param fontInfo
     */
    public void setFontInfo(FontInfo fontInfo) {
        DrawingSpecification drawingSpecification = getDrawingSpecifications()
                .copy();
        fontInfo.applyToDrawingSpecifications(drawingSpecification);
        setDrawingSpecifications(drawingSpecification);
    }

    /* (non-Javadoc)
     * @see org.jhotdraw_7_6.gui.EditableComponent#delete()
     */
    public void delete() {
        getEditor().getWorkflow().doBackspace();
    }

    /* (non-Javadoc)
     * @see org.jhotdraw_7_6.gui.EditableComponent#duplicate()
     */
    public void duplicate() {
        // Currently no-op.
    }

    /* (non-Javadoc)
     * @see org.jhotdraw_7_6.gui.EditableComponent#selectAll()
     */
    public void selectAll() {
        getEditor().getWorkflow().selectAll();
    }

    public void selectCurrentLine() {
        getEditor().getWorkflow().selectCurrentLine();
    }
    
     public void selectCurrentPage() {
        getEditor().getWorkflow().selectCurrentPage();
    }
    /* (non-Javadoc)
     * @see org.jhotdraw_7_6.gui.EditableComponent#clearSelection()
     */
    public void clearSelection() {
        getEditor().getWorkflow().clearSelection();
    }

    /* (non-Javadoc)
     * @see org.jhotdraw_7_6.gui.EditableComponent#isSelectionEmpty()
     */
    public boolean isSelectionEmpty() {
        return getEditor().hasSelection();
    }

}
