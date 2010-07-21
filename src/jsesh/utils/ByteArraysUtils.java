package jsesh.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class ByteArraysUtils {

	public static byte[] readFileInByteArray(File file) throws FileNotFoundException,
			IOException {
		// We read the file in memory,
		// because we need to suppress non manuel de codage headers, if
		// any.
		InputStream inputStream = new BufferedInputStream(new FileInputStream(
				file));
		return readStreamInByteArray(inputStream);
	}

	/**
	 * Method for working in ASCII.
	 * 
	 * @param bytes
	 * @param firstByte
	 * @param string
	 * @return
	 */
	public static boolean startsWith(byte bytes[], int firstByte, String string) {
		byte[] values = new byte[string.length()];
		for (int i = 0; i < string.length(); i++) {
			values[i] = (byte) string.charAt(i);
		}
		return ByteArraysUtils.startsWith(bytes, firstByte, values);
	}

	public static boolean startsWith(byte[] bytes, int firstByte, byte[] values) {
		// test size.
		if (values.length + firstByte > bytes.length)
			return false;
		boolean result = true;
		int i = 0;
		while (result && i < values.length) {
			result = bytes[firstByte + i] == values[i];
			i++;
		}
		return result;
	}

	/**
	 * Uses C-like conventions.
	 * 
	 * @param bytes
	 * @param pos
	 * @return
	 */
	public static byte getByteAt(byte[] bytes, int pos) {
		if (pos < bytes.length) {
			return bytes[pos];
		} else
			return 0;
	}

	public static byte[] readStreamInByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		int c;
		while ((c = in.read()) != -1) {
			byteArrayOutputStream.write(c);
		}
		in.close();
		byte[] bytes = byteArrayOutputStream.toByteArray();
		return bytes;
	}

}
