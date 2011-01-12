/**
 * 
 */
package org.qenherkhopeshef.graphics.generic;


/**
 * Utility class for RamdomAccessStreams.
 * @author rosmord
 *
 */
public class RandomAccessStreamUtils {

	public static void writeU8(RandomAccessStream out, int u8) {
		out.write((int) u8);
	}

	public static void writeS16(RandomAccessStream out, short x) {
		int s0, s1;
		s0 = (x & 0xFF);
		s1 = (x & 0xFF00) >>> 8;
		out.write(s0);
		out.write(s1);
	}

	public static void writeU16(RandomAccessStream out, int x) {
		int s0, s1;
		s0 = (x & 0xFF);
		s1 = (x & 0xFF00) >>> 8;
		out.write(s0);
		out.write(s1);
	}

	public static void writeS32(RandomAccessStream out, long x32) {
		int x = (int) x32;
		int s0, s1, s2, s3;
		s0 = (x & 0xFF);
		s1 = (x & 0xFF00) >>> 8;
		s2 = (x & 0xFF0000) >>> 16;
		s3 = (x & 0xFF000000) >>> 24;
	
		out.write(s0);
		out.write(s1);
		out.write(s2);
		out.write(s3);
	}

	/**
	 * Write an unsigned 32 bits value.
	 * @param out
	 * @param recordType
	 */
	public static void writeU32(RandomAccessStream out, long x32) {
		int s0, s1, s2, s3;
		s0 = (int)(x32 & 0xFF);
		s1 = (int)((x32 & 0xFF00) >>> 8);
		s2 = (int)((x32 & 0xFF0000) >>> 16);
		s3 = (int)((x32 & 0xFF000000l) >>> 24);
	
		out.write(s0);
		out.write(s1);
		out.write(s2);
		out.write(s3);	
	}

}
