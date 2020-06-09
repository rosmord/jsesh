package jsesh.utilitySoftwares.signInfoEditor.model;

import java.util.ArrayList;
import java.util.List;

import jsesh.hieroglyphs.data.SignDescriptionConstants;


/**
 * Information about a particular sign in a XML file.
 * 
 * @author rosmord
 * 
 */
public class EditableSignInfo {
	private String code;
	private boolean alwaysDisplay= false;
	private boolean alwaysDisplayProvidedByUser= false;
	private List signInfoPropertyList = new ArrayList();
	private ChildListener parent= null;
	/**
	 * Object used to listen to this sign's attributes modifications.
	 */
	private PropertyChangeListener propertyChangeListener= new PropertyChangeListener();
	
	public EditableSignInfo(String code) {
		this.code = code;
	}

	/**
	 * Copy constructor for sign info.
	 * 
	 * @param other
	 */
	public EditableSignInfo(EditableSignInfo other) {
		code = other.code;
		for (int i = 0; i < other.signInfoPropertyList.size(); i++) {
			SignInfoProperty newProp = new SignInfoProperty(
					(SignInfoProperty) other.signInfoPropertyList.get(i));
			add(newProp);
			
		}
	}

	public String getCode() {
		return code;
	}

	/**
	 * Returns the relevant property lines for a given property. Changes in
	 * these properties will affect the element.
	 *
     * The labels are stored in SignDescriptionConstants
	 * @param propertyLabel
	 * @return a list of live properties.
     * @see SignDescriptionConstants
	 */
	public List getPropertyList(String propertyLabel) {
		ArrayList l = new ArrayList();
		java.util.Iterator it = signInfoPropertyList.iterator();
		while (it.hasNext()) {
			XMLInfoProperty prop = (XMLInfoProperty) it.next();
			if (prop.getName().equals(propertyLabel))
				l.add(prop);
		}
		return l;
	}


	/**
	 * Returns a copy of the list of this sign's properties.
	 * @return a list of SignInfoProperty
	 * @see SignInfoProperty
	 */
	public List getPropertyList() {
		return new ArrayList(signInfoPropertyList);
	}
	
	public void add(XMLInfoProperty signInfoProperty) {
		signInfoPropertyList.add(signInfoProperty);
		signInfoProperty.setAttribute("sign", getCode());
		// Register as the parent of the new property...
		signInfoProperty.setParent(propertyChangeListener);
		// Notify our own parent...
		notifyParent();
	}

	/**
	 * Removes a property.
	 * 
	 * @param property
	 */
	public void remove(XMLInfoProperty property) {
		signInfoPropertyList.remove(property);
		property.setParent(null);
		notifyParent();
	}

	/**
	 * Add the attributes of another sign into this one.
	 * @param other
	 */
	public void addAttributesOf(EditableSignInfo other) {
		// TODO Auto-generated method stub
		for (int i= 0; i < other.signInfoPropertyList.size(); i++) {
			SignInfoProperty prop= (SignInfoProperty) other.signInfoPropertyList.get(i);
			// Add a copy.
			add(new SignInfoProperty(prop));
		}
	}

	public boolean isAlwaysDisplay() {
		return alwaysDisplay;
	}
	
	public void setAlwaysDisplay(boolean alwaysDisplay) {
		this.alwaysDisplay = alwaysDisplay;
	}

	/**
	 * @return the alwaysDisplayProvidedByUser
	 */
	public boolean isAlwaysDisplayProvidedByUser() {
		return alwaysDisplayProvidedByUser;
	}

	/**
	 * @param alwaysDisplayProvidedByUser the alwaysDisplayProvidedByUser to set
	 */
	public void setAlwaysDisplayProvidedByUser(boolean alwaysDisplayProvidedByUser) {
		this.alwaysDisplayProvidedByUser = alwaysDisplayProvidedByUser;
	}
	
	public void setParent(ChildListener parent) {
		this.parent = parent;
	}
	
	private void notifyParent() {
		if (parent != null) {
			parent.childChanged();
		}
	}
	
	
	private class PropertyChangeListener implements ChildListener {
		public void childChanged() {
			notifyParent();
		}	
	}

}
