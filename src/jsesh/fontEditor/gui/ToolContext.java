/*
 * Created on 5 mars 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.fontEditor.gui;

import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputListener;

import jsesh.fontEditor.control.Command;
import jsesh.fontEditor.control.CommandManager;
import jsesh.fontEditor.model.GlyphModel;
import jsesh.fontEditor.model.Point;

/**
 * Data manipulated by a tool.
 *
 * <p>A Tool will typically manipulate the view and the model, 
 * and then build a command which will be passed to the context for execution.
 * 
 * @author rosmord
 */
public class ToolContext {
	private SimpleDrawView view;

	private CommandManager commandManager;

	public ToolContext(SimpleDrawView view) {
		this.view = view;
		commandManager = new CommandManager();
	}


	/**
	 * @return  the view
	 */
	public SimpleDrawView getView() {
		return view;
	}

	public GlyphModel getDessin() {
		return view.getGlyphModel();
	}

	/**
	 * Fonction utilitaire pour récupérer un point à partir d'un événement souris.
	 * @param e
	 * @return the point where the event took place.
	 */
	public Point getPointFromEvent(MouseEvent e) {
		return view.screenToModel(e.getX(), e.getY());
	}

	/**
	 * Délie le contexte d'un contrôleur qui recevait les événements engendrés par
	 * l'objet graphique sous jacent.
	 * @param control
	 */

	public void deconnecte(MouseInputListener control) {
		view.removeMouseListener(control);
		view.removeMouseMotionListener(control);
	}

	/**
	 *  Lie le contexte à un contrôleur qui recevra les événements engendrés par
	 * l'objet graphique sous jacent.
	 * @param control
	 */

	public void connecte(MouseInputListener control) {
		view.addMouseListener(control);
		view.addMouseMotionListener(control);
	}
	/**
	 * @param c
	 */
	public void add(Command c) {
		commandManager.add(c);
	}

	/**
	 * @return true if the command can be redone; false otherwise.
	 */
	public boolean canRedo() {
		return commandManager.canRedo();
	}

	/**
	 *  @return true if the command can be undone; false otherwise.
	 */
	public boolean canUndo() {
		return commandManager.canUndo();
	}

	/**
	 * redo the command.
	 */
	public void redo() {
		commandManager.redo();
	}

	/**
	 * undo the command.
	 */
	public void undo() {
		commandManager.undo();
	}
}
