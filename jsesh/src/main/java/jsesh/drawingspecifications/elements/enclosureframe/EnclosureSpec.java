package jsesh.drawingspecifications.elements.enclosureframe;

/**
 * Square Fortified town enclosure specifications.
 */
public record EnclosureSpec(
		float bastionDepth,
		float bastionLength) {

	public static final EnclosureSpec DEFAULT =	 new EnclosureSpec(3, 4);
	

	/** Create a copy. */
	public Builder copy() {
		return new Builder(this);
	}

	/** copy builder class */
	public static class Builder {
		private float bastionDepth;
		private float bastionLength;

		
		public Builder(EnclosureSpec specs) {
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

		public EnclosureSpec build() {
			return new EnclosureSpec(bastionDepth, bastionLength);
		}
	}
}
