package jsesh.mdcDisplayer.swing.units;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;

/**
 * Callback to update the displayed value of the height field when the unit is changed.
 * 
 * Use : call the static method linkUnitsToValueField.
 */
public class UnitMaintainter implements ItemListener {
	private LengthUnit currentUnit;
	private JComboBox unitField;
	private JFormattedTextField lengthField;
	
	/**
	 * Utility method used to link a combobox displaying a length unit to a textfield displaying a value expressed in this unit.
	 * @param unitField the field containing the unit (e.g. mm, pt...)
	 * @param lengthField the field containing the length (a double, in the given unit).
	 */
	public static void linkUnitsToValueField(JComboBox unitField, JFormattedTextField lengthField) {
		unitField.addItemListener(new UnitMaintainter(unitField, lengthField));
	}
	
	/**
	 * @param unitField
	 * @param lengthField2
	 */
	private UnitMaintainter(JComboBox unitField, JFormattedTextField lengthField) {
		currentUnit= (LengthUnit) unitField.getSelectedItem();
		this.unitField= unitField;
		this.lengthField= lengthField;
	}



	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (e.getItem() != null) {
				double height= getLengthInPoints();
				currentUnit= (LengthUnit) unitField.getSelectedItem();
				setLengthInPoints(height);
			}
		}
	}



	/**
	 * Sets the value of the length field, using an absolute unit (points), and not the displayed one.
	 * The field will contain the value, expressed in the current unit.
	 * @param length
	 */
	private void setLengthInPoints(double length) {
		lengthField.setValue(new Double(length/currentUnit.getPointsValue()));
		
	}

	/**
	 * Returns the length, expressed in points.
	 * @return
	 */
	private double getLengthInPoints() {
		int pointValue= (int)(((Number)lengthField.getValue()).doubleValue() * currentUnit.getPointsValue());
		return pointValue;		
	}
}