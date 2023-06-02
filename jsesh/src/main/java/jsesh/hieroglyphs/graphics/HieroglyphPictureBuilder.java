package jsesh.hieroglyphs.graphics;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import jsesh.swing.utils.GraphicsUtils;

import jsesh.swing.utils.ImageIconFactory;

/**
 * An helper class to create bitmap versions of single signs.
 * 
 * @author rosmord
 * 
 */
public class HieroglyphPictureBuilder {

	/**
	 * The size of the bitmap
	 */
	private int size;

	/**
	 * the size of the hightest sign, in model coordinate.
	 */
	private double maxSize;

	/**
	 * Do we want a transparent picture ?
	 */
	private boolean transparent;

	/**
	 * Are the bitmaps fitted to the signs ?
	 */
	private boolean fit;

	/**
	 * A component which can be used to build compatible images.
	 */
	private Component component = null;

	private int border;

	private HieroglyphicFontManager fontManager;

	public HieroglyphPictureBuilder(HieroglyphicFontManager hieroglyphicFontManager) {
		this.fontManager = hieroglyphicFontManager;
		size = 50;
		transparent = false;
		border = 4;
	}

	/**
	 * 
	 * @param maxSize
	 *            size of the hightest sign, in model coordinates.
	 * @param imageSize
	 *            size of the bitmap.
	 * @param transparent
	 *            should the picture be transparent.
	 */
	public HieroglyphPictureBuilder(
			HieroglyphicFontManager hieroglyphicFontManager,
			double maxSize, int imageSize,
			boolean transparent) {		
		this.fontManager = hieroglyphicFontManager;
		this.maxSize = maxSize;
		this.size = imageSize;
		this.transparent = transparent;
	}


	public boolean hasCode(String code) {
		return fontManager.get(code) != null;
	}
	
	public BufferedImage buildSignBitmap(String code) {
		ShapeChar shape = fontManager.get(code);
		return buildSignBitmap(shape);
	}
	
	private BufferedImage buildSignBitmap(ShapeChar glyph) {
		// Compute the scale
		float scale = (float) (size / maxSize);

		int colorModel = BufferedImage.TYPE_BYTE_GRAY;
		Color backGroundColor = Color.WHITE;

		if (transparent) {
			colorModel = BufferedImage.TYPE_INT_ARGB;
			backGroundColor = new Color(0, 0, 0, 1);
		}

		int actualWidth = size;
		int actualHeight = size;

		if (isFit()) {
			actualWidth = border
					+ (int) (1 + glyph.getBbox().getWidth() * scale);
			actualHeight = border
					+ (int) (1 + glyph.getBbox().getHeight() * scale);
		}

		double mx = (actualWidth - scale * glyph.getBbox().getWidth()) / 2.0;
		double my = (actualHeight - scale * glyph.getBbox().getHeight()) / 2.0;

		BufferedImage img;
		if (component != null && component.getGraphicsConfiguration() != null ) {
			img = component.getGraphicsConfiguration().createCompatibleImage(
					actualWidth, actualHeight);
		} else
			img = new BufferedImage(actualWidth, actualHeight, colorModel);
		Graphics2D g = img.createGraphics();
		GraphicsUtils.antialias(g);
		g.setBackground(backGroundColor);
		g.setColor(Color.BLACK);
		g.clearRect(0, 0, img.getWidth(), img.getHeight());

		glyph.draw(g, mx, my, scale, scale, 0);
		g.dispose();
		return img;
	}

	/**
	 * @return Returns the maxSize.
	 */
	public double getMaxSize() {
		return maxSize;
	}

	/**
	 * the size of the hightest sign, in model coordinate.
	 * 
	 * @param maxSize
	 *            The maxSize to set.
	 */
	public void setMaxSize(double maxSize) {
		this.maxSize = maxSize;
	}

	/**
	 * The size of the bitmap
	 * 
	 * @return Returns the size.
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size
	 *            The size to set.
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * Do we want a transparent picture ?
	 * 
	 * @return Returns the transparent.
	 */
	public boolean isTransparent() {
		return transparent;
	}

	/**
	 * @param transparent
	 *            The transparent to set.
	 */
	public void setTransparent(boolean transparent) {
		this.transparent = transparent;
	}

	/**
	 * requires that the bitmaps will be fitted to the signs.
	 * 
	 * @param fit
	 */
	public void setFit(boolean fit) {
		this.fit = fit;
	}

	/**
	 * Are the bitmaps fitted the the signs ?
	 * 
	 * @return a boolean
	 */
	public boolean isFit() {
		return fit;
	}

	/**
	 * declare that the icon will be used in a given component. If this is used,
	 * the icon will get the correct depth for the component, and rendering will
	 * be faster.
	 * 
	 * @param component
	 */
	public void setComponent(Component component) {
		this.component = component;

	}

	/**
	 * @return Returns the border.
	 */
	public int getBorder() {
		return border;
	}

	/**
	 * @param border
	 *            The border to set.
	 */
	public void setBorder(int border) {
		this.border = border;
	}

	/**
	 * Creates an icon for a particular hieroglyph.
	 * 
	 * @param code
	 * @param size :
	 *            the icon size
	 * @param border :
	 *            the size of the icon's border
	 * @param container
	 *            the container that will display the icons, or null if none is
	 *            known. May speed up display if correctly set.
	 * @return an icon for the glyph.
	 */
	public  Icon createHieroglyphIcon(String code, int size, int border,
			Component container) {
		this.setSize(size);
		this.setTransparent(false);
		this.setBorder(border);
		this.setFit(true);
		this.setComponent(container);

		BufferedImage img;
		if (this.hasCode(code)) {
			 img = this.buildSignBitmap(code);
		} else {
			img = this.buildSignBitmap("A1");
		}
		return new ImageIcon(img);
	}

}
