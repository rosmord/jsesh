package org.qenherkhopeshef.graphics.vectorClipboard;

import java.awt.Graphics2D;
import java.awt.datatransfer.Transferable;

/**
 * A type of pictures which can be transfered to a clipboard.
 * @author rosmord
 *
 */
public interface TransferablePicture {
	/**
	 * Returns a Graphics2D object to draw on.
	 * The scale used is 1 unit = 1 typographical point.
	 * @return
	 */
	Graphics2D getGraphics();
	
	/**
	 * Gets a transferable to transfer the picture.
	 * <p> Must be called <b> after</b> drawing the picture and releasing the graphics2D.
	 * @return
	 */
	Transferable buildTransferable();
}
