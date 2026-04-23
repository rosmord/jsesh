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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Locale;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.qenherkhopeshef.observable.ObservableEventListener;

import jsesh.defaults.JseshFontKit;
import jsesh.drawingspecifications.JSeshStyle;
import jsesh.drawingspecifications.PaintingSpecifications;
import jsesh.editor.JMDCEditor;
import jsesh.editor.MDCModelEditionAdapter;
import jsesh.editor.MdCSearchQuery;
import jsesh.editor.caret.MDCCaret;
import jsesh.editor.events.TextEvent;
import jsesh.graphics.export.generic.ExportData;
import jsesh.hieroglyphs.data.HieroglyphFamily;
import jsesh.jhotdraw.actions.edit.OpenHieroglyphicMenuAction;
import jsesh.jhotdraw.preferences.JSeshStyleHelper;
import jsesh.jhotdraw.preferences.application.model.FontInfo;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.file.DocumentPreferences;
import jsesh.mdc.file.MDCDocument;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.model.operations.ModelOperation;
import jsesh.mdcDisplayer.context.JSeshRenderContext;
import jsesh.swing.hieroglyphicMenu.HieroglyphicMenu;
import jsesh.swing.hieroglyphicMenu.HieroglyphicMenuListener;

/**
 * An abstract (more or less framework-agnostic) representation of an editing
 * session of a JSesh document. It might be better to merge it with JSeshView.
 *
 * @author rosmord
 */
public final class JSeshViewCore {

    /**
     * Predefined zoom factors for the zoom combo box.
     */
    private static final int[] ZOOMFACTORS = new int[] { 25, 50, 75, 100, 112, 128, 150, 200, 300, 400,
            600, 800, 1600, 3200, 6400, 12800 };

    /**
     * The main graphical component.
     */
    private final JSeshViewComponent<ZoomInfo> viewComponent;

    /**
     * Information about fonts and glyphs.
     */

    private JseshFontKit fontKit;

    /**
     * The document we are working on.
     */
    private MDCDocument mdcDocument;

    /**
     * An object which will learn when the view is modified (typically for us
     * the JSeshView).
     */
    private ObservableEventListener<TextEvent> owner;

    private final ObservableEventListener<TextEvent> delegatingObserver = e -> {
        // Tells the parent observer that the document has changed.
        if (owner != null) {
            owner.eventOccurred(e);
        }
    };

    /**
     * Possible list of search results.
     * 
     */
    private List<MDCPosition> lastSearchResults = null;

    /**
     * Create a view model.
     * 
     * @param fontKit information about hieroglyphic fonts.
     * @param style   the style of the new document.
     */
    public JSeshViewCore(JseshFontKit fontKit, JSeshStyle style) {
        this.fontKit = fontKit;
        viewComponent = new JSeshViewComponent<ZoomInfo>(fontKit, style);
        setCurrentDocument(new MDCDocument());

        // Activate the objects
        // The code model
        CodeModel codeModel = new CodeModel();
        MDCLineManager mdcLineManager = new MDCLineManager();

        getEditor().addCodeChangeListener(codeModel);
        getEditor().getWorkflow().addMDCModelListener(mdcLineManager);
        viewComponent.getMdcField().addActionListener(mdcLineManager);

        // Zoom combobox
        DefaultComboBoxModel<ZoomInfo> comboBoxModel = new DefaultComboBoxModel<>();
        for (int zoom : ZOOMFACTORS) {
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
            mdcDocument.getHieroglyphicTextModel().removeListener(delegatingObserver);
        }
        mdcDocument = newDocument;
        mdcDocument.getHieroglyphicTextModel().addListener(delegatingObserver);
        DocumentPreferences prefs = mdcDocument.getDocumentPreferences();
        JSeshStyle newStyle = JSeshStyleHelper.applyDocumentPreferences(prefs, getEditor().getJSeshStyle());
        getEditor().setJSeshStyle(newStyle);
        getEditor().setHieroglyphiTextModel(mdcDocument.getHieroglyphicTextModel());
    }

    public void setEnabled(boolean enabled) {
        getEditor().setEnabled(enabled);
    }

    public JSeshViewComponent<ZoomInfo> getViewComponent() {
        return viewComponent;
    }

    /**
     * Gets original line number coordinates of a certain point in the text.
     * <p>
     * If the document contains line-number indications, like (vo, 3) which
     * reference the actual source document (ostracon, papyrus...), this
     * function will return the coordinates for a given point in text.
     *
     * @param position technical position in the JSesh document.
     * @return the position in the original document, or the empty string if
     *         none is found.
     */
    public String getOriginalDocumentCoordinates(MDCPosition position) {
        return getEditor().getOriginalDocumentCoordinates(position);
    }

    public void insertLineNumber(String line) {
        getEditor().insertLineNumber(line);
    }

    /**
     * Returns the current caret.
     * <p>
     * TODO : we should simplify this... the inner
     * code should not use so many layers.
     *
     * @return a caret.
     */
    public MDCCaret getCaret() {
        return getEditor().getWorkflow().getCaret();
    }

    /**
     * Returns the inner text representation.
     *
     * @return
     */
    public TopItemList getTopItemList() {
        return getEditor().getHieroglyphicTextModel()
                .getModel();
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
        List<HieroglyphFamily> families = fontKit.hieroglyphDatabase().getFamilies();

        JPopupMenu hieroglyphs = new JPopupMenu();

        hieroglyphs.setLayout(new GridLayout(14, 2));
        for (int i = 0; i < families.size(); i++) {
            HieroglyphFamily family = (HieroglyphFamily) families.get(i);

            HieroglyphicMenu fmenu = new HieroglyphicMenu(family.getCode()
                    + ". " + family.getDescription(), family.getCode(), 6);

            fmenu.setHieroglyphicMenuListener(mediator);
            if (i < 25) {
                fmenu.setMnemonic(family.getCode().toUpperCase(Locale.ENGLISH).charAt(0));
            } else if (i == 25) {
                fmenu.setMnemonic(KeyEvent.VK_J);
            } else if (i == 26) {
                fmenu.setMnemonic(KeyEvent.VK_AMPERSAND);
            }
            hieroglyphs.add(fmenu);
        }

        HieroglyphicMenu[] others = new HieroglyphicMenu[] {
                new HieroglyphicMenu("Tall Narrow Signs",
                        HieroglyphicMenu.TALL_NARROW, 6),
                new HieroglyphicMenu("Low Broad Signs",
                        HieroglyphicMenu.LOW_BROAD, 6),
                new HieroglyphicMenu("Low Narrow Signs",
                        HieroglyphicMenu.LOW_NARROW, 6) };
        for (HieroglyphicMenu m : others) {
            hieroglyphs.add(m);
            m.setHieroglyphicMenuListener(mediator);
        }

        // hieroglyphs.setMnemonic(KeyEvent.VK_H);
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

    /**
     * Sets the parent observer which will receive information when the view is
     * modified
     *
     * @param observer
     */
    public void setOwner(ObservableEventListener<TextEvent> observer) {
        this.owner = observer;
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

    /**
     * The JSesh style of the current editor.
     * 
     * @return
     */
    public JSeshStyle getJSeshStyle() {
        return viewComponent.getJSeshStyle();
    }

    /**
     * The render Context of the current editor.
     */

    public JSeshRenderContext getRenderContext() {
        return viewComponent.getRenderContext();
    }

    public void setJSeshStyle(JSeshStyle jSeshStyle) {
        viewComponent.setJSeshStyle(jSeshStyle);
    }

    public void setFontInfo(FontInfo fontInfo) {
        JSeshStyle newStyle = fontInfo.applyApplyToJSeshStyle(getJSeshStyle());
        setJSeshStyle(newStyle);
    }

    public void setJustify(boolean selected) {
        setJSeshStyle(
                getJSeshStyle().copy().options(o -> o.justified(selected)).build());
    }

    public void setTextOrientation(TextOrientation textOrientation) {
        setJSeshStyle(
                getJSeshStyle().copy().options(o -> o.textOrientation(textOrientation)).build());

    }

    public void setTextDirection(TextDirection textDirection) {
        setJSeshStyle(
                getJSeshStyle().copy().options(o -> o.textDirection(textDirection)).build());
    }

    /**
     * Returns the data needed for the graphical export of a selection.
     *
     * @return
     */
    public ExportData getExportData() {
        // Note : there is some doubt over which drawing specifications should
        // be used ?
        return new ExportData(getRenderContext(), getCaret(), 
                getTopItemList(), 1f);
    }

    public void insertCode(String code) {
        getEditor().insert(code);
    }
}
