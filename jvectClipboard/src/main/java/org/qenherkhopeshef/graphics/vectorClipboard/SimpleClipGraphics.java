package org.qenherkhopeshef.graphics.vectorClipboard;

import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.io.IOException;

/**
 * A very simple class to perform vector cut and paste.
 * 
 * <p>
 * It provides a variety of format. In some of those, the data is formated as
 * RTF, which can be pasted into most word processors.
 * <p>
 * You can select the vector format embedded in the picture. The default value
 * is {@link PictureFormat#MACPICT} which works more or less everywhere.
 * <p>
 * For better quality, you might choose {@link PictureFormat#EMF}. However, it
 * doesn't render correctly in Word on Mac OS X. It renders well in all versions
 * of Openoffice, though.
 * <p>
 * Note that in some cases you will need something a bit more sophisticated,
 * especially if you want to propose more than one format.
 * 
 * @author Serge Rosmorduc
 */

public class SimpleClipGraphics {

	// Technical note :
	// We are trying to add "plain" emf picture which will probably only work on
	// windows.
	// We will also add PDF later.
	// Currently, the code structure supposes we work with RTF embedded files
	// To clean things up, some refactoring will be needed. Later.
	/**
	 * The current picture, writable to a RTF file.
	 */
	private TransferablePicture currentPicture;

	private int pictureFormat = PictureFormat.MACPICT;

	private int width, height;

	/**
	 * Prepare a vector graphic.
	 * 
	 * @param width
	 * @param height
	 */
	public SimpleClipGraphics(int width, int height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * Gets the graphics 2D environment where you must write.
	 * 
	 * Returns a Graphics2D object. The scale used is 1 unit = 1 typographical
	 * point.
	 * 
	 * @return
	 */

	public Graphics2D getGraphics() {
		if (currentPicture == null)
			initPicture();

		return currentPicture.getGraphics();
	}

	private void initPicture() {
		try {
			switch (pictureFormat) {
			case PictureFormat.EMF:
				currentPicture = new EMFPicture(width, height);
				break;
			case PictureFormat.WMF:
				currentPicture = new WMFPicture(width, height);
				break;
			case PictureFormat.MACPICT:
				currentPicture = new MACPictPicture(width, height);
				break;
			case PictureFormat.DIRECT_EMF: {
				EMFPicture emfPicture = new EMFPicture(width, height);
				emfPicture.setEmbeddedInRTF(false);
				currentPicture = emfPicture;
			}
				break;
			case PictureFormat.DIRECT_PICT: {
				MACPictPicture macPictPicture = new MACPictPicture(width,
						height);
				macPictPicture.setEmbeddedInRTF(false);
				currentPicture = macPictPicture;
			}
				break;
			case PictureFormat.PDF:
				currentPicture = new PDFPicture(width, height);
				break;
			default:
				throw new java.lang.IllegalArgumentException("This format is not supported yet "+ pictureFormat);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the vector format used for the picture (EMF, WMF...)
	 * 
	 * @see PictureFormat
	 * @return
	 */
	public int getPictureFormat() {
		return pictureFormat;
	}

	/**
	 * Sets the picture format used for the picture (EMF, WMF...)
	 * 
	 * @param pictureFormat
	 */
	public void setPictureFormat(int pictureFormat) {
		this.pictureFormat = pictureFormat;
	}

	/**
	 * performs a copy to the clipboard.
	 * 
	 * @param owner
	 *            the clipboard owner. Can be null.
	 */
	public void copyToClipboard(ClipboardOwner owner) {
		Transferable transferable = currentPicture.buildTransferable();
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
				transferable, owner);
	}

	/**
	 * performs a copy to the clipboard.
	 */
	public void copyToClipboard() {
		copyToClipboard(null);
	}
}
