package jsesh.editorApplication_old;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.qenherkhopeshef.guiFramework.ActionActivationManager;
import org.qenherkhopeshef.guiFramework.AppDefaults;
import org.qenherkhopeshef.guiFramework.PropertyHolder;

/**
 * 
 * @author rosmord
 * @deprecated
 */
public class MDCEditorApplicationWorkflow implements PropertyHolder {

	private class SessionChanger extends InternalFrameAdapter {

		public void internalFrameActivated(InternalFrameEvent e) {
			updateActionsStatus();
			getCurrentEditingSession().getEditorField().requestFocusInWindow();
		}

		public void internalFrameClosing(InternalFrameEvent e) {
			if (e.getSource() instanceof JInternalFrame) {
				((JInternalFrame) e.getSource()).dispose();
			}
			updateActionsStatus();
		}

		public void internalFrameDeactivated(InternalFrameEvent e) {
			updateActionsStatus();
		}

	}

	private MDCEditorApplicationWindow mainWindow;

	private AppDefaults defaults;

	/**
	 * The list of currently opened editing jobs.
	 */
	private List editingSessions;

	private boolean smallSignsCentered = false;

	private String orientation = "horizontal";

	private String direction = "left2Right";

	private String copySize = "large";

	/**
	 * We could use a more generic system, transforming the workflow into a
	 * bean. For now, it might be enough.
	 */
	private ActionActivationManager actionActivationManager;

	private boolean dataAvailable = false;

	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
			this);

	public MDCEditorApplicationWorkflow(MDCEditorApplicationWindow mainWindow,
			AppDefaults defaults) {
		this.mainWindow = mainWindow;
		this.defaults = defaults;
		editingSessions = new ArrayList();
		actionActivationManager = new ActionActivationManager();

		// mainWindow.getHieroglyphicPalette()
		// .setHieroglyphPaletteListener(new HieroglyphPaletteListener() {
		//
		// public void signSelected(String code) {
		// addCode(code);
		// }
		// });
		//		
	}

	public void addCode(String code) {

	}

	public void exit() {
		int reallyQuit = JOptionPane.showConfirmDialog(mainWindow, defaults
				.getString(ApplicationMessageKeys.ASK_EXIT_MESSAGE), defaults
				.getString(ApplicationMessageKeys.CONFIRMATION_TITLE),
				JOptionPane.YES_NO_OPTION);
		if (reallyQuit == JOptionPane.YES_OPTION)
			System.exit(0);
	}

	public void rotate(String angle) {
	}

	public void insertCode(String code) {
		if (isSessionAvailable()) {
			EditingSession s = getCurrentEditingSession();
			s.getEditorField().getWorkflow().insertMDC(code);
		}
	}

	public void open() {
		dataAvailable = true;
		new JFileChooser().showOpenDialog(mainWindow);
		updateActionsStatus();
	}

	public void updateActionsStatus() {
		getPropertyChangeSupport().firePropertyChange(null, null, null);
	}

	public void close() {
		dataAvailable = false;
		updateActionsStatus();
	}

	/**
	 * @return the dataAvailable
	 */
	public boolean isDataAvailable() {
		return dataAvailable;
	}

	public PropertyChangeSupport getPropertyChangeSupport() {
		return propertyChangeSupport;
	}

	public String getOrientation() {
		return orientation;
	}

	public void setOrientation(String orientation) {
		String oldOrientation = this.orientation;
		this.orientation = orientation;
		propertyChangeSupport.firePropertyChange("orientation", oldOrientation,
				orientation);
	}

	public boolean isSmallSignsCentered() {
		return smallSignsCentered;
	}

	public void setSmallSignsCentered(boolean smallSignsCentered) {
		boolean oldValue = this.smallSignsCentered;
		this.smallSignsCentered = smallSignsCentered;
		propertyChangeSupport.firePropertyChange("smallSignsCentered",
				oldValue, smallSignsCentered);
	}

	/**
	 * @return the copySize
	 */
	public String getCopySize() {
		return copySize;
	}

	/**
	 * @param copySize
	 *            the copySize to set
	 */
	public void setCopySize(String copySize) {
		this.copySize = copySize;
	}

	/**
	 * @return the direction
	 */
	public String getDirection() {
		return direction;
	}

	/**
	 * @param direction
	 *            the direction to set
	 */
	public void setDirection(String direction) {
		this.direction = direction;
	}

	public void newFile() {
		EditingSession session = new EditingSession();
		editingSessions.add(session);
		session.getFrame().setVisible(true);
		session.getFrame().addInternalFrameListener(new SessionChanger());
		mainWindow.addSession(session.getFrame());
		try {
			session.getFrame().setSelected(true);
			session.getFrame().moveToFront();
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns true if there is a session one can works on.
	 * 
	 * @return
	 */
	public boolean isSessionAvailable() {
		return (mainWindow.getCurrentSession() != null);
	}

	/**
	 * Returns true if there is a selection one can works on.
	 * 
	 * @return
	 */

	public boolean isSelectionAvailable() {
		if (isSessionAvailable()) {
			EditingSession s = getCurrentEditingSession();
			return false;
		}
		return false;
	}

	/**
	 * Returns true if the current session exists and needs saving.
	 * 
	 * @return
	 */

	public boolean isSessionSaveable() {
		if (isSelectionAvailable()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isUndoAvailable() {
		return false;
	}

	public boolean isRedoAvailable() {
		return false;
	}

	/**
	 * Returns the current selected editing session, or null if none.
	 * 
	 * @return
	 */
	private EditingSession getCurrentEditingSession() {
		if (mainWindow.getCurrentSession() != null) {
			for (int i = 0; i < this.editingSessions.size(); i++) {
				if (((EditingSession) editingSessions.get(i)).getFrame() == mainWindow
						.getCurrentSession()) {
					return ((EditingSession) editingSessions.get(i));
				}
			}
		}
		return null;
	}

	/**
	 * Called when a session is activated.
	 */
	public void sessionActivated() {

	}

	/**
	 * Called when a session is asked to close.
	 */
	public void sessionClosed() {

	}

	/**
	 * Called when a session is deactivated.
	 */

	public void sessionDeactivated() {

	}

	// public void showPalette() {
	// mainWindow.showPalette(true);
	// }

	public ActionActivationManager getActionActivationManager() {
		return actionActivationManager;
	}

}
