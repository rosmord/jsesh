package jsesh.drawingspecifications.elementspecification;

public record SerekhSpecifications(
		float doorSize) {

	/** Create a copy. */
	public Builder copy() {
		return new Builder(this);
	}

	/** copy builder class */
	public static class Builder {
		private float doorSize;

		public Builder(SerekhSpecifications specs) {
			this.doorSize = specs.doorSize;
		}

		public Builder doorSize(float doorSize) {
			this.doorSize = doorSize;
			return this;
		}

		public SerekhSpecifications build() {
			return new SerekhSpecifications(doorSize);
		}
	}
}
