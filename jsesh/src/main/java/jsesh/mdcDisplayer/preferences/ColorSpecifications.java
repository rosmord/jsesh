package jsesh.mdcDisplayer.preferences;

import java.awt.Color;
import java.util.Map;

public record ColorSpecifications(
			Color blackColor,
			Color redColor,
			Color cursorColor,
			Color grayColor,
			Color backgroundColor,
			/**
			 * An unmodifiable map (should really be immutable).
			 */
			Map<String, Color> colorMap
		) {
}
