package jsesh.mdcDisplayer.swing.palette;

import java.awt.Component;

import javax.swing.table.DefaultTableCellRenderer;

import jsesh.hieroglyphs.HieroglyphicBitmapBuilder;

public class SignTableCellRenderer extends DefaultTableCellRenderer {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3108075866784168186L;
	private int border= 2;
	private int bitmapHeight=20;
	private Component parent= null;

	
	public SignTableCellRenderer(Component parent) {
		super();
		this.parent = parent;
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
