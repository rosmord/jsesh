package jsesh.drawingspecifications.elements;

import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;

/**
 * Global layout specifications.
 */
public record AreaSpec(
		boolean paged,
		boolean justified, // will be removed
		TextDirection textDirection,
		TextOrientation textOrientation) {

	public static final AreaSpec DEFAULT = new AreaSpec(false, false, TextDirection.LEFT_TO_RIGHT, TextOrientation.HORIZONTAL);

	public Builder copy() {
		return new Builder(this);
	}

	/**
	 * Copy Builder class.
	 */

	public static class Builder {
		private boolean isPaged;
		private boolean justified; // will be removed
		private TextDirection textDirection;
		private TextOrientation textOrientation;

		public Builder(AreaSpec specs) {
			this.isPaged = specs.paged;
			this.justified = specs.justified;
			this.textDirection = specs.textDirection;
			this.textOrientation = specs.textOrientation;
		}

		public Builder paged(boolean paged) {
			this.isPaged = paged;
			return this;
		}

		public Builder justified(boolean justified) {
			this.justified = justified;
			return this;
		}

		public Builder textDirection(TextDirection textDirection) {
			this.textDirection = textDirection;
			return this;
		}

		public Builder textOrientation(TextOrientation textOrientation) {
			this.textOrientation = textOrientation;
			return this;
		}

		public AreaSpec build() {
			return new AreaSpec(isPaged, justified, textDirection, textOrientation);
		}
	}

}
