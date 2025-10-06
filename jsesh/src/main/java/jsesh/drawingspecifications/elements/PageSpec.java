package jsesh.drawingspecifications.elements;

import java.awt.print.PageFormat;

/**
 * Specification for page layout.
 * In the current version, the default are computed from the other specifications.
 * The whole specification system should be rethought to use independent values.
 * Note clean at all. Should be replaced.
 * 
 * <p>This class has no default value, as it is computed from other specifications.
 */
public record PageSpec(
		float textWidth,
		float textHeight,
		float leftMargin,
		float topMargin,
		PageFormat format // Maybe replace with something not java.awt related. Can be null.
) {


	public Builder copy() {
		return new Builder(this);
	}

	/**
	 * Copy Builder class.
	 */

	public static class Builder {
		private float textWidth;
		private float textHeight;
		private float leftMargin;
		private float topMargin;
		private PageFormat format;

		public Builder(PageSpec specs) {
			this.textWidth = specs.textWidth;
			this.textHeight = specs.textHeight;
			this.leftMargin = specs.leftMargin;
			this.topMargin = specs.topMargin;
			this.format = specs.format;
		}

		public Builder textWidth(float textWidth) {
			this.textWidth = textWidth;
			return this;
		}

		public Builder textHeight(float textHeight) {
			this.textHeight = textHeight;
			return this;
		}

		public Builder leftMargin(float leftMargin) {
			this.leftMargin = leftMargin;
			return this;
		}

		public Builder topMargin(float topMargin) {
			this.topMargin = topMargin;
			return this;
		}

		public Builder format(PageFormat format) {
			this.format = format;
			return this;
		}

		public PageSpec build() {
			return new PageSpec(textWidth, textHeight, leftMargin, topMargin, format);
		}
	}
}
