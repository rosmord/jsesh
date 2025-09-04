package jsesh.drawingspecifications;

import java.awt.print.PageFormat;

public record PageSpecifications(
		float textWidth,
		float textHeight,
		float leftMargin,
		float rightMargin,
		PageFormat format // Maybe replace with something not java.awt related.
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
		private float rightMargin;
		private PageFormat format;

		public Builder(PageSpecifications specs) {
			this.textWidth = specs.textWidth;
			this.textHeight = specs.textHeight;
			this.leftMargin = specs.leftMargin;
			this.rightMargin = specs.rightMargin;
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

		public Builder rightMargin(float rightMargin) {
			this.rightMargin = rightMargin;
			return this;
		}

		public Builder format(PageFormat format) {
			this.format = format;
			return this;
		}

		public PageSpecifications build() {
			return new PageSpecifications(textWidth, textHeight, leftMargin, rightMargin, format);
		}
	}
}
