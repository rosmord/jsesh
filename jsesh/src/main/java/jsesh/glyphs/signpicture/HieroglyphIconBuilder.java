/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
*/
package jsesh.glyphs.signpicture;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.JLabel;

import jsesh.glyphs.fonts.HieroglyphShapeRepository;
import jsesh.glyphs.shape.ShapeChar;
import jsesh.signcodes.CanonicalCode;
import jsesh.signcodes.ManuelDeCodage;
import jsesh.utils.swing.GraphicsUtils;

/**
 * An helper class to create bitmap/icons versions of single signs.
 * 
 * <p>
 * Note about <code>fit</code> : Normally, the
 * height of the sign is scaled to fit the height of the
 * <code>PictureDimension</code>.
 * If the sign is too wide :
 * <ul>
 * <li>if <code>fit</code> is false, the system will produce a bitmap
 * with a <em>larger</em> width than requested.
 * <li>if <code>fit</code> is true, the system will reduce the scale of the sign
 * to fit the width of the bitmap.
 * </ul>
 * 
 * @author rosmord
 * 
 */
public class HieroglyphIconBuilder {

	/**
	 * A component which can be used to build compatible images.
	 */
	private Component referenceComponent = null;

	/**
	 * A reference component used to build picture which fit with the current
	 * display.
	 */
	private HieroglyphShapeRepository shapeRepository;

	/**
	 * Height of A1.
	 */
	private double a1Height;

	/**
	 * 
	 * @param shapeRepository
	 * @param referenceComponent the container that will display the icons, or null
	 *                           if none is known. May speed up display if correctly
	 *                           set.
	 */
	public HieroglyphIconBuilder(HieroglyphShapeRepository shapeRepository, Component referenceComponent) {
		this.shapeRepository = shapeRepository;
		ShapeChar a1 = shapeRepository.get(ManuelDeCodage.getInstance()
				.getA1Code());
		a1Height = a1.getBbox().getHeight();
		this.referenceComponent = referenceComponent;
	}

	/**
	 * Build a bitmap for a given sign.
	 * 
	 * @param code    the sign code.
	 * @param options rendering options.
	 * @return
	 */
	public BufferedImage buildSignBitmap(CanonicalCode code,
			IconRenderOptions options) {
		return buildSignBitmap(getShapeOrA1(code), options);
	}

	/**
	 * Utility method for drawing an icon in a label.
	 * 
	 * @param label
	 * @param code
	 * @param padding
	 */
	public void drawIconInLabel(JLabel label, CanonicalCode code, int padding) {
		IconRenderOptions renderOptions = IconRenderOptions.DEFAULT.copy()
				.dimension(computeIconDimensionFor(label, padding))
				.fit(true)
				.padding(padding).build();
		label.setIcon(createHieroglyphIcon(code, renderOptions));
	}

	/**
	 * Compute the dimension for an icon displayed in a fixed size label.
	 * 
	 * @param signIconLabel
	 * @return
	 */
	private PictureDimension computeIconDimensionFor(JLabel signIconLabel, int padding) {
		int height = signIconLabel.getHeight();// - 2 * padding;
		int width = signIconLabel.getWidth();// - 2 * padding;
		if (width <= 0 || height <= 0) {
			return IconRenderOptions.DEFAULT.dimension();
		} else {
			return new PictureDimension(width, height);
		}
	}

	/// Creates an icon for a particular hieroglyph.
	///
	/// The icon is a vector one: the sign is drawn when the icon is painted, so it
	/// stays sharp on high resolution screens. If the sign is unknown, A1 is used.
	///
	/// @param code    the sign code.
	/// @param options rendering options.
	/// @return an icon for the glyph.
	public Icon createHieroglyphIcon(CanonicalCode code, IconRenderOptions options) {
		ShapeChar shape = getShapeOrA1(code);
		return new HieroglyphIcon(shape, computeLayout(shape, options), options.transparent());
	}

	private boolean hasCode(CanonicalCode code) {
		return shapeRepository.get(code) != null;
	}

	private ShapeChar getShapeOrA1(CanonicalCode code) {
		CanonicalCode actualCode = hasCode(code) ? code : ManuelDeCodage.getInstance().getA1Code();
		return shapeRepository.get(actualCode);
	}

	private BufferedImage buildSignBitmap(ShapeChar glyph, IconRenderOptions options) {

		// Simple stuff : colors.
		int colorModel = BufferedImage.TYPE_BYTE_GRAY;
		Color backGroundColor = Color.WHITE;

		if (options.transparent()) {
			colorModel = BufferedImage.TYPE_INT_ARGB;
			backGroundColor = new Color(0, 0, 0, 0);
		}
		SignPictureLayout layout = computeLayout(glyph, options);
		BufferedImage img = buildCompatibleBitmap(colorModel, layout.width(), layout.height(), options.transparent());
		drawToBitmap(img, glyph, backGroundColor, layout);
		return img;
	}

	/// Computes the size of the picture and the placement of the sign in it.
	private SignPictureLayout computeLayout(ShapeChar glyph, IconRenderOptions options) {
		// will be the actual dimensions of the picture in the end.
		int width = options.dimension().width();
		int height = options.dimension().height();

		// the projected inner dimensions of the drawing itself (excluding padding).
		PictureDimension innerDimensions = options.innerDimension();

		// the final scale.
		double scale;

		Rectangle2D glyphBBox = glyph.getBbox();

		// First, compute the starting scale, using only sign height.
		double startingScale = innerDimensions.height() / a1Height;
		// Compute the sign size using this scale.
		double projectedWidth = startingScale * glyphBBox.getWidth();
		double projectedHeight = startingScale * glyphBBox.getHeight();
		// Now, see if it fits in the current picture. If not, proceed depending on the
		// fit option.
		if (projectedWidth > innerDimensions.width() || projectedHeight > innerDimensions.height()) {
			if (options.fit()) {
				// Reduce the scale to fit the dimensions, using the sign's actual bounding box.
				// we don't care about the original scale: it was too large anyway.
				scale = Math.min(innerDimensions.width() / glyphBBox.getWidth(),
						innerDimensions.height() / glyphBBox.getHeight());
			} else {
				// Keep the scale, modify the picture dimensions to fit the sign.
				scale = startingScale;
				width = options.padding() + (int) (1 + projectedWidth);
				height = options.padding() + (int) (1 + projectedHeight);
			}
		} else {
			scale = startingScale;
		}
		double xOrigin = (width - scale * glyphBBox.getWidth()) / 2.0;
		double yOrigin = (height - scale * glyphBBox.getHeight()) / 2.0;
		return new SignPictureLayout(width, height, scale, xOrigin, yOrigin);
	}

	/// Draws a centered glyph in a bitmap.
	/// Basic utility method, called when everything is computed.
	///
	/// @param img             the picture to draw on.
	/// @param glyph           the glyph to draw.
	/// @param backGroundColor the background color to use for the bitmap.
	/// @param layout          the size of the picture and the placement of the sign.
	private void drawToBitmap(BufferedImage img, ShapeChar glyph, Color backGroundColor, SignPictureLayout layout) {
		Graphics2D g = img.createGraphics();
		GraphicsUtils.antialias(g);
		g.setBackground(backGroundColor);
		g.setColor(Color.BLACK);
		g.clearRect(0, 0, img.getWidth(), img.getHeight());
		glyph.draw(g, layout.xOrigin(), layout.yOrigin(), layout.scale(), layout.scale(), 0);
		g.dispose();
	}

	/**
	 * Build a bitmap of a given size, compatible either with the current display or
	 * a selected color model.
	 * 
	 * <p>
	 * If a reference component is set, its color model will be used instead of the
	 * one passed as parameter.
	 * 
	 * @param colorModel a color model constant (e.g. BufferedImage.TYPE_INT_ARGB)
	 * @param width
	 * @param height
	 * @return
	 */
	private BufferedImage buildCompatibleBitmap(int colorModel, int width, int height, boolean transparent) {
		if (referenceComponent != null && referenceComponent.getGraphicsConfiguration() != null) {
			return referenceComponent.getGraphicsConfiguration()
					.createCompatibleImage(width, height, transparent ? Transparency.TRANSLUCENT : Transparency.OPAQUE);
		} else {
			return new BufferedImage(width, height, colorModel);
		}
	}

}
