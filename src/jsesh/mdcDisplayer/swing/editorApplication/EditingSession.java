package jsesh.mdcDisplayer.swing.editorApplication;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;

import jsesh.editor.JMDCEditor;

/**
 * A class representing an editing session.
 * @author rosmord
 *
 */
public class EditingSession {

	private JInternalFrame frame;

	private JMDCEditor editorField;
	
	public EditingSession() {
		editorField= new JMDCEditor();
		frame= new JInternalFrame("New Session",
				true,true,true,true
		);
		frame.getContentPane().add(new JScrollPane(editorField));
		frame.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
		frame.pack();
	}
	
	public JInternalFrame getFrame() {
		return frame;
	}
	
	public JMDCEditor getEditorField() {
		return editorField;
	}
}
