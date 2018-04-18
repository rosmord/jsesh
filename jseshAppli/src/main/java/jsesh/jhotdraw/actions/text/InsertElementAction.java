package jsesh.jhotdraw.actions.text;

import java.awt.event.ActionEvent;
import static javax.swing.Action.SMALL_ICON;

import jsesh.editor.JMDCEditor;
import jsesh.jhotdraw.viewClass.JSeshView;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.mdc.constants.LexicalSymbolsUtils;
import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElement;
import jsesh.swing.utils.ImageIconFactory;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;

/**
 * A simple class for easy text addition.
 * <p>We are not going to addAll all text addition commands to the editor object
 * itself, so this class allows application-level actions for this task. A
 * graphic representation of the text to addAll will be used as icon.
 *
 * @author S. Rosmorduc
 */
@SuppressWarnings("serial")
public class InsertElementAction extends AbstractViewAction {

    private ModelElement element;

    /**
     * Create an action for a given element.
     *
     * @param app
     * @param view
     * @param element a prototype of the element to addAll.
     * @param mdcTextForIcon the text of the icon which should be displayed
     * (which can be more than the element).
     */
    public InsertElementAction(Application app, View view, ModelElement element, String mdcTextForIcon) {
        super(app, view);
        this.element = element.deepCopy();
        putValue(SMALL_ICON, ImageIconFactory.getInstance().buildImage(mdcTextForIcon));
    }

    /**
     * Create an action for adding a particular glyph, with a specific numeric
     * code.
     *
     * @param app
     * @param view
     * @param glyphCode see {@link SymbolCodes}
     * @param mdcTextForIcon the mdc for the icon to display.
     */
    public InsertElementAction(Application app, View view, int glyphCode, String mdcTextForIcon) {
        this(app, view, new Hieroglyph(glyphCode), LexicalSymbolsUtils.getStringForLexicalItem(glyphCode));
    }

    /**
     * Build icon-less insert element action.
     *
     * @param app
     * @param view
     * @param id the action id, used to configure the action.
     * @param glyphCode see {@link SymbolCodes}
     */
    public InsertElementAction(Application app, View view, String id, int glyphCode) {
        this(app, view, new Hieroglyph(glyphCode), LexicalSymbolsUtils.getStringForLexicalItem(glyphCode));
        BundleHelper.getInstance().configure(this, id);
    }

    /**
     * Named constructor for insert element action with the symbol as icon.
     *
     * @param app
     * @param view
     * @param id the action id, used to configure the action.
     * @param symbolCode see {@link SymbolCodes}
     * @return an action for inserting this element.
     */
    public static InsertElementAction buildInsertElementActionWithIcon(Application app, View view, String id, int symbolCode) {
        InsertElementAction action = new InsertElementAction(app, view, id, symbolCode);
        action.putValue(SMALL_ICON, ImageIconFactory.getInstance().buildImage(symbolCode));
        return action;
    }

    /**
     * Build insert element action for a certain Manuel de codage code.
     *
     * @param app
     * @param view
     * @param mdc the manuel de codage code for the glyph we want.
     * @return 
     */
    public static InsertElementAction buildInsertElementActionWithIcon(Application app, View view, String mdc) {
        ModelElement elt = new Hieroglyph(mdc);
        InsertElementAction action = new InsertElementAction(app, view, elt, mdc);
        return action;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JSeshView jSeshView = (JSeshView) getActiveView();
        JMDCEditor editor = jSeshView.getEditor();
        if (editor.isEditable()) {
            editor.getWorkflow().insertElement(element);
        }
    }
}