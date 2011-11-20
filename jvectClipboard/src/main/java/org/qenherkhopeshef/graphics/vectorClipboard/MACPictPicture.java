package org.qenherkhopeshef.graphics.vectorClipboard;

import java.awt.Graphics2D;
import java.awt.datatransfer.Transferable;
import java.io.IOException;

import org.qenherkhopeshef.graphics.pict.MacPictGraphics2D;
import org.qenherkhopeshef.graphics.rtfBasicWriter.SimpleRTFWriter;

public class MACPictPicture extends RTFPicture {

	private int width, height;
	private MacPictGraphics2D g2d;
	
	

	/**
	 * Create a picture of the given size.
	 * @param width width in points
	 * @param height height in points
	 * @throws IOException
	 */
	public MACPictPicture(int width, int height) throws IOException {
		this.width= width;
		this.height= height;
		g2d= new MacPictGraphics2D(0,0, width, height);
	}

	public Graphics2D getGraphics() {
		return g2d;
	}

	public void write(SimpleRTFWriter writer) throws IOException {
		writer.writeMacPictPicture(g2d.getAsArrayForRTF(), width, height);
	}
	
	public byte[] getAsByteArray() {
		return g2d.getAsArray();
	}
	
	public byte[] getAsArrayForRTF() {
		return g2d.getAsArrayForRTF();
	}

	protected Transferable buildStandAloneTransferable() {
		return new PICTTransferable(this);
	}
}
