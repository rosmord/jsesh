package jsesh.jhotdraw;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import jsesh.editor.JMDCEditor;
import jsesh.editor.MDCModelEditionAdapter;
import jsesh.editor.caret.MDCCaret;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.mdc.file.MDCDocument;
import jsesh.mdc.model.operations.ModelOperation;

/**
 * An abstract (more or less framework-agnostic) representation of an editing
 * session of a JSesh document. Might be worth merging with JSeshView.
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
	private MDCDocument mdcDocument;
	private JMDCEditor editor;
	private JPanel bottomPanel;
	private JTextField codeField;
	private JTextField separatorField;
	private JTextField mdcField;
	private JComboBox zoomComboBox;

	public JSeshViewModel() {
		mdcDocument = new MDCDocument();
		editor = new JMDCEditor(mdcDocument.getHieroglyphicTextModel());
		// JScrollPane scroll = new JScrollPane(editor);
		// scroll.getVerticalScrollBar().setUnitIncrement(20);
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
				600, 800, 1600 }) {
			comboBoxModel.addElement(new ZoomInfo(zoom));
		}
		zoomComboBox.setModel(comboBoxModel);
		zoomComboBox.setSelectedIndex(7);
		zoomComboBox.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				ZoomInfo zoomInfo= (ZoomInfo) zoomComboBox.getSelectedItem();
				if (zoomInfo != null) {
					editor.setScale(zoomInfo.zoom / 100.0);
				}
			}
		});
	}

	/**
	 * Prepare the bottom panel
	 * 
	 * @return
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
		separatorField.setToolTipText("Manuel de codage separator to use");

		mdcField = new JTextField();
		mdcField.setToolTipText("Here you can edit the manuel de codage code for the current line.");

		zoomComboBox = new JComboBox();

		JToolBar actualBar = new JToolBar(JToolBar.HORIZONTAL);
		actualBar.setFloatable(false);
		actualBar.add(codeField);
		actualBar.add(separatorField);
		actualBar.add(Box.createHorizontalGlue());
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

	public void setCurrentDocument(MDCDocument doc) {
		mdcDocument = doc;
		editor.setHieroglyphiTextModel(mdcDocument.getHieroglyphicTextModel());
		editor.setTextDirection(mdcDocument.getMainDirection());
		editor.setTextOrientation(mdcDocument.getMainOrientation());
	}

	public void setEnabled(boolean enabled) {
		editor.setEnabled(enabled);
	}

	public JPanel getBottomPanel() {
		return bottomPanel;
	}

	/**
	 * Synchronize the hieroglyphic editor and the optional mdc line display
	 * below it.
	 * 
	 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
	 * 
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
}
