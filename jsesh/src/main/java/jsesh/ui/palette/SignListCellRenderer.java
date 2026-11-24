package jsesh.ui.palette;

import java.awt.Component;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;

import org.qenherkhopeshef.swingUtils.lists.DataListItem;
import org.qenherkhopeshef.swingUtils.lists.LabelListItem;

import jsesh.glyphs.fonts.HieroglyphShapeRepository;
import jsesh.glyphs.signpicture.HieroglyphIconBuilder;
import jsesh.glyphs.signpicture.IconRenderOptions;
import jsesh.glyphs.signpicture.PictureDimension;
import jsesh.signcodes.CanonicalCode;
import jsesh.signcodes.ManuelDeCodage;

@SuppressWarnings("serial")
public class SignListCellRenderer extends DefaultListCellRenderer {

	private int border = 3;
	private int bitmapHeight = 28;
	private Component parent = null;
	private boolean displaySignsCodes = false;
	private HieroglyphShapeRepository hieroglyphicFontManager;
	/// Maximal number of icons kept in [#iconCache].
	private static final int ICON_CACHE_SIZE = 600;

	/// Icons already built, by sign code, for the current bitmap height.
	/// Cleared when the height or the sign shapes change.
	///
	/// It's a bounded LRU cache: with the "all signs" list and large icons, keeping
	/// every bitmap would cost too much memory.
	private final Map<String, Icon> iconCache = new LinkedHashMap<>(64, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, Icon> eldest) {
			return size() > ICON_CACHE_SIZE;
		}
	};

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
		setIcon(iconCache.computeIfAbsent(code, this::buildIcon));
	}

	private Icon buildIcon(String code) {
		HieroglyphIconBuilder hieroglyphIconBuilder = new HieroglyphIconBuilder(hieroglyphicFontManager,
				parent);
		IconRenderOptions renderOptions = IconRenderOptions.DEFAULT.copy()
				.fit(true)
				.dimension(getIconSize()).padding(border).build();
		CanonicalCode canonicalCode = ManuelDeCodage.getInstance().getCanonicalCode(code);
		return hieroglyphIconBuilder.createHieroglyphIcon(canonicalCode, renderOptions);
	}

	/// Forgets the icons already built (call it when the sign shapes change).
	public void clearIconCache() {
		iconCache.clear();
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
		if (this.bitmapHeight != bitmapHeight) {
			this.bitmapHeight = bitmapHeight;
			iconCache.clear();
		}
	}

	public void setDisplaySignsCodes(boolean displaySignsCodes) {
		this.displaySignsCodes = displaySignsCodes;
	}
}
