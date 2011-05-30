package org.qenherkhopeshef.jhotdrawChanges;

import javax.swing.JMenu;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.View;

/**
 * A default class for standard menu builder, which does nothing.
 * Can be used as a basis for custom builders.
 * @author rosmord
 *
 */
public class DefaultStandardMenuBuilder implements StandardMenuBuilder {

	
	public void afterFileClose(JMenu fileMenu, Application app, View view) {		
	}

	
	public void afterFileNew(JMenu fileMenu, Application app, View view) {
	}

	
	public void afterFileOpen(JMenu fileMenu, Application app, View view) {
	}

	
	public void atEndOfFileMenu(JMenu fileMenu, Application app,View view) {	
	}

	
	public void atEndOfEditMenu(JMenu editMenu, Application app, View view) {
	}


}
