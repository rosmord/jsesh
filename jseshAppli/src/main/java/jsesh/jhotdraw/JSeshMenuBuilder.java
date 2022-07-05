package jsesh.jhotdraw;

import jsesh.jhotdraw.viewClass.JSeshView;

import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import jsesh.editor.ActionsID;
import jsesh.editor.actions.sign.EditorSignRotationAction;
import jsesh.editor.actions.sign.EditorSignSizeAction;
import jsesh.editor.actions.text.AddPhilologicalMarkupAction;
import jsesh.editor.actions.text.EditorCartoucheAction;
import jsesh.editor.actions.text.EditorShadeAction;
import jsesh.editor.actions.text.EditorSignShadeAction;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.actions.JSeshApplicationActionsID;
import jsesh.jhotdraw.actions.edit.AddToGlossaryAction;
import jsesh.jhotdraw.actions.edit.FindInFolderAction;
import jsesh.jhotdraw.actions.edit.InsertNextLineNumberAction;
import jsesh.jhotdraw.actions.edit.InsertShortTextAction;
import jsesh.jhotdraw.actions.edit.SelectCopyPasteConfigurationAction;
import jsesh.jhotdraw.actions.file.ApplyModelAction;
import jsesh.jhotdraw.actions.file.EditDocumentPreferencesAction;
import jsesh.jhotdraw.actions.file.ExportAsBitmapAction;
import jsesh.jhotdraw.actions.file.ExportAsHTMLAction;
import jsesh.jhotdraw.actions.file.ExportAsPDFAction;
import jsesh.jhotdraw.actions.file.ExportAsRTFAction;
import jsesh.jhotdraw.actions.file.ImportNewSignAction;
import jsesh.jhotdraw.actions.file.ImportPDFAction;
import jsesh.jhotdraw.actions.file.ImportRTFAction;
import jsesh.jhotdraw.actions.file.QuickPDFExportAction;
import jsesh.jhotdraw.actions.file.QuickPDFSelectExportFolderAction;
import jsesh.jhotdraw.actions.file.SetAsModelAction;
import jsesh.jhotdraw.actions.format.*;
import jsesh.jhotdraw.actions.help.JSeshHelpAction;
import jsesh.jhotdraw.actions.text.EditGroupAction;
import jsesh.jhotdraw.actions.windows.ToggleGlossaryEditorAction;
import jsesh.jhotdraw.actions.windows.ToggleGlyphPaletteAction;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.DefaultMenuBuilder;
import org.jhotdraw_7_6.app.View;

public class JSeshMenuBuilder extends DefaultMenuBuilder {

    @Override
    public void addOtherMenus(List<JMenu> menus, Application app, View v) {
        if (v != null) {
            menus.add(createTextMenu(app, (JSeshView) v));
            menus.add(createSignMenu(app, (JSeshView) v));
        }
    }

    private JMenu createTextMenu(Application a, JSeshView v) {
        JMenu textMenu = new JMenu();
        BundleHelper.getInstance().configure(textMenu, "text");
        addToMenu(textMenu, a, v, ActionsID.GROUP_HORIZONTAL);
        addToMenu(textMenu, a, v, ActionsID.GROUP_VERTICAL);
        addToMenu(textMenu, a, v, ActionsID.LIGATURE_ELEMENTS);
        addToMenu(textMenu, a, v, ActionsID.LIGATURE_GROUP_WITH_GLYPH);
        addToMenu(textMenu, a, v, ActionsID.LIGATURE_GLYPH_WITH_GROUP);
        addToMenu(textMenu, a, v, ActionsID.EXPLODE_GROUP);
        textMenu.addSeparator();
        addToMenu(textMenu, a, v, EditGroupAction.ID);
        textMenu.addSeparator();
        addToMenu(textMenu, a, v, ActionsID.INSERT_SPACE);
        addToMenu(textMenu, a, v, ActionsID.INSERT_HALF_SPACE);
        addToMenu(textMenu, a, v, ActionsID.NEW_PAGE);
        addToMenu(textMenu, a, v, ActionsID.INSERT_RED_POINT);
        addToMenu(textMenu, a, v, ActionsID.INSERT_BLACK_POINT);

        // I am not sure it is interesting to give names to those actions...
        // addAll the import and export as menus...
        JMenu shadingSymbolsMenu = BundleHelper.getInstance().configure(
                new JMenu(), "text.shadingSymbols");
        addToMenu(shadingSymbolsMenu, a, v,
                JSeshApplicationActionsID.INSERT_FULL_SHADING);
        addToMenu(shadingSymbolsMenu, a, v,
                JSeshApplicationActionsID.INSERT_HORIZONTAL_SHADING);
        addToMenu(shadingSymbolsMenu, a, v,
                JSeshApplicationActionsID.INSERT_VERTICAL_SHADING);
        addToMenu(shadingSymbolsMenu, a, v,
                JSeshApplicationActionsID.INSERT_QUARTER_SHADING);

        textMenu.add(shadingSymbolsMenu);
        textMenu.addSeparator();
        addToMenu(textMenu, a, v, ActionsID.SHADE_ZONE);
        addToMenu(textMenu, a, v, ActionsID.UNSHADE_ZONE);
        addToMenu(textMenu, a, v, ActionsID.RED_ZONE);
        addToMenu(textMenu, a, v, ActionsID.BLACK_ZONE);
        textMenu.addSeparator();
        textMenu.add(buildShadingMenu(a, v));
        textMenu.add(buildCartoucheMenu(a, v));
        textMenu.add(buildEcdoticMenu(a, v));
        return textMenu;
    }

    private JMenu createSignMenu(Application a, JSeshView v) {
        JMenu menu = new JMenu();
        BundleHelper.getInstance().configure(menu, "sign");
        addToMenu(menu, a, v, ActionsID.REVERSE_SIGN);
        // Size...
        JMenu sizeMenu = new JMenu();
        BundleHelper.getInstance().configure(sizeMenu, "sign.size");
        for (String sizeActionName : EditorSignSizeAction.actionNames) {
            sizeMenu.add(a.getActionMap(v).get(sizeActionName));
        }
        menu.add(sizeMenu);
        // Rotation...
        JMenu rotationMenu = new JMenu();
        BundleHelper.getInstance().configure(rotationMenu, "sign.rotate");
        for (String actionName : EditorSignRotationAction.getActionNames()) {
            rotationMenu.add(a.getActionMap(v).get(actionName));
        }
        menu.add(rotationMenu);
        // Shading...
        JMenu shadingMenu = BundleHelper.getInstance().configure(new JMenu(),
                "sign.shadingMenu");
        shadingMenu.setMnemonic(KeyEvent.VK_H);
        JPopupMenu pm = shadingMenu.getPopupMenu();
        pm.setLayout(new GridLayout(0, 4));
        for (String s : EditorSignShadeAction.getActionNames()) {
            shadingMenu.add(a.getActionMap(v).get(s));
        }
        // Others...
        menu.add(shadingMenu);
        menu.addSeparator();
        addToMenu(menu, a, v, ActionsID.TOGGLE_SIGN_IS_RED);
        addToMenu(menu, a, v, ActionsID.TOGGLE_SIGN_IS_WIDE);
        addToMenu(menu, a, v, ActionsID.TOGGLE_IGNORED_SIGN);
        menu.addSeparator();
        addToMenu(menu, a, v, ActionsID.SIGN_IS_INSIDE_WORD);
        addToMenu(menu, a, v, ActionsID.SIGN_IS_WORD_END);
        addToMenu(menu, a, v, ActionsID.SIGN_IS_SENTENCE_END);
        addToMenu(menu, a, v, ActionsID.TOGGLE_GRAMMAR);

        return menu;
    }

    /**
     * Optional action addition. Only addAll an action to the corresponding menu
     * if the action exists.
     *
     * @param menu
     * @param a
     * @param v
     * @param actionID
     */
    private void addToMenu(JMenu menu, Application a, JSeshView v,
            String actionID) {
        Action action = a.getActionMap(v).get(actionID);
        if (action != null) {
            menu.add(action);
        }
    }

    @Override
    public void addOtherFileItems(JMenu fileMenu, Application app, View view) {
        ActionMap map = app.getActionMap(view);
        JSeshView jSeshView = (JSeshView) view;

        // addAll the import and export as menus...
        JMenu importMenu = BundleHelper.getInstance().configure(new JMenu(),
                "file.import");
        importMenu.add(map.get(ImportPDFAction.ID));
        importMenu.add(map.get(ImportRTFAction.ID));

        JMenu exportMenu = BundleHelper.getInstance().configure(new JMenu(),
                "file.export");
        addToMenu(exportMenu, app, jSeshView, ExportAsBitmapAction.ID);
        addToMenu(exportMenu, app, jSeshView,
                JSeshApplicationActionsID.EXPORT_WMF);
        addToMenu(exportMenu, app, jSeshView,
                JSeshApplicationActionsID.EXPORT_EMF);
        addToMenu(exportMenu, app, jSeshView,
                JSeshApplicationActionsID.EXPORT_MACPICT);
        addToMenu(exportMenu, app, jSeshView,
                JSeshApplicationActionsID.EXPORT_EPS);
        addToMenu(exportMenu, app, jSeshView,
                JSeshApplicationActionsID.EXPORT_SVG);
        addToMenu(exportMenu, app, jSeshView, ExportAsPDFAction.ID);
        addToMenu(exportMenu, app, jSeshView, ExportAsRTFAction.ID);
        addToMenu(exportMenu, app, jSeshView, ExportAsHTMLAction.ID);
        addToMenu(exportMenu, app, jSeshView, QuickPDFExportAction.ID);
        addToMenu(exportMenu, app, jSeshView,
                QuickPDFSelectExportFolderAction.ID);

        fileMenu.add(importMenu);
        fileMenu.add(exportMenu);
        fileMenu.addSeparator();
        addToMenu(fileMenu, app, jSeshView, SetAsModelAction.ID);
        addToMenu(fileMenu, app, jSeshView, ApplyModelAction.ID);
        fileMenu.addSeparator();
        addToMenu(fileMenu, app, jSeshView, EditDocumentPreferencesAction.ID);

        if (view != null) {
            JMenu documentFormat;
            documentFormat = buildFormatMenu(app, jSeshView);
            fileMenu.add(documentFormat);
        }
        fileMenu.addSeparator();
        addToMenu(fileMenu, app, jSeshView, ImportNewSignAction.ID);
    }

    /**
     * Build the Menu for document format.
     * <p>
     * This is a rather complex menu, with specific types of buttons (and a
     * button group in one case).
     *
     * @param app
     * @param jSeshView
     * @return
     */
    private JMenu buildFormatMenu(Application app, JSeshView jSeshView) {
        JMenu documentFormat;
        documentFormat = BundleHelper.getInstance().configure(new JMenu(),
                "format");

        /**
         * TODO Add the actions to the map...
         */
        ButtonGroup orientationGroup = new ButtonGroup();

        JRadioButtonMenuItem horizontalButton = new JRadioButtonMenuItem(
                new SetDocumentOrientationAction(app, jSeshView,
                        TextOrientation.HORIZONTAL));
        JRadioButtonMenuItem verticalButton = new JRadioButtonMenuItem(
                new SetDocumentOrientationAction(app, jSeshView,
                        TextOrientation.VERTICAL));
        orientationGroup.add(horizontalButton);
        orientationGroup.add(verticalButton);
        documentFormat.add(horizontalButton);
        documentFormat.add(verticalButton);

        documentFormat.addSeparator();
        ButtonGroup directionGroup = new ButtonGroup();

        JRadioButtonMenuItem leftToRightButton = new JRadioButtonMenuItem(
                new SetDocumentDirectionAction(app, jSeshView,
                        TextDirection.LEFT_TO_RIGHT));
        JRadioButtonMenuItem rightToLeftButton = new JRadioButtonMenuItem(
                new SetDocumentDirectionAction(app, jSeshView,
                        TextDirection.RIGHT_TO_LEFT));
        directionGroup.add(leftToRightButton);
        directionGroup.add(rightToLeftButton);
        documentFormat.add(leftToRightButton);
        documentFormat.add(rightToLeftButton);
        documentFormat.addSeparator();

        JCheckBoxMenuItem centeredMenuItem = new JCheckBoxMenuItem(
                new CenterSmallSignsAction(app, jSeshView));
        documentFormat.add(centeredMenuItem);

        JCheckBoxMenuItem justifyMenuItem = new JCheckBoxMenuItem(
                new ToggleJustifyAction(app, jSeshView));
        documentFormat.add(justifyMenuItem);
        return documentFormat;
    }


    @Override
    public void addFindItems(JMenu m, Application app, View v) {
        super.addFindItems(m, app, v);
        m.add(app.getActionMap(null).get(FindInFolderAction.ID));
    }

    @Override
    public void addOtherEditItems(JMenu editMenu, Application app, View view) {
        // On all windows with a proper menu
        // editMenu.addSeparator(); (no need/too much space)
        // Only on view windows...
        if (view != null) {
            editMenu.addSeparator();
            JMenu copyAsMenu = BundleHelper.getInstance().configure(new JMenu(),
                    "edit.copyAs");
            ActionMap map = app.getActionMap(view);
            copyAsMenu.add(map.get(ActionsID.COPY_AS_PDF));
            copyAsMenu.add(map.get(ActionsID.COPY_AS_RTF));
            copyAsMenu.add(map.get(ActionsID.COPY_AS_BITMAP));
            copyAsMenu.add(map.get(ActionsID.COPY_AS_MDC));
            copyAsMenu.add(map.get(ActionsID.COPY_AS_UNICODE));
            copyAsMenu.add(map.get(ActionsID.COPY_AS_UNICODE_WITH_FORMAT_CONTROLS));

            editMenu.add(copyAsMenu);
            editMenu.addSeparator();
            JMenu modeMenu = BundleHelper.getInstance().configure(new JMenu(), "edit.modeMenu");
            
            modeMenu.add(map.get(ActionsID.SET_MODE_HIEROGLYPHS));
            modeMenu.add(map.get(ActionsID.SET_MODE_LATIN));
            modeMenu.add(map.get(ActionsID.SET_MODE_ITALIC));
            modeMenu.add(map.get(ActionsID.SET_MODE_BOLD));
            modeMenu.add(map.get(ActionsID.SET_MODE_TRANSLIT));
            modeMenu.add(map.get(ActionsID.SET_MODE_UPPERCASE_TRANSLIT));
            modeMenu.add(map.get(ActionsID.SET_MODE_LINENUMBER));
            modeMenu.add(map.get(InsertNextLineNumberAction.ID));
            modeMenu.add(map.get(InsertShortTextAction.ID));
            editMenu.add(modeMenu);
            
            editMenu.add(map.get(AddToGlossaryAction.ID));
            editMenu.addSeparator();
            ButtonGroup group = new ButtonGroup();
            boolean isFirst = true;
            for (String s : SelectCopyPasteConfigurationAction
                    .getSelectCopyPasteConfigurationActionNames()) {
                JRadioButtonMenuItem selectConfigButton = new JRadioButtonMenuItem(
                        map.get(s));
                group.add(selectConfigButton);
                editMenu.add(selectConfigButton);
                selectConfigButton.setSelected(isFirst);
                isFirst = false;
            }
        }
    }

    /**
     * Build a menu for shading quadrants.
     *
     * @param v
     * @param a
     * @param menubar
     */
    private JMenu buildShadingMenu(Application a, JSeshView v) {
        JMenu shadingMenu = BundleHelper.getInstance().configure(new JMenu(),
                "text.shadingMenu");
        shadingMenu.setMnemonic(KeyEvent.VK_H);
        JPopupMenu pm = shadingMenu.getPopupMenu();
        pm.setLayout(new GridLayout(0, 4));
        for (String s : EditorShadeAction.getActionNames()) {
            shadingMenu.add(a.getActionMap(v).get(s));
        }
        return shadingMenu;
    }

    /**
     * @param menubar
     * @return
     */
    private JMenu buildCartoucheMenu(Application a, JSeshView v) {
        JMenu cartoucheMenu = BundleHelper.getInstance().configure(new JMenu(),
                "text.cartoucheMenu");

        cartoucheMenu.setMnemonic(KeyEvent.VK_C);
        JPopupMenu pm = cartoucheMenu.getPopupMenu();
        pm.setLayout(new GridLayout(0, 4));
        for (String s : EditorCartoucheAction.actionNames) {
            cartoucheMenu.add(a.getActionMap(v).get(s));
        }
        return cartoucheMenu;
    }

    /**
     * Build menu for Ecdotic/Philological marks.
     *
     * @param a
     * @param v
     * @return
     */
    private JMenu buildEcdoticMenu(Application a, JSeshView v) {
        JMenu ecdoticMenu = BundleHelper.getInstance().configure(new JMenu(),
                "text.ecdoticMenu");

        JPopupMenu pm = ecdoticMenu.getPopupMenu();
        pm.setLayout(new GridLayout(0, 4));
        for (String s : AddPhilologicalMarkupAction.philologyActionNames) {
            ecdoticMenu.add(a.getActionMap(v).get(s));
        }
        // Limits should be taken from Symbol codes class
        for (int i = 100; i <= 113; i++) {
            ecdoticMenu.add(a.getActionMap(v).get(
                    JSeshApplicationModel.INSERT_CODE + i));
        }
        for (String s : AdditionalSymbols.ARROWS) {
            ecdoticMenu.add(a.getActionMap(v).get(JSeshApplicationModel.INSERT_CODE + s));
        }
        return ecdoticMenu;
    }

    @Override
    public void addHelpItems(JMenu m, Application app, View v) {
        m.add(app.getActionMap(v).get(JSeshHelpAction.ID));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jhotdraw_7_6.app.DefaultMenuBuilder#addOtherWindowItems(javax.swing
     * .JMenu, org.jhotdraw_7_6.app.Application, org.jhotdraw_7_6.app.View)
     */
    @Override
    public void addOtherWindowItems(JMenu m, Application app, View v) {
        m.addSeparator();
        m.add(new JCheckBoxMenuItem(app.getActionMap(v).get(
                ToggleGlossaryEditorAction.ID)));
        m.add(new JCheckBoxMenuItem(app.getActionMap(v).get(
                ToggleGlyphPaletteAction.ID)));
    }

}
