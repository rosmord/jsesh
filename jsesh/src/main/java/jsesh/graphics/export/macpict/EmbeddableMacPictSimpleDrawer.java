package jsesh.graphics.export.macpict;

import java.awt.Graphics2D;
import java.io.IOException;

import org.qenherkhopeshef.graphics.pict.MacPictGraphics2D;
import org.qenherkhopeshef.graphics.rtfBasicWriter.SimpleRTFWriter;

import jsesh.graphics.export.generic.AbstractRTFEmbeddableDrawer;
import jsesh.mdcDisplayer.context.JSeshRenderContext;

public class EmbeddableMacPictSimpleDrawer extends AbstractRTFEmbeddableDrawer {

	private MacPictGraphics2D macPictGraphics2D;

	/**
	 * @param viewBuilder
	 * @param cadratHeight
	 *
	 */
	public EmbeddableMacPictSimpleDrawer(JSeshRenderContext renderContext,
											double cadratHeight) {
		super(renderContext, cadratHeight);
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
