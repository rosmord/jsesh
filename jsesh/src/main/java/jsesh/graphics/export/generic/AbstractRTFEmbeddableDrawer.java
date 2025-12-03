package jsesh.graphics.export.generic;

import jsesh.mdcDisplayer.context.JSeshRenderContext;
import org.qenherkhopeshef.graphics.rtfBasicWriter.SimpleRTFWriter;

import java.io.IOException;

/**
 * A drawer able to create graphics which can be embedded in RTF.
 * Embedded graphics have little or no margin.
 */
public abstract class AbstractRTFEmbeddableDrawer extends
		AbtractExportDrawer {

	protected AbstractRTFEmbeddableDrawer(
										  JSeshRenderContext renderContext, double cadratHeight) {
		super(prepareDrawingSpecifications(renderContext), cadratHeight);
	}

	private static final JSeshRenderContext prepareDrawingSpecifications(JSeshRenderContext context) {
		return EmbeddableDrawingSpecificationHelper.createEmbeddedDrawingSpecifications(context);
	}

	public abstract void writeToRTF(SimpleRTFWriter rtfWriter)
			throws IOException;

	abstract public byte[] getAsArrayForRTF();

}
