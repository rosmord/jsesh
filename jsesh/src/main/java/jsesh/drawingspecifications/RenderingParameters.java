package jsesh.drawingspecifications;

import jsesh.drawingspecifications.elements.FramesSpec;
import jsesh.drawingspecifications.elements.GroupsSpec;
import jsesh.drawingspecifications.elements.PageSpec;
import jsesh.drawingspecifications.elements.TextLayoutSpec;
import jsesh.drawingspecifications.graphical.StrokeSpec;

/**
 * A record holding all information about Layout and drawing.
 * Specifications for rendering a text.
 * New system for drawing specifications.
 * Will change name when the old one is removed !!
 * 
 * @param elementsSpec specifications related to the various elements (pages, groups, etc.)
 * @param graphicalSpecs other specifications, for instance about colors and strokes width. 
 * @author rosmord
 */
public record RenderingParameters(
	ElementsSpec elementsSpec,
	GraphicalSpecs	graphicalSpecs
		) {

	public Builder copy() {
		return new Builder(this);
	}

	public static class Builder {
		private ElementsSpec elementsSpec;
		private GraphicalSpecs graphicalSpecs;

		public Builder(RenderingParameters specs) {
			this.elementsSpec = specs.elementsSpec;
			this.graphicalSpecs = specs.graphicalSpecs;
		}

		public Builder elementsSpec(ElementsSpec elementsSpec) {
			this.elementsSpec = elementsSpec;
			return this;
		}

		public Builder graphicalSpecs(GraphicalSpecs graphicalSpecs) {
			this.graphicalSpecs = graphicalSpecs;
			return this;
		}

		public RenderingParameters build() {
			return new RenderingParameters(elementsSpec, graphicalSpecs);
		}
	}

}