package jsesh.swing.signPalette;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.qenherkhopeshef.swingUtils.lists.DataListItem;
import org.qenherkhopeshef.swingUtils.lists.LabelListItem;

import jsesh.glyphs.data.coremdc.CanonicalCode;
import jsesh.glyphs.data.coremdc.ManuelDeCodage;
import jsesh.glyphs.fonts.HieroglyphShapeRepository;
import jsesh.glyphs.tools.HieroglyphPictureBuilder;
import jsesh.glyphs.tools.IconRenderOptions;
import jsesh.glyphs.tools.PictureDimension;

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
		if (value instanceof String code) {
			renderCode(code);
		} else if (value instanceof DataListItem dataItem) {
			renderCode(dataItem.toString());
		} else if (value instanceof LabelListItem labelItem) {
			setText(labelItem.getValue());
			setIcon(null);
		} else {
			setText("");
			setIcon(null);
		}
	}

	private void renderCode(String code) {
		if (displaySignsCodes) {
			setText(code);
		} else {
			setText("");
		}
		HieroglyphPictureBuilder hieroglyphPictureBuilder = new HieroglyphPictureBuilder(hieroglyphicFontManager,
				parent);
		IconRenderOptions renderOptions = IconRenderOptions.DEFAULT.copy()
				.fit(true)
				.dimension(getIconSize()).padding(border).build();
		CanonicalCode canonicalCode = ManuelDeCodage.getInstance().getCanonicalCode(code);				
		setIcon(hieroglyphPictureBuilder.createHieroglyphIcon(canonicalCode, renderOptions));
	}

	// TODO : unify the computation of icon size aspect ratio.
	private PictureDimension getIconSize() {
		int height = bitmapHeight;
		int width = Math.ceilDiv(height * 13, 10);
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
