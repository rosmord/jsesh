package jsesh.graphics.export.generic;

import jsesh.graphics.export.generic.AbtractExportDrawer;
import jsesh.mdcDisplayer.mdcView.ViewBuilder;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import org.qenherkhopeshef.graphics.rtfBasicWriter.SimpleRTFWriter;

import java.io.IOException;

/**
 * A drawer able to create graphics which can be embedded in RTF.
 * Embedded graphics have little or no margin.
 */
public abstract class AbstractRTFEmbeddableDrawer extends
		AbtractExportDrawer {

	protected AbstractRTFEmbeddableDrawer(ViewBuilder viewBuilder,
										  DrawingSpecification drawingSpecifications, double cadratHeight) {
		super(viewBuilder, prepareDrawingSpecifications(drawingSpecifications), cadratHeight);
	}

	private static DrawingSpecification prepareDrawingSpecifications(DrawingSpecification drawingSpecifications) {
		return EmbeddableDrawingSpecificationHelper.createEmbeddedDrawingSpecifications(drawingSpecifications);
	}

	public abstract void writeToRTF(SimpleRTFWriter rtfWriter)
			throws IOException;

	abstract public byte[] getAsArrayForRTF();

}
