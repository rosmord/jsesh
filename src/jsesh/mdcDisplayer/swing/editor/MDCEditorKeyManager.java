/*
 * Created on 1 oct. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.mdcDisplayer.swing.editor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import jsesh.mdcDisplayer.swing.actions.CopyAction;
import jsesh.mdcDisplayer.swing.actions.CutAction;
import jsesh.mdcDisplayer.swing.actions.ExpandSelectionAction;
import jsesh.mdcDisplayer.swing.actions.GoDownAction;
import jsesh.mdcDisplayer.swing.actions.GoLeftAction;
import jsesh.mdcDisplayer.swing.actions.GoRightAction;
import jsesh.mdcDisplayer.swing.actions.GoUpAction;
import jsesh.mdcDisplayer.swing.actions.GroupHorizontalAction;
import jsesh.mdcDisplayer.swing.actions.GroupVerticalAction;
import jsesh.mdcDisplayer.swing.actions.NewLineAction;
import jsesh.mdcDisplayer.swing.actions.NewPageAction;
import jsesh.mdcDisplayer.swing.actions.PasteAction;

/**
 * Manages input for an MDC Editor. It might be interesting to make it public.
 * This class uses the "singleton" design pattern, as it is stateless. 
 * 
 * 
 * IMPORTANT : clean. Suppress unused stuff.
 * @author rosmord
 */


public class MDCEditorKeyManager extends KeyAdapter
{
	//static final String ADD_LOWER_LEVEL= "ADD_LOWER_LEVEL";
	//static final String ADD_NEW_CADRAT= "ADD_NEW_CADRAT";
	//static final String ADD_SAME_LEVEL= "ADD_SAME_LEVEL";
	// static final String BACK_SPACE= "BACK SPACE";
	static final String GO_LEFT= "GO LEFT";
	static final String GO_RIGHT= "GO RIGHT";
	static final String INSERT_CHAR= "INSERT CHAR";
	static final String BEGINNING_OF_LINE= "BEGINNING OF LINE";
	static final String END_OF_LINE= "END_OF_LINE";
	static final String COPY= "COPY";
	static final String PASTE= "PASTE";
	static final String CUT= "CUT";
	static final String GROUP_HORIZONTAL= "GROUP HORIZONTAL";
	static final String GROUP_VERTICAL= "GROUP VERTICAL";
	static final String EXPAND_SELECTION_LEFT= "EXPAND_SELECTION_LEFT";
	static final String EXPAND_SELECTION_RIGHT= "EXPAND_SELECTION_RIGHT";
	static final String NEW_LINE= "NEW_LINE";
	static final String NEW_PAGE= "NEW_PAGE";
	static final String GO_DOWN= "GO DOWN";
	static final String GO_UP= "GO UP";
	
	/**
	 */
	static private MDCEditorKeyManager instance = null;

	/**
	 * return the singleton instance.
	 * @return
	 */
	public static MDCEditorKeyManager getInstance() {
		if (instance == null) {
			instance = new MDCEditorKeyManager();
		}
		return instance;
	}

	private ActionMap actionMap;

	private InputMap inputMap;

	private MDCEditorKeyManager()
	{
		inputMap= new InputMap();
		actionMap= new ActionMap();
		addInputs();
		addActions();
	}

	private void addAction(String command, Action action)
	{
		actionMap.put(command, action);
	}

	private void addActions()
	{
		addAction(GO_RIGHT, new GoRightAction());
		addAction(GO_LEFT, new GoLeftAction());
		addAction(GO_DOWN, new GoDownAction());
		addAction(GO_UP, new GoUpAction());
		addAction(BEGINNING_OF_LINE, new AbstractAction("Beginning of line") {

			public void actionPerformed(ActionEvent e) {
				JMDCEditor ed= (JMDCEditor) e.getSource();
				ed.getWorkflow().beginningOfLine();
			}
			
		});
		
		addAction(END_OF_LINE, new AbstractAction("End Of line") {

			public void actionPerformed(ActionEvent e) {
				JMDCEditor ed= (JMDCEditor) e.getSource();
				ed.getWorkflow().endOfLine();		
			}
			
		});
		// addAction(BACK_SPACE, new BackSpaceAction());
		//addAction(ADD_LOWER_LEVEL, new AddLowerLevelAction());
		//addAction(ADD_SAME_LEVEL, new AddSameLevelAction());
		//addAction(ADD_NEW_CADRAT, new AddNewCadratAction());
		addAction(NEW_LINE, new NewLineAction());
		addAction(NEW_PAGE, new NewPageAction());
		addAction(COPY, new CopyAction());
		addAction(PASTE, new PasteAction());
		addAction(CUT, new CutAction());
		addAction(GROUP_HORIZONTAL, new GroupHorizontalAction());
		addAction(GROUP_VERTICAL, new GroupVerticalAction());
		addAction(EXPAND_SELECTION_LEFT, new ExpandSelectionAction(-1));
		addAction(EXPAND_SELECTION_RIGHT, new ExpandSelectionAction(1));
	}

	private void addInput(char code, String command)
	{
		inputMap.put(KeyStroke.getKeyStroke(code), command);
	}

	private void addInput(String code, String command)
	{
		inputMap.put(KeyStroke.getKeyStroke(code), command);
	}

	private void addInputs()
	{
		addInput("LEFT", GO_LEFT);
		addInput("RIGHT", GO_RIGHT);
		addInput("DOWN", GO_DOWN);
		addInput("UP", GO_UP);
		addInput("HOME",  BEGINNING_OF_LINE);
		addInput("END", END_OF_LINE);
		//addInput("BACK_SPACE", BACK_SPACE);
		// addInput(':', ADD_LOWER_LEVEL);
		// addInput('*', ADD_SAME_LEVEL);
		// There are a number of ways to enter new cadrats :
		//addInput('-', ADD_NEW_CADRAT);
		//addInput("SPACE", ADD_NEW_CADRAT);
		// addInput("ENTER", ADD_NEW_CADRAT); // ENTER SHOULD ALSO CREATE A NEW LINE !
		// IMPORTANT : Find how to get abstract cross platform bindings for the following :
		addInput("shift ENTER", NEW_PAGE);
		addInput("ENTER", NEW_LINE);
		/*
		addInput("control C", COPY);
		addInput("control X", CUT);
		addInput("control V", PASTE);
		addInput("control H", GROUP_HORIZONTAL);
		addInput("control G", GROUP_VERTICAL);
		*/
		addInput("shift LEFT", EXPAND_SELECTION_LEFT);
		addInput("shift RIGHT", EXPAND_SELECTION_RIGHT);
		
	}
	
	public void control(JMDCEditor editor)
	{
		editor.addKeyListener(this);
		editor.setActionMap(actionMap);
		editor.setInputMap(JComponent.WHEN_FOCUSED, inputMap);
	}


	public ActionMap getActionMap() {
		return actionMap;
	}


	public InputMap getInputMap() {
		return inputMap;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyAdapter#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e)
	{
		if (e.isActionKey() || e.isControlDown() || e.isAltDown())
			return;
		// Codes handled by actions.
		if (e.getKeyChar() == '\n')
			return;
 		if (e.getSource() instanceof JMDCEditor)
		{
			JMDCEditor editor= (JMDCEditor) e.getSource();
			editor.getWorkflow().keyTyped(e.getKeyChar());
		}
	}

}
