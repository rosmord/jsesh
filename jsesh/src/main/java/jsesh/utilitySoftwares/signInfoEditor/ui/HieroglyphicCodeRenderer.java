package jsesh.utilitySoftwares.signInfoEditor.ui;

import javax.swing.table.DefaultTableCellRenderer;

import jsesh.hieroglyphs.graphics.HieroglyphicBitmapBuilder;

/**
 * Renders <em>one</em> sign
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 *
 * The sign is given  by its mdc code.
 */
@SuppressWarnings("serial")
public class HieroglyphicCodeRenderer extends DefaultTableCellRenderer {

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
