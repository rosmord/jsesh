package jsesh.drawingspecifications;

public record GroupsLayoutSpecifications(
		float maxCadratHeight,
		float maxCadratWidth,
		float smallSkip,
		float standardSignHeight, // Should it be there ?
		float lineSkip,
		float columnSkip,
		float tabUnitWidth) {

	public Builder copy() {
		return new Builder(this);
	}

	public static class Builder {
		private float maxCadratHeight;
		private float maxCadratWidth;
		private float smallSkip;
		private float standardSignHeight;
		private float lineSkip;
		private float columnSkip;
		private float tabUnitWidth;

		public Builder(GroupsLayoutSpecifications spec) {
			this.maxCadratHeight = spec.maxCadratHeight;
			this.maxCadratWidth = spec.maxCadratWidth;
			this.smallSkip = spec.smallSkip;
			this.standardSignHeight = spec.standardSignHeight;
			this.lineSkip = spec.lineSkip;
			this.columnSkip = spec.columnSkip;
			this.tabUnitWidth = spec.tabUnitWidth;
		}

		public Builder maxCadratHeight(float maxCadratHeight) {
			this.maxCadratHeight = maxCadratHeight;
			return this;
		}

		public Builder maxCadratWidth(float maxCadratWidth) {
			this.maxCadratWidth = maxCadratWidth;
			return this;
		}

		public Builder smallSkip(float smallSkip) {
			this.smallSkip = smallSkip;
			return this;
		}

		public Builder standardSignHeight(float standardSignHeight) {
			this.standardSignHeight = standardSignHeight;
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

		public GroupsLayoutSpecifications build() {
			return new GroupsLayoutSpecifications(maxCadratHeight, maxCadratWidth, smallSkip, standardSignHeight,
					lineSkip, columnSkip, tabUnitWidth);
		}
	}
}