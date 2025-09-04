package jsesh.drawingspecifications;

import jsesh.drawingspecifications.elementspecification.ElementDrawingSpecifications;

/**
 * Specifications for rendering a text.
 * New system for drawing specifications.
 * Will change name when the old one is removed !!
 */
public record RenderingSpecifications(
		boolean smallSignsCentered,		
		PageSpecifications pageSpecifications,
		GroupsLayoutSpecifications groupsLayoutSpecifications,
		ElementDrawingSpecifications elementDrawingSpecifications,
		StrokeSpecifications strokeSpecifications
		) {


		public Builder copy() {
			return new Builder(this);
		}

		/**
		 * Copy builder class.
		 */
		public static class Builder {
			private boolean smallSignsCentered;		
			private PageSpecifications pageSpecifications;
			private GroupsLayoutSpecifications groupsLayoutSpecifications;
			private ElementDrawingSpecifications elementDrawingSpecifications;
			private StrokeSpecifications strokeSpecifications;

			public Builder(RenderingSpecifications specs) {
				this.smallSignsCentered = specs.smallSignsCentered;
				this.pageSpecifications = specs.pageSpecifications;
				this.groupsLayoutSpecifications = specs.groupsLayoutSpecifications;
				this.elementDrawingSpecifications = specs.elementDrawingSpecifications;
				this.strokeSpecifications = specs.strokeSpecifications;
			}

			public Builder smallSignsCentered(boolean smallSignsCentered) {
				this.smallSignsCentered = smallSignsCentered;
				return this;
			}

			public Builder pageSpecifications(PageSpecifications pageSpecifications) {
				this.pageSpecifications = pageSpecifications;
				return this;
			}

			public Builder groupsLayoutSpecifications(GroupsLayoutSpecifications groupsLayoutSpecifications) {
				this.groupsLayoutSpecifications = groupsLayoutSpecifications;
				return this;
			}

			public Builder elementDrawingSpecifications(ElementDrawingSpecifications elementDrawingSpecifications) {
				this.elementDrawingSpecifications = elementDrawingSpecifications;
				return this;
			}

			public Builder strokeSpecifications(StrokeSpecifications strokeSpecifications) {
				this.strokeSpecifications = strokeSpecifications;
				return this;
			}

			public RenderingSpecifications build() {
				return new RenderingSpecifications(smallSignsCentered, pageSpecifications, groupsLayoutSpecifications, elementDrawingSpecifications, strokeSpecifications);
			}
		}
	
}
