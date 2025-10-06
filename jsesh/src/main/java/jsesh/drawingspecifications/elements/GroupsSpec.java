package jsesh.drawingspecifications.elements;

/**
 * Specifications for the layout of hieroglyphic groups.
 * 
 * @param maxCadratHeight the maximum height of a cadrat in horizontal text
 * @param maxCadratWidth the maximum width of a cadrat in vertical text
 * @param smallSkip skip between elements in a group
 * @param standardSignHeight the expected height of A1.
 * 
 * @author rosmord
 */

public record GroupsSpec(
		float maxCadratHeight,
		float maxCadratWidth,
		float smallSkip,
		float standardSignHeight) {

	public static final GroupsSpec DEFAULT = 
		new GroupsSpec(
			18f,
			 22f, 
			2f, 
			18f);
			
	public Builder copy() {
		return new Builder(this);
	}

	public static class Builder {
		private float maxCadratHeight;
		private float maxCadratWidth;
		private float smallSkip;
		private float standardSignHeight;

		public Builder(GroupsSpec spec) {
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

		
		public GroupsSpec build() {
			return new GroupsSpec(maxCadratHeight, maxCadratWidth, smallSkip, standardSignHeight);
		}
	}
}