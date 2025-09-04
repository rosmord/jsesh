package jsesh.drawingspecifications;

import java.lang.module.ModuleDescriptor.Builder;

public record DeviceSpecifications(
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
			public Builder(DeviceSpecifications specs) {
				this.graphicDeviceScale = specs.graphicDeviceScale;
			}
			public Builder graphicDeviceScale(double graphicDeviceScale) {
				this.graphicDeviceScale = graphicDeviceScale;
				return this;
			}

			public DeviceSpecifications build() {
				return new DeviceSpecifications(graphicDeviceScale);
			}
		}
}
