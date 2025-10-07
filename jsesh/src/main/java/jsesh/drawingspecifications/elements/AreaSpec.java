package jsesh.drawingspecifications.elements;

import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;

/**
 * Global layout specifications.
 * 
 * @param paged           whether the text is to be laid out on pages.
 * @param justified       whether the text is to be justified (will be removed).
 * @param lineSkip        distance between two lines (in points).
 * @param columnSkip      distance between two columns (in points).
 * @param tabUnitWidth    width of the unit used in MdC tabulation (in points).
 * @param textDirection   text direction (left to right or right to left).
 * @param textOrientation text orientation (horizontal or vertical).
 * @author Serge Rosmorduc
 */
public record AreaSpec(
		boolean paged,
		boolean justified, // will be removed
		float lineSkip,
		float columnSkip,
		float tabUnitWidth,
		TextDirection textDirection,
		TextOrientation textOrientation) {

	public static final AreaSpec DEFAULT = new AreaSpec(false, false,
			6.0f,
			10.0f,
			(18f / 200f),
			TextDirection.LEFT_TO_RIGHT, TextOrientation.HORIZONTAL);

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
		private float lineSkip;
		private float columnSkip;
		private float tabUnitWidth;

		public Builder(AreaSpec spec) {
			this.isPaged = spec.paged;
			this.justified = spec.justified;
			this.textDirection = spec.textDirection;
			this.textOrientation = spec.textOrientation;
			this.lineSkip = spec.lineSkip;
			this.columnSkip = spec.columnSkip;
			this.tabUnitWidth = spec.tabUnitWidth;
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

		public Builder lineSkip(float lineSkip) {
			this.lineSkip = lineSkip;
			return this;
		}

		public Builder columnSkip(float columnSkip) {
			this.columnSkip = columnSkip;
			return this;
		}

		public Builder tabUnitWidth(float tabUnitWidth) {
			this.tabUnitWidth = tabUnitWidth;
			return this;
		}

		public AreaSpec build() {
			return new AreaSpec(isPaged, justified, lineSkip, columnSkip, tabUnitWidth,
					textDirection, textOrientation);
		}
	}

}
