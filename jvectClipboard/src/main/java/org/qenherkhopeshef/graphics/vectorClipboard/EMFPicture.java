package org.qenherkhopeshef.graphics.vectorClipboard;

import java.awt.Graphics2D;
import java.awt.datatransfer.Transferable;
import java.io.IOException;

import org.qenherkhopeshef.graphics.emf.EMFGraphics2D;
import org.qenherkhopeshef.graphics.generic.RandomAccessByteArray;
import org.qenherkhopeshef.graphics.rtfBasicWriter.SimpleRTFWriter;

public class EMFPicture extends RTFPicture {

	int width, height;
	EMFGraphics2D emfGraphics2D;
	private RandomAccessByteArray out;

	public EMFPicture(int width, int height) throws IOException {
		this.width = width;
		this.height = height;
		out = new RandomAccessByteArray();
		emfGraphics2D = new EMFGraphics2D(out, width, height, null, null);
	}

	public Graphics2D getGraphics() {
		return emfGraphics2D;
	}

	public void write(SimpleRTFWriter writer) throws IOException {
		writer.writeEmfPicture(out.getByteArray(), width, height);
	}

	public byte[] getAsByteArray() {
		return out.getByteArray();
	}

	protected Transferable buildStandAloneTransferable() {
		return new EMFTransferable(this);
	}
}
