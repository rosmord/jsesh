/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.swing.units;

import javax.swing.JFormattedTextField;

/**
 * A length unit.
 * @author rosmord
 */
public enum LengthUnit {
    // 1 POINT= 20 TWIPS
    // 1 INCH= 72 POINTS
    // 1 mm = 1/25.4 INCH

    POINT("Point", 1), INCH("Inch", 72), MM("mm", 72 / 25.4);

    private final String name;
    private final double pointsValue;

    /**
     * Create a unit
     *
     * @param name : the name of the unit.
     * @param value : the value of the unit, in points (1/20th of inch).
     */
    private LengthUnit(String name, double value) {
        this.name = name;
        this.pointsValue = value;
    }

    /**
     * Returns the name of the unit.
     * @return 
     */
    @Override
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
     * Returns the value (in points) of a text field expressed in this unit.
     *
     * @param field the formated text field (with Double values)
     * @return a value, expressed in points.
     * @see UnitMaintainter
     */
    public double getValueFromTextField(JFormattedTextField field) {
        return toPoints(((Number) field.getValue()).doubleValue());
    }

    /**
     * Returns the value in this unit of a length expressed in points.
     *
     * @param length a length in points
     * @return a value in this unit
     */
    public Double fromPoints(double length) {
        return length / pointsValue;
    }

    /**
     * Converts a value in this unit into a value in points.
     *
     * @param lengthInUnit
     * @return a value in points
     */
    public double toPoints(double lengthInUnit) {
        return lengthInUnit * getPointsValue();
    }

}
