package jsesh.mdcDisplayer.preferences;

import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;

public record LayoutSpecifications(
		boolean isPaged,
		boolean justified, // will be removed
		TextDirection textDirection,
		TextOrientation textOrientation
		) {

}
