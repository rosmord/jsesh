/**
 * 
 */
package jsesh.mdcDisplayer.swing.editor;

/**
 * Command for the undo/redo system.
 * @author rosmord
 *
 */
abstract class MDCCommand {
	/**
	 * Was this command applied on a "clean" text ?
	 */
	private boolean firstCommand;
	
	
	protected MDCCommand(boolean firstCommand) {
		super();
		this.firstCommand = firstCommand;
	}

	abstract public void doCommand();
	
	abstract public void undoCommand();
	
	/**
	 * Was this command applied on a clean text?
	 * @return
	 */
	
	public boolean isFirstCommand() {
		return firstCommand;
	}
}