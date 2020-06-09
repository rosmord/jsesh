package jsesh.jhotdraw.viewClass;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import jsesh.editor.JMDCEditor;
import jsesh.editor.MDCModelEditionAdapter;
import jsesh.editor.caret.MDCCaret;
import jsesh.hieroglyphs.data.HieroglyphDatabaseRepository;
import jsesh.hieroglyphs.data.HieroglyphFamily;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.actions.edit.OpenHieroglyphicMenuAction;
import jsesh.mdc.file.DocumentPreferences;
import jsesh.mdc.file.MDCDocument;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.operations.ModelOperation;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.editor.MdCSearchQuery;
import jsesh.swing.hieroglyphicMenu.HieroglyphicMenu;
import jsesh.swing.hieroglyphicMenu.HieroglyphicMenuListener;

/**
 * An abstract (more or less framework-agnostic) representation of an editing
 * session of a JSesh document. It might be better to merge it with JSeshView.
 *
 * <p>
 * TODO this class is a bit too heavy for my taste... nothing as awful as the
 * old JSesh application, but still...
 *
 * @author rosmord
 */
public final class JSeshViewModel {

    /**
     * The main graphical component.
     */
    private final JSeshViewComponent viewComponent;

    /**
     * The document we are working on.
     */
    private MDCDocument mdcDocument;

    /**
     * An object which will learn when the view is modified (typically for us
     * the JSeshView).
     */
    private Observer observer;

    /**
     * Possible list of search results.
     */
    private List<MDCPosition> lastSearchResults = null;

    private final DelegatingObserver delegatingObserver = new DelegatingObserver();

    public JSeshViewModel() {
        viewComponent = new JSeshViewComponent();
        setCurrentDocument(new MDCDocument());

        // Activate the objects
        // The code model
        CodeModel codeModel = new CodeModel();
        MDCLineManager mdcLineManager = new MDCLineManager();

        getEditor().addCodeChangeListener(codeModel);
        getEditor().getWorkflow().addMDCModelListener(mdcLineManager);
        viewComponent.getMdcField().addActionListener(mdcLineManager);
        
        // Zoom combobox
        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
        for (int zoom : new int[]{25, 50, 75, 100, 112, 128, 150, 200, 400,
            600, 800, 1600, 3200, 6400, 12800}) {
            comboBoxModel.addElement(new ZoomInfo(zoom));
        }
        viewComponent.getZoomComboBox().setModel(comboBoxModel);
        viewComponent.getZoomComboBox().setSelectedIndex(7);
        viewComponent.getZoomComboBox().addActionListener(e -> doZoom());

        JPopupMenu hieroglyphicMenu = buildHieroglyphicMenus();
        JButton hieroglyphsButton = viewComponent.getHieroglyphsButton();
        hieroglyphsButton.setAction(new OpenHieroglyphicMenuAction(
                hieroglyphsButton, hieroglyphicMenu));
    }

    private void doZoom() {
        ZoomInfo zoomInfo = (ZoomInfo) viewComponent.getZoomComboBox().getSelectedItem();
        if (zoomInfo != null) {
            getEditor().setScale(zoomInfo.zoom / 100.0);
        }
    }

    public JMDCEditor getEditor() {
        return viewComponent.getEditor();
    }

    public MDCDocument getMdcDocument() {
        return mdcDocument;
    }

    public final void setCurrentDocument(MDCDocument newDocument) {
        if (mdcDocument != null) {
            mdcDocument.getHieroglyphicTextModel().deleteObserver(delegatingObserver);
        }
        mdcDocument = newDocument;
        mdcDocument.getHieroglyphicTextModel().addObserver(delegatingObserver);
        DocumentPreferences prefs = mdcDocument.getDocumentPreferences();
        DrawingSpecification ds = getEditor().getDrawingSpecifications();
        ds.applyDocumentPreferences(prefs);
        getEditor().setDrawingSpecifications(ds);
        getEditor().setHieroglyphiTextModel(mdcDocument.getHieroglyphicTextModel());
    }

    public void setEnabled(boolean enabled) {
        getEditor().setEnabled(enabled);
    }

    public JSeshViewComponent getViewComponent() {
        return viewComponent;
    }

    
    /**
     * Synchronize the hieroglyphic editor and the optional mdc line display
     * below it.
     *
     * Plus clears the last search when the text has been modified.
     *
     * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
     */
    private final class MDCLineManager extends MDCModelEditionAdapter implements
            ActionListener {

        private boolean textLineRefreshAsked = false;

        public void caretChanged(MDCCaret caret) {
            updateCurrentMDCLine();
        }

        public void textChanged() {
            updateCurrentMDCLine();
            lastSearchResults = null;
        }

        public void textEdited(ModelOperation op) {
            updateCurrentMDCLine();
            lastSearchResults = null;
        }

        /**
         * Mdc field update, performed "later", but still on the EDT.
         */
        public void updateMdCField() {
            textLineRefreshAsked = true;
            String s = getEditor().getWorkflow().getCurrentLineAsString();
            viewComponent.getMdcField().setText(s);
            textLineRefreshAsked = false;
        }

        /**
         * Called when the MdC text editor field has been validated. Don't
         * de-activate the MdC field update : it will show what has been
         * understood.
         */
        public void actionPerformed(ActionEvent e) {
            getEditor().getWorkflow().setCurrentLineTo(viewComponent.getMdcField().getText());
        }

        /**
         * Update the displayed Manuel de codage line.
         */
        private void updateCurrentMDCLine() {
            // We don't need instant synchronization, especially if we are
            // moving
            // in the text at high speed.
            // Hence : when the text change, a mdc display change is only
            // *scheduled*.
            if (!this.textLineRefreshAsked) {
                textLineRefreshAsked = true;
                final Timer timer = new Timer(500, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                updateMdCField();
                            }
                        });
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        }
    }

    /**
     * Monitors the display of the current code buffer.
     *
     * @author S. Rosmorduc
     */
    private class CodeModel extends MDCModelEditionAdapter {

        public void separatorChanged() {
            viewComponent.getSeparatorField().setText("" + getEditor().getCurrentSeparator());
        }

        public void codeChanged(StringBuffer code) {
            viewComponent.getCodeField().setText(getEditor().getCodeBuffer());
        }
    }

    /**
     * Data for the zoom combobox
     *
     * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
     */
    private static class ZoomInfo {

        int zoom;

        public ZoomInfo(int zoom) {
            this.zoom = zoom;
        }

        @Override
        public String toString() {
            return zoom + " %";
        }
    }

    /**
     * Build the hieroglyphic menu.
     *
     * @return a menu containing only "standard" glyphs.
     */
    private JPopupMenu buildHieroglyphicMenus() {
        HieroglyphicMenuMediator mediator = new HieroglyphicMenuMediator();
        List<HieroglyphFamily> families = HieroglyphDatabaseRepository
                .getHieroglyphDatabase().getFamilies();
        BundleHelper bundle = BundleHelper.getInstance();

        JPopupMenu hieroglyphs = new JPopupMenu();

        hieroglyphs.setLayout(new GridLayout(14, 2));
        for (int i = 0; i < families.size(); i++) {
            HieroglyphFamily family = (HieroglyphFamily) families.get(i);

            HieroglyphicMenu fmenu = new HieroglyphicMenu(family.getCode()
                    + ". " + family.getDescription(), family.getCode(), 6);

            fmenu.setHieroglyphicMenuListener(mediator);
            if (i < 25) {
                fmenu.setMnemonic(family.getCode().toUpperCase().charAt(0));
            } else if (i == 25) {
                fmenu.setMnemonic(KeyEvent.VK_J);
            } else if (i == 26) {
                fmenu.setMnemonic(KeyEvent.VK_AMPERSAND);
            }
            hieroglyphs.add(fmenu);
        }

        HieroglyphicMenu[] others = new HieroglyphicMenu[]{
            new HieroglyphicMenu("Tall Narrow Signs",
            HieroglyphicMenu.TALL_NARROW, 6),
            new HieroglyphicMenu("Low Broad Signs",
            HieroglyphicMenu.LOW_BROAD, 6),
            new HieroglyphicMenu("Low Narrow Signs",
            HieroglyphicMenu.LOW_NARROW, 6)};
        for (HieroglyphicMenu m : others) {
            hieroglyphs.add(m);
            m.setHieroglyphicMenuListener(mediator);
        }

        //hieroglyphs.setMnemonic(KeyEvent.VK_H);
        return hieroglyphs;
    }

    public void setMessage(String messsage) {
        viewComponent.getMessageField().setText(messsage);
    }

    /**
     * Manages interactions between the menu and the view.
     *
     * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
     *
     */
    private class HieroglyphicMenuMediator implements HieroglyphicMenuListener {

        /**
         * Insert the sign in the document.
         */
        public void codeSelected(String code) {
            getEditor().insert(code);

        }

        /**
         * Display sign informations.
         */
        public void enter(String code) {
            setMessage(code);
        }

        // Erase the message
        public void exit(String code) {
            setMessage("");
        }
    }

    public void setDrawingSpecifications(
            DrawingSpecification drawingSpecifications) {
        getEditor().setDrawingSpecifications(drawingSpecifications);
        getMdcDocument().setDocumentPreferences(drawingSpecifications.extractDocumentPreferences());
    }

    /**
     * Sets the parent observer which will receive information when the view is
     * modified
     *
     * @param observer
     */
    public void setObserver(Observer observer) {
        this.observer = observer;
    }

    private class DelegatingObserver implements Observer {

        @Override
        public void update(Observable o, Object arg) {
            observer.update(o, arg);
        }
    }

    public void doSearch(MdCSearchQuery query) {
        this.lastSearchResults = getEditor().doSearch(query);
        this.nextSearch();
    }

    public void nextSearch() {
        if (this.lastSearchResults != null) {
            if (!this.lastSearchResults.isEmpty()) {
                MDCPosition nextPos = this.lastSearchResults.get(0);
                this.lastSearchResults.remove(0); // not efficient. But who cares.
                getEditor().getWorkflow().setCursor(nextPos);
            } else {
                this.lastSearchResults = null;
            }
        }
    }
}
