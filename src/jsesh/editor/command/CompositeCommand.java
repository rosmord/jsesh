/**
 * 
 */
package jsesh.editor.command;

import java.util.ArrayList;
import java.util.List;



class  CompositeCommand extends AbstractMDCCommand {
	private List commands= new ArrayList();
	
	/**
	 * Create an empty composite command.
	 * @param firstCommand was the command applied to a clean text ?
	 */
	public CompositeCommand(boolean firstCommand) {
		super(firstCommand);
	}
	
	public void addCommand(MDCCommand c) {commands.add(c);}
	
	public void doCommand() {
		for (int i= 0; i < commands.size(); i++) {
			MDCCommand c= (MDCCommand)commands.get(i);
			c.doCommand();
		}
	}
	
	public void undoCommand() {
		for (int i= commands.size()-1; i >=0 ; i--) {
			MDCCommand c= (MDCCommand)commands.get(i);
			c.undoCommand();
		}
	}
}