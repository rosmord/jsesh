package org.qenherkhopeshef.graphics.vectorClipboard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.qenherkhopeshef.graphics.rtfBasicWriter.SimpleRTFWriter;

/**
 * Various useful methods for manipulating pictures embedded in RTF files
 * 
 * @author rosmord
 * 
 */
public class RTFPictureUtils {

	/**
	 * Create a byteArrayInput stream containing a RTF containing a picture.
	 * 
	 * @param picture
	 * @return
	 * @throws IOException
	 */
	public static ByteArrayInputStream createRTFByteArrayInputStream(
			RTFPicture picture) throws IOException {
		return new ByteArrayInputStream(createRTFByteArray(picture));
	}

	/**
	 * Create a byte array containing a RTF file with a picture embedded in it.
	 * @param picture
	 * @return a byte array.
	 * @throws IOException
	 */
	public static byte[] createRTFByteArray(RTFPicture picture)
			throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		writeAsRTF(picture, outputStream);
		return outputStream.toByteArray();
	}

	/**
	 * Write a picture embedded as RTF on a outputStream.
	 * @param picture
	 * @param outputStream
	 * @throws IOException
	 */
	public static void writeAsRTF(RTFPicture picture,
			OutputStream outputStream) throws IOException {
		SimpleRTFWriter rtfWriter = new SimpleRTFWriter(outputStream);
		rtfWriter.writeHeader();
		picture.write(rtfWriter);
		rtfWriter.writeTail();
	}

}
