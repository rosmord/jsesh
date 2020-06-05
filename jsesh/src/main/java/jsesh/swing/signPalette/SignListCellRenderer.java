package jsesh.swing.signPalette;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import jsesh.hieroglyphs.graphics.HieroglyphicBitmapBuilder;

@SuppressWarnings("serial")
public class SignListCellRenderer extends DefaultListCellRenderer {

	private int border = 2;
	private int bitmapHeight = 20;
	private Component parent = null;
	private boolean displaySignsCodes = false;

	public SignListCellRenderer(Component parent) {
		super();
		this.parent = parent;
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		Component result = super.getListCellRendererComponent(list, value,
				index, isSelected, cellHasFocus);
		setValue(value);
		return result;
	}

	/**
	 * 
	 */
	protected void setValue(Object value) {
		if (value instanceof String) {

			String code = (String) value;
			if (displaySignsCodes)
				setText(code);
			else
				setText("");
			setIcon(HieroglyphicBitmapBuilder.createHieroglyphIcon(code,
					bitmapHeight, border, parent));
		} else if (value instanceof StringBuffer) {
			setText(value.toString());
			setIcon(null);
		} else {
			setText("");
			setIcon(null);
		}
	}

	public int getBitmapHeight() {
		return bitmapHeight;
	}

	public void setBitmapHeight(int bitmapHeight) {
		this.bitmapHeight = bitmapHeight;
	}

	public void setDisplaySignsCodes(boolean displaySignsCodes) {
		this.displaySignsCodes = displaySignsCodes;
	}
}
