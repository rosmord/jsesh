package jsesh.drawingspecifications;

import java.awt.Color;
import java.util.Map;

/**
 * Specifications for actual drawing; mainly colors but also shading.
 * 
 * @param blackColor the color to use for normal text.
 * @param redColor the color to use for red text.
 * @param cursorColor the color to use for the cursor.
 * @param grayColor the color to use for shading.
 * @param backgroundColor the color to use for the background.
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
		Map<String, Color> colorMap) {

	// Record constructor, which ensures the map is unmodifiable.
	public PaintingSpecifications {
		colorMap = Map.copyOf(colorMap);
	}

	public static final PaintingSpecifications DEFAULT = new PaintingSpecifications(
			Color.BLACK,
			Color.RED,
			Color.BLUE,
			Color.LIGHT_GRAY,
			Color.WHITE,
			ShadingMode.GRAY_SHADING,
			Map.of()
			);

	
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
		private Map<String, Color> colorMap;
		
		public Builder(PaintingSpecifications specs) {
			this.blackColor = specs.blackColor;
			this.redColor = specs.redColor;
			this.cursorColor = specs.cursorColor;
			this.grayColor = specs.grayColor;
			this.backgroundColor = specs.backgroundColor;
			this.shadingStyle = specs.shadingStyle;
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

		public PaintingSpecifications build() {
			return new PaintingSpecifications(blackColor, redColor, cursorColor, grayColor, backgroundColor, shadingStyle, colorMap);
		}
	}
}
