package jsesh.mdcDisplayer.swing.editorApplication;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import jsesh.editor.JMDCEditor;
import jsesh.editor.MDCModelEditionAdapter;
import jsesh.editor.MDCModelEditionListener;
import jsesh.editor.caret.MDCCaret;
import jsesh.mdc.model.operations.ModelOperation;
import jsesh.swingUtils.StringDisplayer;

/**
 * Graphical interface for MDC displayer.
 * 
 * Key listener added just for testing purposes. It should move in a CONTROL
 * layer.
 * 
 * This file is free Software (c) Serge Rosmorduc
 * 
 * @author rosmord
 */

public class MDCEditorPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2418383359483811588L;

	JMDCEditor editor;

	// StringDisplayer codeField;
	JTextField codeField;

	// StringDisplayer separatorField;
	JTextField separatorField;

	StringDisplayer messageField;

	JProgressBar progressBar;

	JTextField mdcField;

	private boolean textLineRefreshAsked = false;

	// MDCDisplayerAppliWorkflow workflow;

	/**
	 * Constructor for MDCDisplayerAppliFrame.
	 * 
	 * @param title
	 * @param workflow
	 * 
	 */
	public MDCEditorPanel()  {
		
		
		editor = new JMDCEditor();
		editor.setDrawingSpecifications(null);
		editor.setMdcModelTransferableBroker(null);

		// The code model
		CodeModel codeModel = new CodeModel();
		editor.addCodeChangeListener(codeModel);

		// codeField = new StringDisplayer(codeModel, 5);
		codeField = new JTextField(5);
		codeField.setEnabled(false);
		codeField.setFocusable(false);
		codeField.setDisabledTextColor(codeField.getForeground());
		codeField.setToolTipText("current Manuel de codage code being typed");

		// separatorField= new StringDisplayer(separatorModel, 1);
		separatorField = new JTextField(1);
		separatorField.setEnabled(false);
		separatorField.setFocusable(false);
		separatorField.setDisabledTextColor(separatorField.getForeground());
		separatorField.setToolTipText("Manuel de codage separator to use");

		JScrollPane scroll = new JScrollPane(editor);
		// scroll.
		// scroll.getHorizontalScrollBar().setFocusable(false);
		// scroll.getVerticalScrollBar().setFocusable(false);

		//messageField = new StringDisplayer(workflow.getMessage(), 30);
		//messageField.setToolTipText("Various messages");
		progressBar = new JProgressBar();
		progressBar.setToolTipText("Progress bar for future uses.");

		JSplitPane bottom2 = new JSplitPane();
		JSplitPane bottom1 = new JSplitPane();
		JSplitPane bottom3 = new JSplitPane();

		bottom2.setLeftComponent(messageField);
		bottom2.setRightComponent(progressBar);
		bottom3.setRightComponent(bottom2);
		bottom3.setLeftComponent(separatorField);
		bottom1.setLeftComponent(codeField);
		bottom1.setRightComponent(bottom3);

		mdcField = new JTextField();
		mdcField
				.setToolTipText("Here you can edit the manuel de codage code for the current line.");
		JPanel bottomPanel = new JPanel();
		editor.getWorkflow().addMDCModelListener(new MDCModelEditionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see jsesh.editor.MDCModelEditionAdapter#caretChanged(jsesh.mdcDisplayer.draw.MDCCaret)
			 */
			public void caretChanged(MDCCaret caret) {
				updateCurrentMDCLine();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see jsesh.editor.MDCModelEditionAdapter#textChanged()
			 */
			public void textChanged() {
				updateCurrentMDCLine();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see jsesh.editor.MDCModelEditionAdapter#textEdited(jsesh.mdc.model.operations.ModelOperation)
			 */
			public void textEdited(ModelOperation op) {
				updateCurrentMDCLine();
			}
		});
		mdcField.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setCurrentMDCLine();
			}

		});

		bottomPanel.add(mdcField);
		bottomPanel.add(bottom1);

		bottomPanel.setLayout(new GridLayout(2, 0));
		// layout :
		setLayout(new BorderLayout());
		add(scroll, BorderLayout.CENTER);
		// getContentPane().add(code, BorderLayout.SOUTH);
		add(bottomPanel, BorderLayout.SOUTH);

		setSize(800, 600);

	}

	/**
	 * @return Returns the editor.
	 */
	public JMDCEditor getEditor() {
		return editor;
	}

	private void setCurrentMDCLine() {
		editor.getWorkflow().setCurrentLineTo(mdcField.getText());

	}

	/**
	 * Update the displayed Manuel de codage line.
	 */
	private void updateCurrentMDCLine() {
		// The system performs much (much) better if we ask for this function to
		// be performed "later".
		// This is not mandatory, though, as getCurrentMDCLine is called from
		// the graphic loop anyway.
		if (!this.textLineRefreshAsked) {

			TimerTask task = new TimerTask() {

				public void run() {
					textLineRefreshAsked = true;
					SwingUtilities.invokeLater(new ManuelDeCodageLineUpdater());
					cancel();
				}

			};
			Timer timer= new Timer();
			timer.schedule(task, 500);
			//task.run();
		}
	}

	/**
	 * Monitors the display of the current code buffer.
	 * 
	 * @author S. Rosmorduc
	 * 
	 */
	private class CodeModel implements MDCModelEditionListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdcDisplayer.draw.MDCCaretChangeListener#caretChanged(jsesh.mdcDisplayer.draw.MDCCaret)
		 */
		public void caretChanged(MDCCaret caret) {
		}

		public void codeChanged(StringBuffer code) {
			codeField.setText(getEditor().getCodeBuffer());
		}

		public void focusGained(StringBuffer code) {
		}

		public void focusLost() {
		}

		public String getText() {
			if (editor.getCodeBuffer() != null)
				return editor.getCodeBuffer();
			else
				return "";
		}

		public void separatorChanged() {
			separatorField.setText("" + getEditor().getCurrentSeparator());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.editor.MDCModelEditionListener#textChanged()
		 */
		public void textChanged() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.editor.MDCModelEditionListener#textEdited(jsesh.mdc.model.operations.ModelOperation)
		 */
		public void textEdited(ModelOperation op) {
		}
	}

	private final class ManuelDeCodageLineUpdater implements Runnable {
		String s;

		public void run() {
			String s = editor.getWorkflow().getCurrentLineAsString();
			mdcField.setText(s);
			textLineRefreshAsked = false;
		}
	}
}