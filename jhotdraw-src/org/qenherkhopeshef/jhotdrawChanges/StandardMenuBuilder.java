package org.qenherkhopeshef.jhotdrawChanges;

import javax.swing.JMenu;

import org.jhotdraw_7_4_1.app.View;

/**
 * An optional component which can be used to spy on the building of standard menus.
 * The idea is that one will at time wants to use and build standard menus, but to insert his own 
 * elements in them. The application model can provide an optional StandardMenuBuilder which will be called 
 * at specific points. 
 * @author rosmord
 *
 */
public interface StandardMenuBuilder {
	/**
	 * Called after inserting all files... new elements.
	 * @param fileMenu
	 */
	void afterFileNew(JMenu fileMenu, View view);
	
	/**
	 * Called after all "open file"...
	 */
	void afterFileOpen(JMenu fileMenu, View view);
	
	/**
	 * Called after all "save file"...
	 */
	void afterFileClose(JMenu fileMenu, View view);
	
	/**
	 * Called at the end of the file menu, but before the "Quit" entry (if any) is added.
	 */
	void atEndOfFileMenu(JMenu fileMenu, View view);

	/**
	 * Called at the end of the "edit" menu.
	 */
	void atEndOfEditMenu(JMenu editMenu, View view);

}
