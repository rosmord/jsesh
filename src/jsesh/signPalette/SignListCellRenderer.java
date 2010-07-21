package jsesh.signPalette;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import jsesh.hieroglyphs.HieroglyphicBitmapBuilder;

public class SignListCellRenderer extends DefaultListCellRenderer {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3108075866784168186L;
	private int border= 2;
	private int bitmapHeight=20;
	private Component parent= null;

	
	public SignListCellRenderer(Component parent) {
		super();
		this.parent = parent;
	}

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        setValue(value);
        return result;
    }




	/**
	 * 
	 */
	protected void setValue(Object value) {
		if (value instanceof String) {
			
			String code = (String) value;
			setText("");
			setIcon(HieroglyphicBitmapBuilder.createHieroglyphIcon(code, bitmapHeight, border, parent));			
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
	
}
