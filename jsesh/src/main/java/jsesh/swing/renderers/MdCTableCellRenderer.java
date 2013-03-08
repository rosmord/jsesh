package jsesh.swing.renderers;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.table.DefaultTableCellRenderer;

import jsesh.swing.utils.ImageIconFactory;

/**
 * Renders mdc codes as hieroglyphic texts in tables.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 * 
 */
@SuppressWarnings("serial")
public class MdCTableCellRenderer extends DefaultTableCellRenderer {

	@Override
	protected void setValue(Object value) {
		if (value instanceof String) {
			String mdc= (String) value;
			setIcon(ImageIconFactory.buildImage(mdc));
		}
	}

}
