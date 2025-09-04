package jsesh.drawingspecifications;

import java.awt.Color;
import java.util.Map;

public record ColorSpecifications(
		Color blackColor,
		Color redColor,
		Color cursorColor,
		Color grayColor,
		Color backgroundColor,
		/**
		 * An unmodifiable map (should really be immutable).
		 */
		Map<String, Color> colorMap) {

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

		public Builder(ColorSpecifications specs) {
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

		public ColorSpecifications build() {
			return new ColorSpecifications(blackColor, redColor, cursorColor, grayColor, backgroundColor, colorMap);
		}
	}
}
