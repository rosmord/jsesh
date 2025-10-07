package jsesh.drawingspecifications;


/**
 * A record holding all information about Layout and drawing.
 * Specifications for rendering a text.
 * New system for drawing specifications.
 * Will change name when the old one is removed !!
 * 
 * @param elementsSpec specifications related to the various elements (pages, groups, etc.)
 * @param graphicalSpecs other specifications, for instance about colors and strokes width. 
 * @param graphicDeviceScale  Returns the scale of the graphic device, in graphic units per typographical
 *		 point. This is the scale used by the device if g.getXScale() returns 1.0, not
 *		 the current scale. Note that we could be lying. In the case of a screen zoom,
 *		 for instance, we will still provide the original scale.
 * @author rosmord
 */
public record RenderingParameters(
	ElementsSpec elementsSpec,
	GraphicalSpecs	graphicalSpecs,
	double graphicDeviceScale
		) {

	public Builder copy() {
		return new Builder(this);
	}

	public static class Builder {
		private ElementsSpec elementsSpec;
		private GraphicalSpecs graphicalSpecs;
		private double graphicDeviceScale;

		public Builder(RenderingParameters specs) {
			this.elementsSpec = specs.elementsSpec;
			this.graphicalSpecs = specs.graphicalSpecs;
			this.graphicDeviceScale = specs.graphicDeviceScale;
		}

		public Builder elementsSpec(ElementsSpec elementsSpec) {
			this.elementsSpec = elementsSpec;
			return this;
		}

		public Builder graphicalSpecs(GraphicalSpecs graphicalSpecs) {
			this.graphicalSpecs = graphicalSpecs;
			return this;
		}

		public Builder graphicDeviceScale(double graphicDeviceScale) {
			this.graphicDeviceScale = graphicDeviceScale;
			return this;
		}

		public RenderingParameters build() {
			return new RenderingParameters(elementsSpec, graphicalSpecs, graphicDeviceScale);
		}
	}

}