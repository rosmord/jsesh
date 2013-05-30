package org.qenherkhopeshef.viewToolKit.drawing.element.property;

/**
 * Pseudo-property for an elements bounds (position and size).
 * Right now, elements bounds are dealt with as simple instance variable. When
 * they are modified, we fire a property change with a fake property object.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 *
 */
public class BoundsProperty extends Property{

	public BoundsProperty() {
		super("BOUNDS", true);
	}
	
}
