package jsesh.drawingspecifications.elements.enclosureframe;

/**
 * Serekh specifications.
 */
public record SerekhSpec(
		float doorSize) {

	public static final SerekhSpec DEFAULT = new SerekhSpec(20);

	/** Create a copy. */
	public Builder copy() {
		return new Builder(this);
	}

	/** copy builder class */
	public static class Builder {
		private float doorSize;

		public Builder(SerekhSpec specs) {
			this.doorSize = specs.doorSize;
		}

		public Builder doorSize(float doorSize) {
			this.doorSize = doorSize;
			return this;
		}

		public SerekhSpec build() {
			return new SerekhSpec(doorSize);
		}
	}
}
