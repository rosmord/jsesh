package jsesh.jhotdraw;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import jsesh.editor.JMDCEditor;
import jsesh.editor.MDCModelEditionAdapter;
import jsesh.editor.caret.MDCCaret;
import jsesh.hieroglyphs.CompositeHieroglyphsManager;
import jsesh.hieroglyphs.HieroglyphFamily;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.mdc.file.DocumentPreferences;
import jsesh.mdc.file.MDCDocument;
import jsesh.mdc.model.operations.ModelOperation;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.swing.hieroglyphicMenu.HieroglyphicMenu;
import jsesh.swing.hieroglyphicMenu.HieroglyphicMenuListener;

/**
 * An abstract (more or less framework-agnostic) representation of an editing
 * session of a JSesh document. Might be worth merging with JSeshView.
 * 
 * <p>
 * TODO this class is a bit too heavy for my taste... nothing as awful as the
 * old JSesh application, but still...
 * 
 * @author rosmord
 */
public class JSeshViewModel {

	// The view contains
	// a top-view tool bar
	//
	// a bottom bar with
	// an optional MDC zone
	// current code / current sep / message / zoom
	//
	/**
	 * The document we are working on.
	 */
	private MDCDocument mdcDocument;

	/**
	 * The editor to edit the document's text.
	 */
	private JMDCEditor editor;

	/**
	 * Toolbar associated with this element.
	 */
	private JComponent topPanel;
	/**
	 * Panel holding various information.
	 */
	private JPanel bottomPanel;
	/**
	 * Field displaying the code being typed. (a combobox could be a nice idea
	 * here).
	 */
	private JTextField codeField;
	/**
	 * the separator which has been selected (* or :)
	 */
	private JTextField separatorField;
	/**
	 * The MdC for the current "line" (or column).
	 */
	private JTextField mdcField;
	
	/**
	 * A field to display various messages.
	 */
	private JTextField messageField;
	
	/**
	 * Actually, a menu to choose the zoom factor.
	 */
	private JComboBox zoomComboBox;

	private Observer parentObserver;

	private DelegatingObserver delegatingObserver= new DelegatingObserver();
	
	public JSeshViewModel() {		
		editor = new JMDCEditor();
		setCurrentDocument(new MDCDocument());
		// JScrollPane scroll = new JScrollPane(editor);
		// scroll.getVerticalScrollBar().setUnitIncrement(20);
		topPanel = prepareTopPanel();
		bottomPanel = prepareBottomPanel();

		// Activate the objects
		// The code model
		CodeModel codeModel = new CodeModel();
		editor.addCodeChangeListener(codeModel);
		MDCLineManager mdcLineManager = new MDCLineManager();
		editor.getWorkflow().addMDCModelListener(mdcLineManager);
		mdcField.addActionListener(mdcLineManager);
		// Zoom combobox
		DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
		for (int zoom : new int[] { 25, 50, 75, 100, 112, 128, 150, 200, 400,
				600, 800, 1600, 3200, 6400, 12800 }) {
			comboBoxModel.addElement(new ZoomInfo(zoom));
		}
		zoomComboBox.setModel(comboBoxModel);
		zoomComboBox.setSelectedIndex(7);
		zoomComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ZoomInfo zoomInfo = (ZoomInfo) zoomComboBox.getSelectedItem();
				if (zoomInfo != null) {
					editor.setScale(zoomInfo.zoom / 100.0);
				}
			}
		});
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

		codeField = new JTextField(5);
		codeField.setEnabled(false);
		codeField.setFocusable(false);
		codeField.setDisabledTextColor(codeField.getForeground());
		codeField.setToolTipText(bundle.getLabel("codeField.toolTipText"));
		codeField.setMaximumSize(codeField.getPreferredSize());

		separatorField = new JTextField(1);
		separatorField.setEnabled(false);
		separatorField.setFocusable(false);
		separatorField.setMaximumSize(separatorField.getPreferredSize());
		separatorField.setDisabledTextColor(separatorField.getForeground());
		separatorField.setToolTipText(bundle
				.getLabel("separatorField.toolTipText"));

		messageField= new JTextField();
		messageField.setEditable(false);
		messageField.setBorder(BorderFactory.createEmptyBorder());
		
		mdcField = new JTextField();
		mdcField.setToolTipText(bundle.getLabel("mdcField.toolTipText"));

		zoomComboBox = new JComboBox();

		JToolBar actualBar = new JToolBar(JToolBar.HORIZONTAL);
		actualBar.setFloatable(false);
		actualBar.add(codeField);
		actualBar.add(separatorField);
		actualBar.add(Box.createHorizontalGlue());
		actualBar.add(messageField);
		actualBar.add(Box.createHorizontalGlue());

		// The following should be a regular button with a pop-up menu.
		JMenu hieroglyphicMenu = buildHieroglyphicMenus();
		JMenuBar menuBar= new JMenuBar();
		menuBar.add(hieroglyphicMenu);
		menuBar.setMaximumSize(menuBar.getPreferredSize());
		actualBar.add(menuBar);
		
		actualBar.add(new JLabel(bundle.getLabel("combobox.zoom.text")));
		actualBar.add(zoomComboBox);
		panel.add(mdcField);
		panel.add(actualBar);

		panel.setLayout(new GridLayout(2, 0));
		return panel;
	}

	public JMDCEditor getEditor() {
		return editor;
	}

	public MDCDocument getMdcDocument() {
		return mdcDocument;
	}

	public void setCurrentDocument(MDCDocument newDocument) {
		if (mdcDocument != null) {
			mdcDocument.getHieroglyphicTextModel().deleteObserver(delegatingObserver);
		}
		mdcDocument = newDocument;
		mdcDocument.getHieroglyphicTextModel().addObserver(delegatingObserver);
		DocumentPreferences prefs = mdcDocument.getDocumentPreferences();		
		DrawingSpecification ds = editor.getDrawingSpecifications();
		ds.applyDocumentPreferences(prefs);
		editor.setDrawingSpecifications(ds);
		editor.setHieroglyphiTextModel(mdcDocument.getHieroglyphicTextModel());
	}

	public void setEnabled(boolean enabled) {
		editor.setEnabled(enabled);
	}

	public JPanel getBottomPanel() {
		return bottomPanel;
	}

	public JComponent getTopPanel() {
		return topPanel;
	}

	/**
	 * Synchronize the hieroglyphic editor and the optional mdc line display
	 * below it.
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
		}

		public void textEdited(ModelOperation op) {
			updateCurrentMDCLine();
		}

		/**
		 * Mdc field update, performed "later", but still on the EDT.
		 */
		public void updateMdCField() {
			textLineRefreshAsked = true;
			String s = editor.getWorkflow().getCurrentLineAsString();
			mdcField.setText(s);
			textLineRefreshAsked = false;
		}

		/**
		 * Called when the MdC text editor field has been validated. Don't
		 * de-activate the MdC field update : it will show what has been
		 * understood.
		 */
		public void actionPerformed(ActionEvent e) {
			editor.getWorkflow().setCurrentLineTo(mdcField.getText());
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
			separatorField.setText("" + getEditor().getCurrentSeparator());
		}

		public void codeChanged(StringBuffer code) {
			codeField.setText(getEditor().getCodeBuffer());
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
	private JMenu buildHieroglyphicMenus() {
		HieroglyphicMenuMediator mediator = new HieroglyphicMenuMediator();
		List<HieroglyphFamily> families = CompositeHieroglyphsManager
				.getInstance().getFamilies();
		BundleHelper bundle = BundleHelper.getInstance();

		JMenu hieroglyphs = new JMenu(bundle.getLabel("hieroglyphicMenu.text"));

		hieroglyphs.getPopupMenu().setLayout(new GridLayout(14, 2));
		for (int i = 0; i < families.size(); i++) {
			HieroglyphFamily family = (HieroglyphFamily) families.get(i);

			HieroglyphicMenu fmenu = new HieroglyphicMenu(family.getCode()
					+ ". " + family.getDescription(), family.getCode(), 6);

			fmenu.setHieroglyphicMenuListener(mediator);
			if (i < 25)
				fmenu.setMnemonic(family.getCode().toUpperCase().charAt(0));
			else if (i == 25) {
				fmenu.setMnemonic(KeyEvent.VK_J);
			} else if (i == 26)
				fmenu.setMnemonic(KeyEvent.VK_AMPERSAND);
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

		hieroglyphs.setMnemonic(KeyEvent.VK_H);
		return hieroglyphs;
	}

	public void setMessage(String messsage) {
		messageField.setText(messsage);
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
			editor.insert(code);

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
	 * Sets the parent parentObserver which will receive information when the view is modified
	 * @param parentObserver
	 */
	public void setParentObserver(Observer observer) {
		this.parentObserver= observer;
	}
	
	private class DelegatingObserver implements Observer {

		public void update(Observable o, Object arg) {
			parentObserver.update(o, arg);
		}
		
	}
}
