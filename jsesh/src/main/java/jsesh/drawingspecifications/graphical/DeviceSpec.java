package jsesh.drawingspecifications.graphical;

public record DeviceSpec(
		/**
		 * Returns the scale of the graphic device, in graphic units per typographical
		 * point. This is the scale used by the device if g.getXScale() returns 1.0, not
		 * the current scale. Note that we could be lying. In the case of a screen zoom,
		 * for instance, we will still provide the original scale.
		 * 
		 * @return
		 */
		double graphicDeviceScale
		) {

		Builder copy() {
			return new Builder(this);
		}

		/** Copy Builder class */
		public static class Builder {
			private double graphicDeviceScale;
			public Builder(DeviceSpec specs) {
				this.graphicDeviceScale = specs.graphicDeviceScale;
			}
			public Builder graphicDeviceScale(double graphicDeviceScale) {
				this.graphicDeviceScale = graphicDeviceScale;
				return this;
			}

			public DeviceSpec build() {
				return new DeviceSpec(graphicDeviceScale);
			}
		}
}
