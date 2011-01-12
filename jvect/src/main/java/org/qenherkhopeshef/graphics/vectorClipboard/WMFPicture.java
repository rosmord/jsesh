package org.qenherkhopeshef.graphics.vectorClipboard;

import java.awt.Graphics2D;
import java.awt.datatransfer.Transferable;
import java.io.IOException;

import org.qenherkhopeshef.graphics.generic.RandomAccessByteArray;
import org.qenherkhopeshef.graphics.rtfBasicWriter.SimpleRTFWriter;
import org.qenherkhopeshef.graphics.wmf.WMFGraphics2D;

public class WMFPicture extends RTFPicture {

	int width, height;
	WMFGraphics2D wmfGraphics2D;
	private RandomAccessByteArray out;
	
	
	public WMFPicture(int width, int height) throws IOException {
		this.width= width;
		this.height= height;
		out = new RandomAccessByteArray();
		wmfGraphics2D= new WMFGraphics2D(out, width, height);
	}

	public Graphics2D getGraphics() {
		return wmfGraphics2D;
	}

	public void write(SimpleRTFWriter writer) throws IOException {
		// Note : in the case of embedded WMF picture, we skip the 22 first bytes
		// of the WMF file (i.e. its header).
		writer.writeWmfPicture(out.getByteArray(22), width, height);
	}

	protected Transferable buildStandAloneTransferable() {
		throw new UnsupportedOperationException("WMF is not available as stand-alone vector format yet");
	}
}
