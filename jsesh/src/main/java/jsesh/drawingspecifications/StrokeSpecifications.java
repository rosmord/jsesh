package jsesh.drawingspecifications;

/**
 * Note : should probably be renamed as drawing specifications ?
 */
public record StrokeSpecifications(
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

		public Builder(StrokeSpecifications specs) {
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

		public StrokeSpecifications build() {
			return new StrokeSpecifications(fineLineWidth, fineLigneHeight, shadingStyle);
		}
	}
}
