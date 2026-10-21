package org.qenherkhopeshef.viewtoolkit1.drawing.element;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;

import org.qenherkhopeshef.swingUtils.DoubleDimensions;

/// An element displaying a bitmap picture.
///
/// The picture can be displayed at a size different from its pixel size: see
/// [#ImageElement(BufferedImage, double, double)].
///
/// @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
public class ImageElement extends GraphicalElement {

	private final BufferedImage image;

	private final double displayWidth;

	private final double displayHeight;

	/// Displays the image at its natural size.
	public ImageElement(BufferedImage image) {
		this(image, Double.MAX_VALUE, Double.MAX_VALUE);
	}

	/// Displays the image, scaled down (keeping its aspect ratio) if needed so
	/// that it fits in a maxWidth x maxHeight box.
	public ImageElement(BufferedImage image, double maxWidth, double maxHeight) {
		this.image = image;
		double w = image.getWidth();
		double h = image.getHeight();
		double scale = Math.min(1, Math.min(maxWidth / w, maxHeight / h));
		this.displayWidth = w * scale;
		this.displayHeight = h * scale;
	}

	@Override
	protected void drawElement(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(image, 0, 0, (int) Math.round(displayWidth),
				(int) Math.round(displayHeight), null);
	}

	@Override
	public Dimension2D getPreferredSize() {
		return new DoubleDimensions(displayWidth, displayHeight);
	}

	public BufferedImage getImage() {
		return image;
	}
}
