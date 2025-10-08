package jsesh.drawingspecifications;

import java.awt.Color;
import java.util.Map;

/**
 * Color specifications.
 * 
 * @param blackColor the color to use for normal text.
 * @param redColor the color to use for red text.
 * @param cursorColor the color to use for the cursor.
 * @param grayColor the color to use for shading.
 * @param backgroundColor the color to use for the background.
 * @param colorMap a color map (which should not be modifiable).
 * @author Serge Rosmorduc
 */
public record ColorSpecification(
		Color blackColor,
		Color redColor,
		Color cursorColor,
		Color grayColor,
		Color backgroundColor,
		Map<String, Color> colorMap) {

	// Record constructor, which ensures the map is unmodifiable.
	public ColorSpecification {
		colorMap = Map.copyOf(colorMap);
	}

	public static final ColorSpecification DEFAULT = new ColorSpecification(
			Color.BLACK,
			Color.RED,
			Color.BLUE,
			Color.LIGHT_GRAY,
			Color.WHITE,
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
		private Map<String, Color> colorMap;

		public Builder(ColorSpecification specs) {
			this.blackColor = specs.blackColor;
			this.redColor = specs.redColor;
			this.cursorColor = specs.cursorColor;
			this.grayColor = specs.grayColor;
			this.backgroundColor = specs.backgroundColor;
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

		public ColorSpecification build() {
			return new ColorSpecification(blackColor, redColor, cursorColor, grayColor, backgroundColor, colorMap);
		}
	}
}
