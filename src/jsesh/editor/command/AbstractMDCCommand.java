/**
 * 
 */
package jsesh.editor.command;

/**
 * Command for the undo/redo system.
 * @author rosmord
 *
 */
abstract class AbstractMDCCommand implements MDCCommand {
	/**
	 * Was this command applied on a "clean" text ?
	 */
	private boolean firstCommand;
	
	/**
	 * Create a command.
	 * @param firstCommand was the command applied to a "clean" text ?
	 */
	protected AbstractMDCCommand(boolean firstCommand) {
		super();
		this.firstCommand = firstCommand;
	}

	/* (non-Javadoc)
	 * @see jsesh.editor.command.MyCommand#doCommand()
	 */
	abstract public void doCommand();
	
	/* (non-Javadoc)
	 * @see jsesh.editor.command.MyCommand#undoCommand()
	 */
	abstract public void undoCommand();
	
	/* (non-Javadoc)
	 * @see jsesh.editor.command.MyCommand#isFirstCommand()
	 */
	
	public boolean isFirstCommand() {
		return firstCommand;
	}
}