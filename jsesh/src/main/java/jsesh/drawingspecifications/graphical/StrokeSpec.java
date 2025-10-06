package jsesh.drawingspecifications.graphical;

/**
 * Specification for rendering strokes.
 */
public record StrokeSpec(
		float fineLineWidth,
		float fineLigneHeight,
		ShadingStyle shadingStyle
		) {

	public Builder copy() {
		return new Builder(this);
	}

	/** copy builder class */
	public static class Builder {
		private float fineLineWidth;
		private float fineLigneHeight;
		private ShadingStyle shadingStyle;

		public Builder(StrokeSpec specs) {
			this.fineLineWidth = specs.fineLineWidth;
			this.fineLigneHeight = specs.fineLigneHeight;
			this.shadingStyle = specs.shadingStyle;
		}

		public Builder fineLineWidth(float fineLineWidth) {
			this.fineLineWidth = fineLineWidth;
			return this;
		}

		public Builder fineLigneHeight(float fineLigneHeight) {
			this.fineLigneHeight = fineLigneHeight;
			return this;
		}

		public Builder shadingStyle(ShadingStyle shadingStyle) {
			this.shadingStyle = shadingStyle;
			return this;
		}

		public StrokeSpec build() {
			return new StrokeSpec(fineLineWidth, fineLigneHeight, shadingStyle);
		}
	}
}
