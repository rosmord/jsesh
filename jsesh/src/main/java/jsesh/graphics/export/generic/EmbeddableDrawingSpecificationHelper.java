package jsesh.graphics.export.generic;

import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.DrawingSpecificationsImplementation;
import jsesh.mdcDisplayer.preferences.PageLayout;

import java.awt.*;

/**
 * Helper class for creating drawing specifications for embedded pictures.
 */

public class EmbeddableDrawingSpecificationHelper {

	public static DrawingSpecification createEmbeddedDrawingSpecifications(DrawingSpecification originalSpecifications) {
		DrawingSpecification newDrawingSpecifications = originalSpecifications.copy();
        PageLayout pageLayout = newDrawingSpecifications.getPageLayout();
        pageLayout.setLeftMargin(01);
        pageLayout.setTopMargin(01);
        newDrawingSpecifications.setPageLayout(pageLayout);
        return newDrawingSpecifications;
	}
}
