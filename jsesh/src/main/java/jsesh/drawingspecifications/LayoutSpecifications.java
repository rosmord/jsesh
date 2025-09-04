package jsesh.drawingspecifications;

import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;

public record LayoutSpecifications(
		boolean isPaged,
		boolean justified, // will be removed
		TextDirection textDirection,
		TextOrientation textOrientation
		) {


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
		public Builder(LayoutSpecifications specs) {
			this.isPaged = specs.isPaged;
			this.justified = specs.justified;
			this.textDirection = specs.textDirection;
			this.textOrientation = specs.textOrientation;
		}
		public Builder paged(boolean isPaged) {
			this.isPaged = isPaged;
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
		public LayoutSpecifications build() {
			return new LayoutSpecifications(isPaged, justified, textDirection, textOrientation);
		}
	}

}
