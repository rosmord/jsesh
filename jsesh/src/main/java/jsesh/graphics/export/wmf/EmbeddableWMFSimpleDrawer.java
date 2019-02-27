package jsesh.graphics.export.wmf;

import jsesh.graphics.export.generic.AbstractRTFEmbeddableDrawer;
import jsesh.mdcDisplayer.mdcView.ViewBuilder;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import org.qenherkhopeshef.graphics.generic.RandomAccessByteArray;
import org.qenherkhopeshef.graphics.rtfBasicWriter.SimpleRTFWriter;
import org.qenherkhopeshef.graphics.wmf.WMFGraphics2D;

import java.awt.*;
import java.io.IOException;

public class EmbeddableWMFSimpleDrawer extends AbstractRTFEmbeddableDrawer {

	private WMFGraphics2D wmfGraphics2D;
	private RandomAccessByteArray out;
	private double deviceScale = 1.0;

	public EmbeddableWMFSimpleDrawer(ViewBuilder viewBuilder, DrawingSpecification drawingSpecification, double cadratHeight) {
		super(viewBuilder, drawingSpecification, cadratHeight);
		setShadeAfter(false);
	}

	@Override
	protected Graphics2D buildGraphics() {
		out = new RandomAccessByteArray();
		wmfGraphics2D = null;
		try {
			wmfGraphics2D = new WMFGraphics2D(out,
					(int) getScaledWidth() + 1, (int) getScaledHeight() + 1);
			wmfGraphics2D.setPrecision(1);
			deviceScale = wmfGraphics2D.getTransform().getScaleX();
			getDrawingSpecifications().setGraphicDeviceScale(deviceScale);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return wmfGraphics2D;
	}

	@Override
	public byte[] getAsArrayForRTF() {
		// Skip the placeable header.
		return out.getByteArray(22);
	}

	@Override
	public void writeToRTF(SimpleRTFWriter rtfWriter) throws IOException {
		rtfWriter.writeWmfPicture(getAsArrayForRTF(), getScaledWidth(),
				getScaledHeight());

	}

}
