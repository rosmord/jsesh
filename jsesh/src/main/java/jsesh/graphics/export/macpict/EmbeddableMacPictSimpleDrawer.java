package jsesh.graphics.export.macpict;

import jsesh.graphics.export.generic.AbstractRTFEmbeddableDrawer;
import jsesh.mdcDisplayer.mdcView.ViewBuilder;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import org.qenherkhopeshef.graphics.pict.MacPictGraphics2D;
import org.qenherkhopeshef.graphics.rtfBasicWriter.SimpleRTFWriter;

import java.awt.*;
import java.io.IOException;

public class EmbeddableMacPictSimpleDrawer extends AbstractRTFEmbeddableDrawer {

	private MacPictGraphics2D macPictGraphics2D;

	/**
	 * @param viewBuilder
	 * @param cadratHeight
	 *
	 */
	public EmbeddableMacPictSimpleDrawer(ViewBuilder viewBuilder, DrawingSpecification drawingSpecification,
											double cadratHeight) {
		super(viewBuilder, drawingSpecification, cadratHeight);
		setShadeAfter(false);
	}

	@Override
	protected Graphics2D buildGraphics() {

		macPictGraphics2D = new MacPictGraphics2D(0, 0,
				getScaledWidth() + 1, getScaledHeight() + 1);

		return macPictGraphics2D;
	}

	@Override
	public byte[] getAsArrayForRTF() {
		return macPictGraphics2D.getAsArrayForRTF();
	}

	@Override
	public void writeToRTF(SimpleRTFWriter rtfWriter) throws IOException {
		rtfWriter.writeMacPictPicture(getAsArrayForRTF(), getScaledWidth(),
				getScaledHeight());
	}
}
