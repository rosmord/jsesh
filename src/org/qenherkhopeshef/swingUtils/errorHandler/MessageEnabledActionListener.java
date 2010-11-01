/**
 * 
 */
package org.qenherkhopeshef.swingUtils.errorHandler;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Action listener which will catch user messages and display them.
 * @author rosmord
 *
 */
abstract public class MessageEnabledActionListener implements ActionListener {

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	final public void actionPerformed(ActionEvent e) {
		try {
			performProtectedAction(e);
		} catch (UserMessage ex) {
			new ErrorMessageDisplayer(ex).display();
		}
	}

	/**
	 * @param e
	 */
	abstract protected void performProtectedAction(ActionEvent e);
	 

}
