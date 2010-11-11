package org.qenherkhopeshef.jhotdrawChanges;

import javax.swing.JMenu;

import org.jhotdraw_7_4_1.app.View;

/**
 * A default class for standard menu builder, which does nothing.
 * Can be used as a basis for custom builders.
 * @author rosmord
 *
 */
public class DefaultStandardMenuBuilder implements StandardMenuBuilder {

	
	public void afterFileClose(JMenu fileMenu, View view) {		
	}

	
	public void afterFileNew(JMenu fileMenu, View view) {
	}

	
	public void afterFileOpen(JMenu fileMenu, View view) {
	}

	
	public void atEndOfFileMenu(JMenu fileMenu, View view) {	
	}

	
	public void atEndOfEditMenu(JMenu editMenu, View view) {
	}
}
