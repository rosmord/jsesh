package jsesh.drawingspecifications.elementspecification;

public record EnclosureSpecifications(
		float bastionDepth,
		float bastionLength) {

	/** Create a copy. */
	public Builder copy() {
		return new Builder(this);
	}

	/** copy builder class */
	public static class Builder {
		private float bastionDepth;
		private float bastionLength;

		public Builder(EnclosureSpecifications specs) {
			this.bastionDepth = specs.bastionDepth;
			this.bastionLength = specs.bastionLength;
		}

		public Builder bastionDepth(float bastionDepth) {
			this.bastionDepth = bastionDepth;
			return this;

		}

		public Builder bastionLength(float bastionLength) {
			this.bastionLength = bastionLength;
			return this;
		}

		public EnclosureSpecifications build() {
			return new EnclosureSpecifications(bastionDepth, bastionLength);
		}
	}
}
