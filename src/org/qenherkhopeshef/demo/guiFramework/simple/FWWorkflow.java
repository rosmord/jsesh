package org.qenherkhopeshef.demo.guiFramework.simple;

import java.beans.PropertyChangeSupport;
import java.text.MessageFormat;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.qenherkhopeshef.guiFramework.AppDefaults;
import org.qenherkhopeshef.guiFramework.PropertyHolder;


/**
 * The Application facade.
 * <p> Note that in reality, facade still belong to the ui layer. 
 * This one is more or less a "presenter" in the "model-view-presenter" system.
 * <p> For strict MVC representation, the facade should delegate all calls to the appropriate model class.
 * 
 * <p> "Precondition" handling: to manage actions which can be enabled/disabled and depend on one boolean property of this object,
 * one must call activationManager.updateActions(this) when the boolean property changes (this could be handled by a bean listener ?)
 * 
 * <p> groups and the like: to properly manage groups actions (which govern RadioButtons) and "boolean" actions (which govern CheckButtons)
 * one must fire the correct property change event when the relevant properties are modified.
 * @author rosmord
 *
 */

public class FWWorkflow implements PropertyHolder {

	public static final String INCREMENT_VALUE = "incrementValue";

	/**
	 * UI.
	 */
	private JLabel display;

	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
			this);

	/**
	 * The "model". Here a simple integer.
	 * Normally, the actual model should be in a class of its own.
	 */
	
	private int value= 100;
	
	/**
	 * Value applied to the model.
	 */
	private int increment= 1; 
	
	/**
	 * Can the model be changed ?
	 */
	private boolean readOnly= false;
	
	/**
	 * Internationnal data (mainly for messages).
	 */
	private AppDefaults defaults;
	
	public FWWorkflow(JLabel display, AppDefaults defaults) {
		this.display= display;
		this.defaults= defaults;
		// Ensure the display is o.k.
		updateDisplay();
	}

	
	public PropertyChangeSupport getPropertyChangeSupport() {
		return propertyChangeSupport;
	}

	public void open() {
		JOptionPane.showMessageDialog(display, defaults.getString("NOT_OPENING"));
	}
	
	public void saveAs(String text) {
		String msg= MessageFormat.format(defaults.getString("SAVING_AS"), new Object[] {text});
		JOptionPane.showMessageDialog(display, msg);
	}
	
	/**
	 * Change the model value.
	 * @param incrString
	 */
	public void increase(String incrString) {
		if (! isReadOnly()) {
			int sign= Integer.parseInt(incrString);
			this.value+= sign*increment;
			updateDisplay();   
		}
	}
	
	private void updateDisplay() {
		display.setText(""+ value+ "   ");
	}
	
	public void setReadOnly(boolean readOnly) {
		boolean old= this.readOnly;
		this.readOnly = readOnly;
		getPropertyChangeSupport().firePropertyChange("readOnly",old, readOnly);
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}
	
	/**
	 * Change the increment which will be applied to the model.
	 * @param incrString
	 */
	public String getIncrementValue() {
		return "" + increment;
	}
	
	public void setIncrementValue(String incrString) {
		String old= getIncrementValue();
		if (incrString != null)
			increment= Integer.parseInt(incrString);
		getPropertyChangeSupport().firePropertyChange(INCREMENT_VALUE, old, incrString);
	}
	
	public void resetIncrement() {
		String old= getIncrementValue();
		increment= 1;
		getPropertyChangeSupport().firePropertyChange(INCREMENT_VALUE, old,"1");
	}
	
	public boolean isChangeable() {
		return ! readOnly;
	}
	
	public void exit() {
		System.exit(0);
	}
}
