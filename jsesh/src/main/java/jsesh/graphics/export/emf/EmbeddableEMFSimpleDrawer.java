package jsesh.graphics.export.emf;

import java.awt.Graphics2D;
import java.io.IOException;

import org.qenherkhopeshef.graphics.emf.EMFGraphics2D;
import org.qenherkhopeshef.graphics.generic.RandomAccessByteArray;
import org.qenherkhopeshef.graphics.rtfBasicWriter.SimpleRTFWriter;

import jsesh.graphics.export.generic.AbstractRTFEmbeddableDrawer;
import jsesh.mdcDisplayer.context.JSeshRenderContext;

/**
 * A drawer which can be used to generate EMF pictures for embedding.
 */
public class EmbeddableEMFSimpleDrawer extends AbstractRTFEmbeddableDrawer {

	private EMFGraphics2D emGraphics2D;
	private RandomAccessByteArray out;
	private final String comment;

	/**
	 * Creates the drawer.
	 * @param viewBuilder the view builder to use
	 * @param renderContext the JSeshRenderContext
	 * @param cadratHeight the desired quadrat height
	 * @param comment a comment to include in the files.
	 */
	public EmbeddableEMFSimpleDrawer(JSeshRenderContext renderContext, double cadratHeight,
									 String comment) {
		super(renderContext, cadratHeight);
		setShadeAfter(false);
		this.comment = comment;
	}

	@Override
	protected Graphics2D buildGraphics() {
		out = new RandomAccessByteArray();
		emGraphics2D = null;
		try {
			// TODO : change the "JSesh" name here into a field...
			emGraphics2D = new EMFGraphics2D(out,
					(int) getScaledWidth() + 1,
					(int) getScaledHeight() + 1, "JSesh", comment);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return emGraphics2D;
	}

	public byte[] getBytes() {
		return out.getByteArray();
	}
	
	@Override
	public byte[] getAsArrayForRTF() {
		return out.getByteArray();
	}

	@Override
	public void writeToRTF(SimpleRTFWriter rtfWriter) throws IOException {
		rtfWriter.writeEmfPicture(getAsArrayForRTF(), getScaledWidth(),
				getScaledHeight());
	}
}
