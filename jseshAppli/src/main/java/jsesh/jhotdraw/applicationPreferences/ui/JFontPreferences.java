package jsesh.jhotdraw.applicationPreferences.ui;

import jsesh.jhotdraw.Messages;
import jsesh.jhotdraw.applicationPreferences.model.FontInfo;
import jsesh.jhotdraw.utils.FontSelectorComponentGroup;
import jsesh.jhotdraw.utils.PanelBuilder;
import jsesh.mdc.utils.YODChoice;
import jsesh.resources.ResourcesManager;
import net.miginfocom.swing.MigLayout;
import org.qenherkhopeshef.swingUtils.portableFileDialog.FileOperationResult;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialog;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialogFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * font preferences chooser.
 * <p>
 * Allows the choice of default :
 * <ul>
 * <li>hieroglyphic library (more on this later)
 * <li>fonts for non-hieroglyphic text (plain, italic, bold). If these font are
 * unicode font, they can be used for coptic too.
 * <li>fonts for transliteration. Note that choices have to be made here.
 * </ul>
 *
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
public class JFontPreferences {

    // Panel infrastructure
    private JPanel panel;
    private JPanel optionPanelContainer;
    private JPanel transliterationOptionPanel;

    // General font-oriented widgets.
    private FontSelectorComponentGroup alphabeticFontHelper;
    /**
     * Select the use of JSesh default font (old transliteration font).
     */
    private JButton useDefaultJSeshFontButton;

    private FontSelectorComponentGroup transliterationFontHelper;

    private JFormattedTextField hieroglyphsFolderField;
    private JButton browseHieroglyphicFolderButton;

    // Transliteration option manager
    private JButton showOptionButton;
    private boolean optionDisplayed = false;
    private boolean useDefaultJSeshFont = true;

    /**
     * Check to state that the transliteration font is Unicode or MDC.
     */
    private JRadioButton useUnicodeRadioButton;

    private JRadioButton useMdCRadioButton;

    /**
     * Probably the theoretical "best" choice, but doesn't always work with
     * today softwares.
     */
    private JRadioButton yodUsesU0486;

    /**
     * Unusual capital yod, but gives a reasonable rendering with most fonts.
     */
    private JRadioButton yodUsesU0313;

    public JFontPreferences() {
        init();
        layout();
        animate();
        setFont(new Font("Dialog", Font.PLAIN, 12));
        setTranslitFont(new Font("Dialog", Font.PLAIN, 12));
    }

    private void setFont(Font font) {
        alphabeticFontHelper.setFont(font);
    }

    private void setTranslitFont(Font font) {
        transliterationFontHelper.setFont(font);
    }

    private void init() {
        panel = new JPanel();
        alphabeticFontHelper = new FontSelectorComponentGroup(panel,
                "fontPreferences.font.label.text");
        transliterationFontHelper = new FontSelectorComponentGroup(panel,
                "fontPreferences.transliterationFont.label.text");
        useDefaultJSeshFontButton = new JButton(
                Messages.getString("fontPreferences.useDefaultJSeshFontCB.text"));
        hieroglyphsFolderField = new JFormattedTextField(new File("."));
        browseHieroglyphicFolderButton = new JButton(
                Messages.getString("fontPreferences.browseHiero.text"));
        // Transliteration option (expert mode).
        showOptionButton = new JButton(
                Messages.getString("fontPreferences.showOptionButton.show.text"));
        optionPanelContainer = new JPanel();
        transliterationOptionPanel = new JPanel();
        transliterationOptionPanel
                .setBorder(BorderFactory.createTitledBorder(Messages
                        .getString("fontPreferences.transliterationPanel.text")));
        this.useMdCRadioButton = new JRadioButton(
                Messages.getString("fontPreferences.useMdCRadioButton.text"));
        this.useUnicodeRadioButton = new JRadioButton(
                Messages.getString("fontPreferences.useUnicodeRadioButton.text"));
        this.yodUsesU0313 = new JRadioButton(
                Messages.getString("fontPreferences.yodUsesU0313.text"));
        this.yodUsesU0486 = new JRadioButton(
                Messages.getString("fontPreferences.yodUsesU0486.text"));

    }

    private void layout() {
        panel.setLayout(new MigLayout("", "[][grow,fill][][]"));
        PanelBuilder helper = new PanelBuilder(panel);
        helper.addWithLabel("fontPreferences.hiero.label.text",
                hieroglyphsFolderField, "sg a");
        helper.add(browseHieroglyphicFolderButton, "sg b, wrap");
        alphabeticFontHelper.doMigLayout(panel, "sg a, wmin 300pt", "sg b",
                "wrap para");
        transliterationFontHelper.doMigLayout(panel, "sg a, wmin 300pt",
                "sg b", "wrap para");
        helper.add(useDefaultJSeshFontButton, "wrap");

        panel.add(showOptionButton, "wrap");

        optionPanelContainer.setLayout(new MigLayout("wrap 1", "[fill, grow]"));
        panel.add(optionPanelContainer, "grow, spanx 4");

        transliterationOptionPanel.setLayout(new MigLayout());
        PanelBuilder trlHelper = new PanelBuilder(transliterationOptionPanel);
        trlHelper.add(useMdCRadioButton, "wrap");
        trlHelper.add(useUnicodeRadioButton, "wrap para");
        trlHelper.add(yodUsesU0486, "wrap");
        trlHelper.add(yodUsesU0313, "wrap");
    }

    private void animate() {
        ButtonGroup trlEncodingGroup = new ButtonGroup();
        trlEncodingGroup.add(useMdCRadioButton);
        trlEncodingGroup.add(useUnicodeRadioButton);
        ButtonGroup yodGroup = new ButtonGroup();
        yodGroup.add(yodUsesU0313);
        yodGroup.add(yodUsesU0486);
        useMdCRadioButton.addActionListener((e) -> trlChanged());
        useUnicodeRadioButton.addActionListener((e) -> trlChanged());
        showOptionButton.addActionListener(e -> toggleShowOption());
        useDefaultJSeshFontButton.addActionListener(e -> useOldDefaultFont());
        this.transliterationFontHelper.addPropertyChangeListener(FontSelectorComponentGroup.FONT, (e) -> useDefaultJSeshFont = false);
        this.browseHieroglyphicFolderButton.addActionListener(
                e -> selectHieroglyphicFolder());
    }

    /**
     * Displays the selection dialog for hieroglyphs.
     */
    private void selectHieroglyphicFolder() {
        File hieroglyphicFolder = (File) hieroglyphsFolderField.getValue();
        PortableFileDialog selector = PortableFileDialogFactory.createDirectorySaveDialog(panel);
        selector.setTitle(Messages.getString("JFontPreferences.hieroglyphDialog.label"));
        if (hieroglyphicFolder != null) {
            selector.setCurrentDirectory(hieroglyphicFolder);
        }
        if (selector.show() == FileOperationResult.OK) {
            hieroglyphsFolderField.setValue(selector.getSelectedFile());
        }

    }

    /**
     * Use the old JSesh MDC-compatible font, taken from software resources.
     */
    protected void useOldDefaultFont() {
        Font trl = ResourcesManager.getInstance().getTransliterationFont();
        setTranslitFont(trl);
        useMdCRadioButton.setSelected(true);
        useDefaultJSeshFont = true;
    }

    protected void trlChanged() {
        yodUsesU0313.setEnabled(useUnicodeRadioButton.isSelected());
        yodUsesU0486.setEnabled(useUnicodeRadioButton.isSelected());
    }

    protected void toggleShowOption() {
        if (optionDisplayed) {
            optionPanelContainer.remove(transliterationOptionPanel);
            this.showOptionButton.setText(Messages
                    .getString("fontPreferences.showOptionButton.show.text"));
        } else {
            optionPanelContainer.add(transliterationOptionPanel, "growx");
            this.showOptionButton.setText(Messages
                    .getString("fontPreferences.showOptionButton.hide.text"));
        }
        try {
            Window window = (Window) SwingUtilities.getRoot(panel);
            if (window != null) {
                window.pack();
            }
        } catch (ClassCastException e) {
            // DO NOTHING
        }
        optionDisplayed = !optionDisplayed;
    }

    public JPanel getPanel() {
        return panel;
    }

    public FontInfo getFontInfo() {
        FontInfo fontInfo = new FontInfo(
                (File) hieroglyphsFolderField.getValue(),
                alphabeticFontHelper.getSelectedFont(),
                transliterationFontHelper.getSelectedFont());
        fontInfo = fontInfo.withTranslitUnicode(useUnicodeRadioButton
                .isSelected());
        if (yodUsesU0313.isSelected()) {
            fontInfo = fontInfo.withYodChoice(YODChoice.U0313);
        } else if (yodUsesU0486.isSelected()) {
            fontInfo = fontInfo.withYodChoice(YODChoice.U0486);
        }
        fontInfo = fontInfo.withUseEmbeddedFont(useDefaultJSeshFont);
        return fontInfo;
    }

    public void setFontInfo(FontInfo fontInfo) {
        hieroglyphsFolderField.setValue(fontInfo.getHieroglyphsFolder());
        setFont(fontInfo.getBaseFont());
        setTranslitFont(fontInfo.getTransliterationFont());
        if (fontInfo.isTranslitUnicode()) {
            useUnicodeRadioButton.setSelected(true);
        } else {
            useMdCRadioButton.setSelected(true);
        }
        trlChanged();
        switch (fontInfo.getYodChoice()) {
            case U0313:
                yodUsesU0313.setSelected(true);
                break;
            case U0486:
                yodUsesU0486.setSelected(true);
                break;
        }
        if (fontInfo.isUseEmbeddedFont()) {
            useOldDefaultFont();
        } else {
            useDefaultJSeshFont = false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                JFontPreferences prefs = new JFontPreferences();
                frame.add(prefs.getPanel());
                frame.pack();
                frame.setVisible(true);
            }
        });
    }
}
