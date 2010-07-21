package jsesh.utilitySoftwares.signInfoEditor.ui;

import javax.swing.table.DefaultTableCellRenderer;

import jsesh.hieroglyphs.HieroglyphicBitmapBuilder;

public class HieroglyphicCodeRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8259595791768407149L;
	private int bitmapSize = 30;
	private int bitmapBorder = 4;

	
	public HieroglyphicCodeRenderer(int bitmapSize, int bitmapBorder) {
		super();
		this.bitmapSize = bitmapSize;
		this.bitmapBorder = bitmapBorder;
	}


	protected void setValue(Object value) {
		String code= (String) value;
		setText(code);
		if (code != null && ! "".equals(code))
			setIcon(HieroglyphicBitmapBuilder.createHieroglyphIcon(code, bitmapSize-2*bitmapBorder, bitmapBorder, this));
		else
			setIcon(null);
	}

	

}
