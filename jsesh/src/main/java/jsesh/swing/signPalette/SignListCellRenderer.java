package jsesh.swing.signPalette;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import jsesh.hieroglyphs.fonts.HieroglyphShapeRepository;
import jsesh.hieroglyphs.utils.HieroglyphPictureBuilder;
import jsesh.hieroglyphs.utils.IconRenderOptions;
import jsesh.hieroglyphs.utils.PictureDimension;

@SuppressWarnings("serial")
public class SignListCellRenderer extends DefaultListCellRenderer {

	private int border = 3;
	private int bitmapHeight = 28;
	private Component parent = null;
	private boolean displaySignsCodes = false;
	private HieroglyphShapeRepository hieroglyphicFontManager;

	public SignListCellRenderer(Component parent, HieroglyphShapeRepository hieroglyphicFontManager) {		
		this.parent = parent;
		this.hieroglyphicFontManager = hieroglyphicFontManager;
	}

	public Component getListCellRendererComponent(JList<?> list, Object value,
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
			if (displaySignsCodes) {
				setText(code);
			} else {
				setText("");
			}
			HieroglyphPictureBuilder hieroglyphPictureBuilder =  new HieroglyphPictureBuilder(hieroglyphicFontManager, parent);
			IconRenderOptions renderOptions = IconRenderOptions.DEFAULT.copy()
			.fit(true)
			.dimension(getIconSize()).padding(border).build();
			setIcon(hieroglyphPictureBuilder.createHieroglyphIcon(code, renderOptions));
		} else if (value instanceof StringBuffer) {
			setText(value.toString());
			setIcon(null);
		} else {
			setText("");
			setIcon(null);
		}
	}

	// TODO : unify the computation of icon size aspect ratio.
	private PictureDimension getIconSize() {
		int height = bitmapHeight ;
		int width = Math.ceilDiv(height*13, 10);
		return new PictureDimension(width, height);
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
