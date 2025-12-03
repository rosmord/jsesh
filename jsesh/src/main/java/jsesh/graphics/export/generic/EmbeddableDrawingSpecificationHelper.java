package jsesh.graphics.export.generic;

import jsesh.mdcDisplayer.context.JSeshRenderContext;

/**
 * Helper class for creating drawing specifications for embedded pictures.
 */

public class EmbeddableDrawingSpecificationHelper {

    private EmbeddableDrawingSpecificationHelper() {
    }

    public static JSeshRenderContext createEmbeddedDrawingSpecifications(JSeshRenderContext originalRenderContext) {
        return originalRenderContext.copy()
                .jseshStyle(style -> style.geometry(g -> g
                        .leftMargin(01)
                        .topMargin(01)))
                .build();

    }
}
