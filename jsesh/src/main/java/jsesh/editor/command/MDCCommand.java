package jsesh.editor.command;

public interface MDCCommand {

	abstract public void doCommand();

	abstract public void undoCommand();

	/**
	 * Was this command applied on a clean text?
	 * @return
	 */

	public abstract boolean isFirstCommand();

}