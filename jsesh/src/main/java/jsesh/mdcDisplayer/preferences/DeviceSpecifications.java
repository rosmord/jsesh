package jsesh.mdcDisplayer.preferences;

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

}
