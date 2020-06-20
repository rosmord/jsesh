/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.jhotdraw.actions.edit;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.viewClass.JSeshView;
import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;

/**
 * This action inserts a computed line number.
 * <p> The line number is computed in the following way:
 * <ul>
 * <li> if there is no existing line number, line number |1- is inserted.
 * <li> if there is a line number indication X <em>before</em> the current position,
 *      the next line number is computed, by incremented the last number which appears
 *      in X. For instance, if X is <code>|col 12, l. 13-</code>, the inserted 
 *      line number will be <code>|col 12, l. 14-</code>.
 * </ul>
 * @author rosmord
 */
public class InsertNextLineNumberAction extends AbstractViewAction{
    
	public static final String ID = "edit.insertNextLineNumber";

	public InsertNextLineNumberAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configure(this);
	}

	public void actionPerformed(ActionEvent e) {
		JSeshView v = (JSeshView) getActiveView();
		if (v != null) {
			
		}
	}
        
        ERROR... Remove me when code is written.
}
