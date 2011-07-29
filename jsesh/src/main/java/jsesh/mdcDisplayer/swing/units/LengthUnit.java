/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.units;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;

/**
 * @author rosmord
 * 
 */
public class LengthUnit {
	// 1 POINT= 20 TWIPS
	// 1 INCH= 72 POINTS
	// 1 mm = 1/25.4 INCH

	public static final LengthUnit POINT = new LengthUnit("Point", 1);
	public static final LengthUnit INCH = new LengthUnit("Inch", 72);
	public static final LengthUnit MM = new LengthUnit("mm", 72 / 25.4);
	public static final LengthUnit UNITS[] = new LengthUnit[] { POINT, INCH, MM };

	private String name;
	private double pointsValue;

	/**
	 * Create a unit
	 * 
	 * @param name
	 *            : the name of the unit.
	 * @param value
	 *            : the value of the unit, in points (1/20th of inch).
	 */
	public LengthUnit(String name, double value) {
		this.name = name;
		this.pointsValue = value;
	}

	/**
	 * Returns the name of the unit.
	 */
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	/**
	 * Returns the unit value, expressed in points
	 * 
	 * @return
	 */
	public double getPointsValue() {
		return pointsValue;
	}

	/**
	 * Configure a combobox to display units.
	 * 
	 * Note that the fields containing lengths should be attached to this
	 * combobox using the method UnitMaintainter.linkUnitsToValueField.
	 * 
	 * <p>
	 * We could have used a custom type extending JCombobox for this. But It
	 * would have required more configuration for our GUI generator.
	 * 
	 * @param combobox
	 *            the JCombobox
	 * @param unit
	 *            the initial value to display.
	 * 
	 * @see UnitMaintainter#linkUnitsToValueField(JComboBox,
	 *      JFormattedTextField)
	 */
	static public void attachToCombobox(JComboBox combobox, LengthUnit unit) {
		combobox.setModel(new DefaultComboBoxModel(LengthUnit.UNITS));
		combobox.setSelectedItem(unit);
	}

	/**
	 * Returns the value (in points) of a text field expressed in this unit.
	 * @param field the formated text field (with Double/Float... values)
	 * @return a value, expressed in points.
	 * @see UnitMaintainter
	 */
	public double getValueFromTextField(JFormattedTextField field) {
		return convertToPoints((double)(((Number) field.getValue()).doubleValue()));
	}

	/**
	 * Returns the value in this unit of a length expressed in points.
	 * 
	 * @param length a length in points
	 * @return a value in this unit
	 */
	public Double convert(int length) {
		return new Double(length / pointsValue);
	}
	
	/**
	 * Converts a value in this unit into a value in points.
	 * @param lengthInUnit
	 * @return a value in points
	 */
	public double convertToPoints(double lengthInUnit) {
		return lengthInUnit* getPointsValue();
	}
	
}
