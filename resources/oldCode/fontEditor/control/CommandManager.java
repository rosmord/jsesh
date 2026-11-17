/*
 * Created on 5 mars 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.fontEditor.control;

import java.util.Stack;

/**
 * @author rosmord
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CommandManager {
	private Command undone;

	private Stack commandes;

	public CommandManager() {
		commandes = new Stack();
		undone = null;
	}

	public void add(Command c) {
		c.doCommande();
		commandes.push(c);
	}

	public void undo() {
		if (canUndo()) {
			undone = (Command) commandes.pop();
			undone.undoCommande();
		}
	}

	public void redo() {
		if (canRedo()) {
			add(undone);
			undone = null;
		}
	}

	public boolean canRedo() {
		return (undone != null);
	}

	public boolean canUndo() {
		return (!commandes.isEmpty());
	}
}
