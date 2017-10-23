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
package jsesh.jhotdraw.viewClass;

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
import jsesh.editor.JMDCEditor;
import jsesh.jhotdraw.actions.BundleHelper;

/**
 * The actual Swing component with all fixed elements for the JSesh view. As
 * usual, this component is passive (it's kind of a drawing). The JSeshViewModel
 * adds the logic.
 *
 * @author rosmord
 */
public final class JSeshViewComponent extends JPanel {

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
     * A menu to choose the zoom factor.
     */
    private final JComboBox zoomComboBox;

    /**
     * The menu to show Gardiner basic glyphs.
     */
    private final JButton hieroglyphsButton;

    public JSeshViewComponent() {
        // simple components.
        editor = new JMDCEditor();
        codeField = new JTextField(5);
        separatorField = new JTextField(1);
        mdcField = new JTextField();
        messageField= new JTextField();
        zoomComboBox= new JComboBox();
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
        JToolBar top = new JToolBar(JToolBar.HORIZONTAL);
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

        JToolBar actualBar = new JToolBar(JToolBar.HORIZONTAL);
        actualBar.setFloatable(false);
        actualBar.add(codeField);
        actualBar.add(separatorField);
        actualBar.add(Box.createHorizontalGlue());
        actualBar.add(messageField);
        actualBar.add(Box.createHorizontalGlue());

        hieroglyphsButton.setBorderPainted(true);
        hieroglyphsButton.setBorder(BorderFactory.createBevelBorder(3));
        actualBar.add(hieroglyphsButton);

        actualBar.add(new JLabel(bundle.getLabel("combobox.zoom.text")));
        actualBar.add(zoomComboBox);
        panel.add(mdcField);
        panel.add(actualBar);
        actualBar.putClientProperty("Quaqua.ToolBar.style", "bottom");
        actualBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        hieroglyphsButton.putClientProperty("Quaqua.Button.style", "push");
        panel.setLayout(new GridLayout(2, 0));
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

    public JComboBox getZoomComboBox() {
        return zoomComboBox;
    }

    public JButton getHieroglyphsButton() {
        return hieroglyphsButton;
    }

}
