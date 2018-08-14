/*
 * Created on 19 oct. 2004
 *
 */
package jsesh.graphics.export.html;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A field with an associated label.
 *
 * TODO : remove this class (a good layout will make it obsolete).
 * @author rosmord
 */
public class LabeledField extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1431028853567089282L;

    public static final String NORTH = BorderLayout.NORTH;
    public static final String SOUTH = BorderLayout.SOUTH;
    public static final String EAST = BorderLayout.EAST;
    public static final String WEST = BorderLayout.WEST;

    public JLabel label;
    public JComponent field;

    /**
     * build a field with a label on its left.
     *
     * @param label
     * @param jfield
     */
    public LabeledField(String label, JComponent jfield) {
        this(label, jfield, WEST);
    }

    /**
     * Build a field with a label
     *
     * @param label
     * @param jfield
     * @param position position of the label ; one of NORTH, SOUTH, WEST, EAST
     * (use the constants for this class).
     */
    public LabeledField(String label, JComponent jfield, String position) {
        this.field = jfield;
        this.label = new JLabel(label);
        setLayout(new BorderLayout());
        add(this.label, position);
        add(field, BorderLayout.CENTER);
    }

    /**
     * @return the field component.
     */
    public JComponent getField() {
        return field;
    }

    /**
     * @return the label component.
     */
    public JLabel getLabel() {
        return label;
    }
}
