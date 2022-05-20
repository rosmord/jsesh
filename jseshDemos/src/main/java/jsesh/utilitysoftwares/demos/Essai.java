package jsesh.utilitysoftwares.demos;

import java.beans.PropertyChangeSupport;
import org.qenherkhopeshef.guiFramework.PropertyHolder;



/**
 * Test class for PropertyButtonModel.
 * @author rosmord
 *
 */
public class Essai implements PropertyHolder {
	
	public static final String INDICATEUR= "indicateur";
	public static final String VALUE= "value";
	
	private boolean indicateur= true;
	
	private String value= "x";
	
	private PropertyChangeSupport propertyChangeSupport= new PropertyChangeSupport(this);
	
	public boolean isIndicateur() {
		return indicateur;
	}
	
	public void setIndicateur(boolean indicateur) {
		boolean old= this.indicateur;
		this.indicateur = indicateur;
		propertyChangeSupport.firePropertyChange(INDICATEUR, old, indicateur);
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		String oldValue= this.value;
		this.value = value;
		propertyChangeSupport.firePropertyChange(VALUE, oldValue, value);
	}
	
	public String toString() {
		return "value " + value + "\t boolean "+ indicateur;
	}
	
	public PropertyChangeSupport getPropertyChangeSupport() {
		return propertyChangeSupport;
	}
}