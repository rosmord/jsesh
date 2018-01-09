package jsesh.swing.units;

import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;

/**
 * A changeable unit holder to use in Swing applications. Manages a combobox and
 * a list of text fields.
 *
 * @author rosmord
 */
public class UnitMediator {

    private LengthUnit currentUnit = LengthUnit.POINT;
    private JComboBox<LengthUnit> managedCombobox = null;
    private final List<JFormattedTextField> textFields = new ArrayList<>();

    public void setCurrentUnit(LengthUnit newUnit) {
        LengthUnit oldValue = this.currentUnit;
        if (oldValue != newUnit) {
            this.currentUnit= newUnit;
            // Update display.
            if (managedCombobox.getSelectedItem() != currentUnit) {
                managedCombobox.setSelectedItem(currentUnit);
            }
            // Update text fields (which are supposed to be originaly in points.            
            for (JFormattedTextField f : textFields) {
                // convert the old length in points:
                double points= oldValue.toPoints(((Number)f.getValue()).doubleValue());
                double newValue= this.currentUnit.fromPoints(points);
                f.setValue(newValue);
            }
        }
    }

    public LengthUnit getCurrentUnit() {
        return currentUnit;
    }

    public void managedTextField(JFormattedTextField field) {
        textFields.add(field);
    }
    
    public double getManagedFieldInPoints(JFormattedTextField field) {
        return currentUnit.getValueFromTextField(field);
    }

    /**
     * Attach this UnitMediator to a combobox for two ways linking.
     *
     * @param unitField
     */
    public void attachToComboBox(JComboBox<LengthUnit> unitField) {
        unitField.setModel(new DefaultComboBoxModel<>(LengthUnit.values()));        
        managedCombobox = unitField;
        managedCombobox.setSelectedItem(currentUnit);
        unitField.addItemListener(this::updatedCombobox);
    }

    private void updatedCombobox(ItemEvent e) {
        if (managedCombobox.getSelectedItem() != null
                && managedCombobox.getSelectedItem() != currentUnit) {
            setCurrentUnit((LengthUnit) managedCombobox.getSelectedItem());
        }
    }

}
