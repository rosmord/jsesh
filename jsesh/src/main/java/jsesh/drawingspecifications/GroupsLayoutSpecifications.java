package jsesh.drawingspecifications;

/**
 * Specifications for the layout of hieroglyphic groups.
 * 
 * @author rosmord
 */

public record GroupsLayoutSpecifications(
		float maxCadratHeight,
		float maxCadratWidth,
		float smallSkip,
		float standardSignHeight) {

	public Builder copy() {
		return new Builder(this);
	}

	public static class Builder {
		private float maxCadratHeight;
		private float maxCadratWidth;
		private float smallSkip;
		private float standardSignHeight;

		public Builder(GroupsLayoutSpecifications spec) {
			this.maxCadratHeight = spec.maxCadratHeight;
			this.maxCadratWidth = spec.maxCadratWidth;
			this.smallSkip = spec.smallSkip;
			this.standardSignHeight = spec.standardSignHeight;			
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

		
		public GroupsLayoutSpecifications build() {
			return new GroupsLayoutSpecifications(maxCadratHeight, maxCadratWidth, smallSkip, standardSignHeight);
		}
	}
}