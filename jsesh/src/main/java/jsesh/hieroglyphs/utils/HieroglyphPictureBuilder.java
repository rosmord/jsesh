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
package jsesh.hieroglyphs.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import jsesh.hieroglyphs.fonts.HieroglyphShapeRepository;
import jsesh.hieroglyphs.signshape.ShapeChar;
import jsesh.swing.utils.GraphicsUtils;

/**
 * An helper class to create bitmap/icons versions of single signs.
 * 
 * TODO : rename as HieroglyphIconCreator.
 * 
 * @author rosmord
 * 
 */
public class HieroglyphPictureBuilder {

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
	 * The scale from sign space to bitmap space.
	 * 
	 * @return the scale to apply to sign coordinate to get bitmap coordinates.
	 */
	public double scaleForSize(double size) {
		return size / a1Height;
	}

	// The old code for fixing and chosing the right constructor.

	// public static Icon createHieroglyphIcon(String code, int size, int border,
	// Component container) {
	// // HieroglyphicBitmapBuilder builder = new HieroglyphicBitmapBuilder(20,
	// // 30, false);
	// HieroglyphicBitmapBuilder builder = new HieroglyphicBitmapBuilder();
	// builder.setSize(size);
	// builder.setTransparent(false);
	// builder.setBorder(border);
	// builder.setFit(true);
	// builder.setComponent(container);

	/**
	 * 
	 * @param shapeRepository
	 * @param referenceComponent the container that will display the icons, or null
	 *                           if none is known. May speed up display if correctly
	 *                           set.
	 */
	public HieroglyphPictureBuilder(HieroglyphShapeRepository shapeRepository, Component referenceComponent) {
		this.shapeRepository = shapeRepository;
		ShapeChar a1 = shapeRepository.get("A1");
		a1Height = a1.getBbox().getHeight();
		this.referenceComponent = referenceComponent;
	}

	/**
	 * Build a bitmap for a given sign.
	 * 
	 * @param code
	 * @param options
	 * @return
	 */
	public BufferedImage buildSignBitmap(String code,
			IconRenderOptions options) {
		String actualCode = hasCode(code) ? code : "A1";
		ShapeChar shape = shapeRepository.get(actualCode);
		return buildSignBitmap(shape, options);
	}

	/**
	 * Creates an icon for a particular hieroglyph.
	 * 
	 * @param code
	 * @return an icon for the glyph.
	 */
	public Icon createHieroglyphIcon(String code, IconRenderOptions options) {
		BufferedImage img = buildSignBitmap(code, options);
		return new ImageIcon(img);
	}

	private boolean hasCode(String code) {
		return shapeRepository.get(code) != null;
	}

	private BufferedImage buildSignBitmap(ShapeChar glyph, IconRenderOptions options) {
		// NOTE: maxSize = A1.getBbox().getHeight();

		// Compute the scale
		double scale = scaleForSize(options.size());

		int colorModel = BufferedImage.TYPE_BYTE_GRAY;
		Color backGroundColor = Color.WHITE;

		if (options.transparent()) {
			colorModel = BufferedImage.TYPE_INT_ARGB;
			backGroundColor = new Color(0, 0, 0, 1);
		}

		int actualWidth = options.size();
		int actualHeight = options.size();

		if (options.fit()) {
			actualWidth = options.border()
					+ (int) (1 + glyph.getBbox().getWidth() * scale);
			actualHeight = options.border()
					+ (int) (1 + glyph.getBbox().getHeight() * scale);
		}

		double mx = (actualWidth - scale * glyph.getBbox().getWidth()) / 2.0;
		double my = (actualHeight - scale * glyph.getBbox().getHeight()) / 2.0;

		BufferedImage img;
		if (referenceComponent != null && referenceComponent.getGraphicsConfiguration() != null) {
			img = referenceComponent.getGraphicsConfiguration().createCompatibleImage(
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
}
