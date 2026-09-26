/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2016) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est un programme informatique servant à mettre en place 
 * une base de données linguistique pour l'égyptien ancien.

 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence [CeCILL|CeCILL-B|CeCILL-C] telle que diffusée par le CEA, le CNRS et l'INRIA 
 * sur le site "http://www.cecill.info".

 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.

 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant 
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à 
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement, 
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité. 

 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez 
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */
package jsesh.jhotdraw.documentview;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import jsesh.render.style.JSeshStyle;
import jsesh.defaults.HieroglyphResources;
import jsesh.document.HieroglyphicTextModel;
import jsesh.ui.editor.JMDCEditor;
import jsesh.ui.editor.JSeshStyleReference;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.render.context.JSeshRenderContext;

import net.miginfocom.swing.MigLayout;

/**
 * The actual Swing component with all fixed elements for the JSesh view. As
 * usual, this component is passive (it's kind of a drawing). The JSeshViewModel
 * adds the logic.
 * @author rosmord
 * 
 * @param <ZOOMINFO>  the type of elements in the zoomComboBox.
 */
public final class JSeshViewComponent<ZOOMINFO> extends JPanel {

    /**
     * The editor to edit the document's text.
     */
    private final JMDCEditor editor;

    /**
     * Toolbar associated with this element.
     */
    private final JComponent topPanel;
    /**
     * Panel holding various information.
     */
    private final JPanel bottomPanel;
    /**
     * Field displaying the code being typed. (a combobox could be a nice idea
     * here).
     */
    private final JTextField codeField;

    /**
     * the separator which has been selected (* or :)
     */
    private final JTextField separatorField;

    /**
     * The MdC for the current "line" (or column).
     */
    private final JTextField mdcField;

    /**
     * A field to display various messages.
     */
    private final JTextField messageField;

    /**
     * Displays the current input mode (hieroglyphs, latin, transliteration...).
     */
    private final JLabel inputModeLabel;

    /**
     * A menu to choose the zoom factor.
     */
    private final JComboBox<ZOOMINFO> zoomComboBox;

    /**
     * The menu to show Gardiner basic glyphs.
     */
    private final JButton hieroglyphsButton;

    public JSeshViewComponent(HieroglyphResources fontKit, JSeshStyle style) {
        // simple components.
        editor = new JMDCEditor(new HieroglyphicTextModel(), new JSeshStyleReference(style), fontKit);
        codeField = new JTextField(5);
        separatorField = new JTextField(1);
        mdcField = new JTextField();
        messageField= new JTextField();
        inputModeLabel= new JLabel();
        zoomComboBox= new JComboBox<>();
        hieroglyphsButton= new JButton();
        // Panels layout
        topPanel = prepareTopPanel();
        bottomPanel = prepareBottomPanel();
        // final layout.
        setLayout(new BorderLayout());
        add(new JScrollPane(editor), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.PAGE_END);
        add(topPanel, BorderLayout.PAGE_START);
    }

    private JComponent prepareTopPanel() {
        JToolBar top = new JToolBar(SwingConstants.HORIZONTAL);
        top.setFloatable(false);

        top.add(Box.createHorizontalGlue());
        return top;
    }

    /**
     * Prepare the bottom panel
     *
     * @return the bottom panel.
     */
    private JPanel prepareBottomPanel() {
        BundleHelper bundle = BundleHelper.getInstance();

        JPanel panel = new JPanel();
        codeField.setEnabled(false);
        codeField.setFocusable(false);
        codeField.setDisabledTextColor(codeField.getForeground());
        codeField.setToolTipText(bundle.getLabel("codeField.toolTipText"));
        codeField.setMaximumSize(codeField.getPreferredSize());

        separatorField.setEnabled(false);
        separatorField.setFocusable(false);
        separatorField.setMaximumSize(separatorField.getPreferredSize());
        separatorField.setDisabledTextColor(separatorField.getForeground());
        separatorField.setToolTipText(bundle
                .getLabel("separatorField.toolTipText"));

        messageField.setEditable(false);
        messageField.setBorder(BorderFactory.createEmptyBorder());

        mdcField.setToolTipText(bundle.getLabel("mdcField.toolTipText"));

        inputModeLabel.setToolTipText(bundle.getLabel("inputModeField.toolTipText"));

        JPanel actualBar = new JPanel(new MigLayout("insets 0"));
        hieroglyphsButton.putClientProperty("JButton.buttonType", "toolBarButton");

        actualBar.add(inputModeLabel, "gapleft rel");
        actualBar.add(codeField, "gapleft unrel");
        actualBar.add(separatorField, "gapleft unrel");
        actualBar.add(messageField, "growx, pushx");
        actualBar.add(hieroglyphsButton, "gapleft unrel");
        actualBar.add(new JLabel(bundle.getLabel("combobox.zoom.text")), "gapleft unrel");
        actualBar.add(zoomComboBox, "gapleft rel, gapright rel");
        panel.setLayout(new GridLayout(2, 0));
        panel.add(mdcField);
        panel.add(actualBar);
        return panel;
    }

    public JMDCEditor getEditor() {
        return editor;
    }

    public JTextField getCodeField() {
        return codeField;
    }

    public JTextField getSeparatorField() {
        return separatorField;
    }

    public JTextField getMdcField() {
        return mdcField;
    }

    public JTextField getMessageField() {
        return messageField;
    }

    public JLabel getInputModeLabel() {
        return inputModeLabel;
    }

    public JComboBox<ZOOMINFO> getZoomComboBox() {
        return zoomComboBox;
    }

    public JButton getHieroglyphsButton() {
        return hieroglyphsButton;
    }

    public JSeshStyle getJSeshStyle() {
        return editor.getJSeshStyle();
    }

    /**
     * The RenderContext of the current editor.
     * @return
     */
    public JSeshRenderContext getRenderContext() {
        return editor.getRenderContext();
    }

    public void setJSeshStyle(JSeshStyle jSeshStyle) {
        editor.setJSeshStyle(jSeshStyle);
    }

}
