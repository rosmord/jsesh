package jsesh.render.style;

import java.awt.Color;
import java.util.Map;

/**
 * Specifications for actual drawing; mainly colors but also shading.
 * 
 * The colormap is used to map from tag names to colors. Tags are added to signs with the notation <code>SIGNCODE\TAGNAME</code>, for instance
 * <code>A1\det</code> to mark that A1 is a determinative.
 * 
 * @param blackColor the color to use for normal text.
 * @param redColor the color to use for red text.
 * @param cursorColor the color to use for the cursor.
 * @param grayColor the color to use for shading.
 * @param backgroundColor the color to use for the background.
 * @param shadingStyle how shaded areas are drawn (gray or hatching).
 * @param underlineRed if true, the red quadrats of the text (those in a red
 *        part, `$r`) are drawn in black and underlined, as in monochrome
 *        typography. Red signs inside a quadrat (`\red` modifier) are then
 *        simply drawn in black.
 * @param colorMap a color map (which should not be modifiable).
 * @author Serge Rosmorduc
 */
public record PaintingSpecifications(
		Color blackColor,
		Color redColor,
		Color cursorColor,
		Color grayColor,
		Color backgroundColor,
		ShadingMode shadingStyle,
		boolean underlineRed,
		Map<String, Color> colorMap) {

	/**
	 * Default gray suitable for shading.
	 */
	public static final Color DEFAULT_GRAY_COLOR = new Color(0.6f, 0.6f, 0.6f, 0.3f);


	// Record constructor, which ensures the map is unmodifiable.
	public PaintingSpecifications {
		colorMap = Map.copyOf(colorMap);
	}

	public static final PaintingSpecifications DEFAULT = new PaintingSpecifications(
			Color.BLACK,
			Color.RED,
			Color.BLUE,
			DEFAULT_GRAY_COLOR,
			Color.WHITE,
			ShadingMode.GRAY_SHADING,
			false,
			Map.of()
			);

	/**
	 * Returns the color for a given tag, as stored in the color map.
	 * @param tagName name of the tag
	 * @return the color, if any
	 */
	public java.util.Optional<Color> tagColor(String tagName) {
		return java.util.Optional.ofNullable(colorMap.get(tagName));
	}

	public Builder copy() {
		return new Builder(this);
	}

	/** copy builder class */
	public static class Builder {
		private Color blackColor;
		private Color redColor;
		private Color cursorColor;
		private Color grayColor;
		private Color backgroundColor;
		private ShadingMode shadingStyle;
		private boolean underlineRed;
		private Map<String, Color> colorMap;
		
		public Builder(PaintingSpecifications specs) {
			this.blackColor = specs.blackColor;
			this.redColor = specs.redColor;
			this.cursorColor = specs.cursorColor;
			this.grayColor = specs.grayColor;
			this.backgroundColor = specs.backgroundColor;
			this.shadingStyle = specs.shadingStyle;
			this.underlineRed = specs.underlineRed;
			this.colorMap = specs.colorMap;
		}

		public Builder blackColor(Color blackColor) {
			this.blackColor = blackColor;
			return this;
		}

		public Builder redColor(Color redColor) {
			this.redColor = redColor;
			return this;
		}

		public Builder cursorColor(Color cursorColor) {
			this.cursorColor = cursorColor;
			return this;
		}

		public Builder grayColor(Color grayColor) {
			this.grayColor = grayColor;
			return this;
		}

		public Builder backgroundColor(Color backgroundColor) {
			this.backgroundColor = backgroundColor;
			return this;
		}

		public Builder colorMap(Map<String, Color> colorMap) {
			this.colorMap = colorMap;
			return this;
		}

		public Builder shadingStyle(ShadingMode shadingStyle) {
			this.shadingStyle = shadingStyle;
			return this;
		}

		public Builder underlineRed(boolean underlineRed) {
			this.underlineRed = underlineRed;
			return this;
		}

		public PaintingSpecifications build() {
			return new PaintingSpecifications(blackColor, redColor, cursorColor, grayColor, backgroundColor, shadingStyle, underlineRed, colorMap);
		}
	}
}
