package jsesh.utilitySoftwares.signInfoEditor.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple entry in signinfo.
 * Corresponds to a XML tag, with attributes
 * @author rosmord
 */
public class XMLInfoProperty {

	private String name = "";
	private String content = "";
	private ChildListener parent= null;
	
	/**
	 * Is the current info a user created one, or is it part of the standard JSesh.
	 */
	private boolean userDefinition = false;
	
	/**
	 * Map of String->String.
	 */
	private Map<String, String> attributes = new HashMap<String, String>();

	

	public XMLInfoProperty(String name, boolean userDefinition) {
		super();
		this.name = name;
		this.userDefinition = userDefinition;
	}

	
	public XMLInfoProperty(String name, String content, boolean userDefinition) {
		super();
		this.name = name;
		this.content = content;
		this.userDefinition = userDefinition;
	}


	public XMLInfoProperty() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		notifyParent();
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
		notifyParent();
	}


	public boolean isUserDefinition() {
		return userDefinition;
	}

	public void setUserDefinition(boolean userDefinition) {
		this.userDefinition = userDefinition;
		notifyParent();
	}

	public void setAttribute(String attrName, String attrValue) {
		String oldAttribute= get(attrName);
		// Do not change anything if the values are the same !
		if (oldAttribute == null && attrValue == null)
			return;
		if (attrValue != null && attrValue.equals(oldAttribute))
			return;
		attributes.put(attrName, attrValue);
		notifyParent();
	}
	
	public String toString() {
		StringBuffer buff= new StringBuffer();
		buff.append("<").append(name).append(" ").append(attributes).append(">");
		buff.append(content);
		buff.append("</").append(name).append(">");
		return buff.toString();
	}

	public String get(String attributeName) {
		return attributes.get(attributeName);
	}

	protected Map<String, String> getAttributes() {
		return Collections.unmodifiableMap(attributes);
	}
	
	/**
	 * Returns the collection of all attribute names
	 * @return a collection of Strings.
	 */
	public Collection<String> getAttributeNames() {
		return attributes.keySet();
	}
	
	public void setParent(ChildListener parent) {
		this.parent = parent;
	}
	
	private void notifyParent() {
		if (parent != null)
			parent.childChanged();
	}
}