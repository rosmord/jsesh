package org.qenherkhopeshef.viewToolKit.drawing.element.property;

public class Property {

	private String name;
	private boolean geometric;

	public Property(String name, boolean geometric) {
		super();
		this.name = name;
		this.geometric= geometric;
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * Does this property influences the geometry of the element?.
	 * Typically, a color property would not. Nor a property which doesn't change the actual size of an element.
	 * @return
	 */
	public boolean isGeometric() {
		return geometric;
	}
	
}
