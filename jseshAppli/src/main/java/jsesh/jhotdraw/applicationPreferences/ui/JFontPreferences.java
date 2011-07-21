package jsesh.jhotdraw.applicationPreferences.ui;

import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import jsesh.jhotdraw.Messages;
import jsesh.jhotdraw.utils.FontSelectorHelper;
import jsesh.jhotdraw.utils.PanelHelper;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import net.miginfocom.swing.MigLayout;

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
	private FontSelectorHelper alphabeticFontHelper;
	private JFormattedTextField hieroglyphsFolderField;
	private JButton browseHieroglyphicFolderButton;

	// Transliteration option manager
	private JButton showOptionButton;
	private boolean optionDisplayed = false;
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

	//
	// private JRadioButton useIFAOEncodingRadioButton;

	private Font font;

	public JFontPreferences() {
		init();
		layout();
		animate();
		setFont(font);
	}

	private void setFont(Font font) {
		this.font = font;
		alphabeticFontHelper.setFont(font);
	}

	private void init() {
		font = new java.awt.Font("Serif", Font.PLAIN, 12);
		panel = new JPanel();
		alphabeticFontHelper = new FontSelectorHelper(panel,
				"fontPreferences.font.label.text");

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
		PanelHelper helper = new PanelHelper(panel);
		helper.addWithLabel("fontPreferences.hiero.label.text",
				hieroglyphsFolderField, "sg a");
		helper.add(browseHieroglyphicFolderButton, "sg b, wrap");
		alphabeticFontHelper.doMigLayout(panel, "sg a, wmin 300pt", "sg b",
				"wrap para");
		panel.add(showOptionButton, "wrap");

		optionPanelContainer.setLayout(new MigLayout("wrap 1", "[fill, grow]"));
		panel.add(optionPanelContainer, "grow, spanx 4");

		transliterationOptionPanel.setLayout(new MigLayout());
		PanelHelper trlHelper = new PanelHelper(transliterationOptionPanel);
		trlHelper.add(useMdCRadioButton, "wrap");
		trlHelper.add(useUnicodeRadioButton, "wrap para");
		trlHelper.add(yodUsesU0486, "wrap");
		trlHelper.add(yodUsesU0313, "wrap");
	}

	private void animate() {
		ButtonGroup trlEncodingGroup= new ButtonGroup();
		trlEncodingGroup.add(useMdCRadioButton);
		trlEncodingGroup.add(useUnicodeRadioButton);
		ButtonGroup yodGroup= new ButtonGroup();
		yodGroup.add(yodUsesU0313);
		yodGroup.add(yodUsesU0486);
		useMdCRadioButton.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				trlChanged();
			}
		});
		useUnicodeRadioButton.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				trlChanged();
			}
		});
		showOptionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggleShowOption();
			}
		});
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
			if (window != null) window.pack();
		} catch (ClassCastException e) {
			// DO NOTHING
		}
		optionDisplayed = !optionDisplayed;
	}

	public JPanel getPanel() {
		return panel;
	}

	public void updatePreferences(DrawingSpecification drawingSpecification) {
		
	}
	
	public void loadPreferences(DrawingSpecification drawingSpecification) {
		
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
