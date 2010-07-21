package jsesh.graphics.glyphs.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import jsesh.graphics.glyphs.control.ExternalSignImporterPresenter;
import jsesh.graphics.glyphs.model.ExternalSignImporterModel;
import jsesh.resources.ResourcesManager;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

public class ExternalSignImporterUI {

	private JPanel mainPanel = null;

	private JButton insertButton;

	private JButton closeButton;

	private JButton importButton;

	private JButton nextButton;

	private JButton previousButton;

	private JButton flipVerticallyButton;

	private JButton flipHorizontallyButton;

	private JButton fullCadratHeightButton;

	private JTextField codeField;

	private ShapeDisplayer importedSignDisplay;

	private ShapeDisplayer referenceSignDisplay;

	private UIEventListener uiEventListener;

	private JButton buttons[];

	public ExternalSignImporterUI() {
		initComponents();
		mainPanel = build();
	}

	private void initComponents() {
		importButton = new JButton("Import file", ResourcesManager
				.getInstance().getIcon("Import file"));
		importButton
				.setToolTipText("Import glyph(s) from a truetype font, a bzr file, or an svg drawing");
		closeButton = new JButton("Close");
		insertButton = new JButton("Insert");
		insertButton
				.setToolTipText("Insert the currently displayed sign in the base with the current code");
		codeField = new JTextField(8);
		codeField
				.setToolTipText("Code to give to the new sign. The code should be a correct Gardiner-like code ");
		nextButton = new JButton(ResourcesManager.getInstance().getIcon("Next"));
		previousButton = new JButton(ResourcesManager.getInstance().getIcon(
				"Previous"));
		flipHorizontallyButton = new JButton(ResourcesManager.getInstance()
				.getIcon("Flip sign horizontally"));
		flipVerticallyButton = new JButton(ResourcesManager.getInstance()
				.getIcon("Flip sign vertically"));
		importedSignDisplay = new ShapeDisplayer();

		ResizerListener resizeListener = new ResizerListener();
		importedSignDisplay.addMouseListener(resizeListener);
		importedSignDisplay.addMouseMotionListener(resizeListener);
		referenceSignDisplay = new ShapeDisplayer();

		fullCadratHeightButton = new JButton("Full cadrat Height");
		buttons = new JButton[] { insertButton, closeButton, importButton,
				previousButton, nextButton, flipHorizontallyButton,
				flipVerticallyButton, fullCadratHeightButton };

	}

	private JPanel build() {
		mainPanel = new JPanel();
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				buildDisplayPanel(), buildFormPanel());
		mainPanel.add(split);
		return mainPanel;
	}

	private Component buildFormPanel() {
		FormLayout layout = new FormLayout("pref",
				"pref,pref,pref,pref,pref,pref");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		builder.append(importButton);
		builder.nextLine();
		builder.addSeparator("New sign");
		builder.nextLine();
		builder.append("code :");
		builder.append(codeField);
		builder.nextLine();
		builder.append(insertButton);
		builder.nextRow();
		builder.append(flipHorizontallyButton);
		builder.append(flipVerticallyButton);
		builder.append(fullCadratHeightButton);
		builder.setDefaultDialogBorder();
		return builder.getPanel();

	}

	private JPanel buildDisplayPanel() {
		FormLayout layout = new FormLayout("pref,3dlu,pref", "pref,pref,pref");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		layout.setColumnGroups(new int[][] { { 1, 3 } });
		builder.append("A1 sign");
		builder.append("Current sign");
		builder.nextLine();
		builder.append(getReferenceSignDisplay());
		builder.append(getImportedSignDisplay());
		builder.nextLine();
		builder.nextColumn(2);
		
		JPanel navPanel= new JPanel();
		navPanel.add(getPreviousButton());
		navPanel.add(getNextButton());

		builder.append(navPanel);

		return builder.getPanel();
	}

	public static void main(String[] args) {
		JFrame jf = new JFrame();
		ExternalSignImporterModel model = new ExternalSignImporterModel();
		ExternalSignImporterUI ui = new ExternalSignImporterUI();
		jf.getContentPane().add(ui.getPanel());
		ui.addEventListener(new ExternalSignImporterPresenter(model, ui));
		jf.pack();
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);
	}

	public void addEventListener(UIEventListener listener) {
		for (int i = 0; i < buttons.length; i++) {
			buttons[i].addActionListener(listener);
		}

		CodeFieldListener codeFieldListener = new CodeFieldListener(listener);
		codeField.getDocument().addDocumentListener(codeFieldListener);
		uiEventListener = listener;

	}

	private class CodeFieldListener implements DocumentListener {
		ActionListener listener;

		public CodeFieldListener(ActionListener listener) {
			super();
			this.listener = listener;
		}

		public void changedUpdate(DocumentEvent e) {
			listener.actionPerformed(new ActionEvent(codeField, 0, ""));
		}

		public void insertUpdate(DocumentEvent e) {
			listener.actionPerformed(new ActionEvent(codeField, 0, ""));
		}

		public void removeUpdate(DocumentEvent e) {
			listener.actionPerformed(new ActionEvent(codeField, 0, ""));
		}

	}

	/**
	 * @return Returns the buttons.
	 */
	public JButton[] getButtons() {
		return buttons;
	}

	/**
	 * @return Returns the closeButton.
	 */
	public JButton getCloseButton() {
		return closeButton;
	}

	/**
	 * @return Returns the codeField.
	 */
	public JTextField getCodeField() {
		return codeField;
	}

	/**
	 * @return Returns the importButton.
	 */
	public JButton getImportButton() {
		return importButton;
	}

	/**
	 * @return Returns the insertButton.
	 */
	public JButton getInsertButton() {
		return insertButton;
	}

	/**
	 * @return Returns the nextButton.
	 */
	public JButton getNextButton() {
		return nextButton;
	}

	/**
	 * @return the full cadrat height button.
	 */
	public JButton getFullCadratHeightButton() {
		return fullCadratHeightButton;
	}

	/**
	 * @return Returns the previousButton.
	 */
	public JButton getPreviousButton() {
		return previousButton;
	}

	/**
	 * @return Returns the flipHorizontallyButton.
	 */
	public JButton getFlipHorizontallyButton() {
		return flipHorizontallyButton;
	}

	/**
	 * @return Returns the flipVerticallyButton.
	 */
	public JButton getFlipVerticallyButton() {
		return flipVerticallyButton;
	}

	/**
	 * @return Returns the importedSignDisplay.
	 */
	public ShapeDisplayer getImportedSignDisplay() {
		return importedSignDisplay;
	}

	/**
	 * @return Returns the referenceSignDisplay.
	 */
	public ShapeDisplayer getReferenceSignDisplay() {
		return referenceSignDisplay;
	}

	public JPanel getPanel() {
		return mainPanel;
	}

	private class ResizerListener extends MouseAdapter implements
			MouseMotionListener {


		public void mouseClicked(MouseEvent e) {
			if (importedSignDisplay.inResizeArea(e.getX(), e.getY())) {
			}
		}

		public void mouseDragged(MouseEvent e) {
			double y = importedSignDisplay.convertToModelY(e.getY());
			uiEventListener.resizeVerticallyTo(y);

		}

		public void mouseMoved(MouseEvent e) {

		}

	}
}
