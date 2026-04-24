/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
package jsesh.jhotdraw.documentview;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.jhotdraw_7_6.app.AbstractView;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.edit.CopyAction;
import org.jhotdraw_7_6.app.action.edit.CutAction;
import org.jhotdraw_7_6.app.action.edit.PasteAction;
import org.jhotdraw_7_6.app.action.edit.RedoAction;
import org.jhotdraw_7_6.app.action.edit.UndoAction;
import org.jhotdraw_7_6.gui.JFileURIChooser;
import org.jhotdraw_7_6.gui.URIChooser;
import org.qenherkhopeshef.observable.ObservableEventListener;
import org.qenherkhopeshef.swingUtils.errorHandler.UserMessage;

import jsesh.editor.ActionsID;
import jsesh.editor.JMDCEditor;
import jsesh.editor.MDCModelTransferableBroker;
import jsesh.editor.events.TextEvent;
import jsesh.graphics.export.pdfExport.PDFExportPreferences;
import jsesh.graphics.export.pdfExport.PDFExporter;
import jsesh.io.importer.pdf.PDFImportException;
import jsesh.io.importer.pdf.PDFImporter;
import jsesh.io.importer.rtf.RTFImportException;
import jsesh.io.importer.rtf.RTFImporter;
import jsesh.jhotdraw.JSeshApplicationCore;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.preferences.application.model.FontInfo;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.file.DocumentPreferences;
import jsesh.mdc.file.MDCDocument;
import jsesh.mdc.file.MDCDocumentReader;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItemList;
import jsesh.resources.JSeshMessages;
import jsesh.utils.JSeshWorkingDirectory;

/**
 * A view of a JSesh editor instance, as used by the jhotdraw framework.
 * <p>
 * JHotdraw-specific code is somehow limited to this class, as most of
 * the work is performed by the JSeshViewModel.
 * 
 * <p>
 * This is a good thing, as it allows to keep the coupling with jhotdraw low,
 * and to reuse the view model in other contexts if needed.
 * It also makes it easier to test the view model without having to deal with
 * jhotdraw.
 * 
 * @author rosmord
 */
@SuppressWarnings("serial")
public class JSeshView extends AbstractView {

    /**
     * Name of the property fired when document information change.
     */
    public static final String DOCUMENT_INFO_PROPERTY = "documentInfo";

    /**
     * The view model. Should be final, but we wait until the call to init() to
     * initialize it.
     */
    private JSeshViewCore viewCore;

    private ObservableEventListener<TextEvent> viewModelListener = e -> updateViewData();

    public JSeshView() {
    }

    @Override
    public void init() {
        // We wait for initWithResources() call to perform actual initialization.
    }

    /**
     * Type-safe initialization method, called by the application.
     */
    public void initWithResources(JSeshApplicationCore appBase) {
        viewCore = new JSeshViewCore(appBase.getFontKit(), appBase.newDocumentStyle());
        setFontInfo(appBase.getFontInfo()); // Moved from JSeshApplicationModel.initView, which was not the right place
                                            // for it.
        setFocusable(false); // Focus should go to the editor, not to the view.
        setLayout(new BorderLayout());
        add(viewCore.getViewComponent(), BorderLayout.CENTER);
        viewCore.setOwner(viewModelListener);
        initActions();
    }

    /**
     * React to changes in the model.
     */
    private void updateViewData() {
        setHasUnsavedChanges(!getEditor().getHieroglyphicTextModel()
                .isClean());
    }

    @Override
    public void start() {
        super.start();
        viewCore.getEditor().requestFocusInWindow();
    }

    public JMDCEditor getEditor() {
        return viewCore.getEditor();
    }

    @Override
    public void dispose() {
        // The document model might be disposable, it would be cleaner.
        // anyway:
        viewCore.getEditor().clearText();
        super.dispose();

    }

    /**
     * @return @see jsesh.jhotdraw.JSeshViewModel#getMdcDocument()
     */
    public MDCDocument getMdcDocument() {
        return viewCore.getMdcDocument();
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
            SwingUtilities.invokeAndWait(() -> {
                getEditor().clearText();
                setHasUnsavedChanges(false);
            });
        } catch (RuntimeException | InterruptedException | InvocationTargetException e) {
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
     * @param uri     uri for the file ; may be null.
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
                SwingUtilities.invokeLater(() -> viewCore.setCurrentDocument(document));
            } catch (RTFImportException e) {
                throw new UserMessage(e.getMessage());
            }
        } else if (uri.getSchemeSpecificPart().equals("pdf")) {
            try {
                final MDCDocument document = (PDFImporter
                        .createPDFPasteImporter(new File("Unnamed.gly"))
                        .getMdcDocument());
                SwingUtilities.invokeLater(() -> viewCore.setCurrentDocument(document));
            } catch (PDFImportException e) {
                throw new UserMessage(e.getMessage());
            }
        }
    }

    /**
     * Read a JSesh file in the current view.
     * 
     * <p>
     * TODO : improve this code, which is
     * not correct regarding the EDT.
     *
     * <p>
     * Given its strong coupling with the JHotdraw framework logic, we keep it in
     * view.
     * 
     * @param uri
     */
    private void readFromFile(URI uri) {
        File file = new File(uri);
        try {
            if (file.getName().toLowerCase(Locale.ENGLISH).endsWith(".pdf")) {
                FileInputStream in = new FileInputStream(file);
                PDFImporter importer = PDFImporter.createPDFStreamImporter(in,
                        file);
                final MDCDocument document = importer.getMdcDocument();
                document.setFile(new File(JSeshWorkingDirectory
                        .getWorkingDirectory(), "Unnamed.gly"));
                SwingUtilities.invokeLater(
                        () -> viewCore.setCurrentDocument(document));
            } else {
                MDCDocumentReader mdcDocumentReader = new MDCDocumentReader();
                final MDCDocument document = mdcDocumentReader.loadFile(file);
                // Observe changes to this document in the future.
                SwingUtilities.invokeLater(() -> {
                    viewCore.setCurrentDocument(document);
                    // Fire the corresponding event, with dummy
                    // properties...
                    // We might decide to use "real" property at some point.
                    firePropertyChange(DOCUMENT_INFO_PROPERTY, false, true);
                });
            }
        } catch (MDCSyntaxError e) {
            String msg = "error at line " + e.getLine();
            msg += " near token: " + e.getToken();
            displayErrorInEdt(JSeshMessages.getString("syntaxError.title"), msg);

            System.err.println(e.getCharPos());
        } catch (IOException | PDFImportException e) {
            throw new UserMessage(e);
        }

    }

    private void displayErrorInEdt(final String title, final String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(getParent(), message, title,
                JOptionPane.ERROR_MESSAGE));
    }

    @Override
    public void write(URI uri, URIChooser chooser) throws IOException {
        File file = new File(uri);
        MDCDocument document = viewCore.getMdcDocument();
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
                        getEditor()
                                .getTextDirection())
                .withTextOrientation(
                        getEditor()
                                .getTextOrientation())
                .withSmallSignCentered(
                        getEditor()
                                .isSmallSignsCentered());

        // TODO END OF TEMPORARY PATCH
        // Check if the file is PDF or MdC
        boolean isPdfFile = false;
        if (document.getFile() != null) {
            String fileName = document.getFile().getName().toLowerCase(Locale.ENGLISH);
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
            prefs.setJseshStyle(viewCore.getJSeshStyle()); // Check if jseshStyle is needed here... We pass a render
                                                           // context!
            PDFExporter exporter = new PDFExporter();
            exporter.setPdfExportPreferences(prefs);
            TopItemList model = document.getHieroglyphicTextModel().getModel();
            exporter.exportModel(model, viewCore.getCaret(), viewCore.getRenderContext());
        } else {
            document.save();
        }
    }

    /**
     * Gets original line number coordinates of a certain point in the text.
     * <p>
     * If the document contains line-number indications, like (vo, 3) which
     * reference the actual source document (ostracon, papyrus...), this
     * function will return the coordinates for a given point in text.
     * 
     * @param position technical position in the JSesh document.
     * @return the position in the original document, or the empty string if none is
     *         found.
     */
    public String getOriginalDocumentCoordinates(MDCPosition position) {
        return viewCore.getOriginalDocumentCoordinates(position);
    }

    @Override
    public void setEnabled(boolean enabled) {
        viewCore.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    /**
     * Sets the object which will be used to generate copy/paste information.
     * This object must handle the current copy/paste selection.
     *
     * @param mdcModelTransferableBroker
     */
    public void setMDCModelTransferableBroker(
            MDCModelTransferableBroker mdcModelTransferableBroker) {
        viewCore.getEditor().setMdcModelTransferableBroker(
                mdcModelTransferableBroker);
    }

    // TODO : move to core.
    public void insertMDC(String mdcText) {
        viewCore.getEditor().getWorkflow().insertMDC(mdcText);
    }

    @Override
    public boolean canSaveTo(URI uri) {
        if (uri != null && !"file".equals(uri.getScheme())) {
            return false;
        }
        return super.canSaveTo(uri);
    }

   

    /**
     * Returns a <em>name</em> suitable for use as basis for export files (as
     * pictures). For instance, if the original file name is Sinuhe.gly, it will
     * be "Sinuhe". If there is no file, the name will be equals to the scheme,
     * or, in despair, to "untitled".
     *
     * @return a string
     */
    public String baseFileName() {
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
     *
     * @param extension the extension to use (without "."). Exemply gratia :
     *                  "rtf" or "png".
     * @return
     */
    public File createDefaultExportFile(String extension) {
        String name = baseFileName();        
        return new File(getApplicationModel().getCurrentDirectory(), name + "."
                + extension);
    }

    /**
     * Message display system... to do.
     *
     * @param message the message to display.
     */
    public void setMessage(String message) {
        viewCore.setMessage(message);
    }

    public void setOrientation(TextOrientation orientation) {
        viewCore.getEditor().setTextOrientation(orientation);
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
        viewCore.setJSeshStyle(viewCore.getJSeshStyle().copy().options(o -> o.smallSignCentered(selected)).build());
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
        viewCore.setJustify(selected);
        firePropertyChange(DOCUMENT_INFO_PROPERTY, false, true);
    }

    public void setTextOrientation(TextOrientation textOrientation) {
        viewCore.setTextOrientation(textOrientation);
        // getEditor().setTextOrientation(textOrientation);
        firePropertyChange(DOCUMENT_INFO_PROPERTY, false, true);
    }

    public void setTextDirection(TextDirection textDirection) {
        viewCore.setTextDirection(textDirection);
        firePropertyChange(DOCUMENT_INFO_PROPERTY, false, true);
    }

    /**
     * Change the fonts JSesh uses.
     * Used:
     * <ul>
     * <li>when creating the view
     * <li>when the hieroglyphic font preferences are changed
     * </ul>
     * 
     * If we move to a MVC architecture, it might become obsolete.
     * 
     * @param fontInfo
     */
    public void setFontInfo(FontInfo fontInfo) {
        viewCore.setFontInfo(fontInfo);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jhotdraw_7_6.gui.EditableComponent#delete()
     */
    public void delete() {
        getEditor().getWorkflow().doBackspace();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jhotdraw_7_6.gui.EditableComponent#duplicate()
     */    
    public void duplicate() {
        // Currently no-op.
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jhotdraw_7_6.gui.EditableComponent#selectAll()
     */
    public void selectAll() {
        getEditor().getWorkflow().selectAll();
    }

    public void selectCurrentLine() {
        getEditor().getWorkflow().selectCurrentLine();
    }

    

    /*
     * (non-Javadoc)
     * 
     * @see org.jhotdraw_7_6.gui.EditableComponent#clearSelection()
     */
    public void clearSelection() {
        getEditor().getWorkflow().clearSelection();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jhotdraw_7_6.gui.EditableComponent#isSelectionEmpty()
     */
    public boolean isSelectionEmpty() {
        return getEditor().hasSelection();
    }

    /**
     * Returns the frame-independant core of the view.
     * 
     * @return the viewCore
     */
    public JSeshViewCore core() {
        return viewCore;
    }

    private JSeshApplicationModel getApplicationModel() {
        return (JSeshApplicationModel) getApplication().getModel();
    }

}
