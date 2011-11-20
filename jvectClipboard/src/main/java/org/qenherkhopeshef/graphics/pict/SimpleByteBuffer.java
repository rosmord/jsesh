/**
 * 
 */
package org.qenherkhopeshef.graphics.pict;

import java.io.IOException;
import java.io.OutputStream;

/**
 * In memory, growable, adressable, writable byte buffer. 
 * @author rosmord
 */
public class SimpleByteBuffer {
	
	private byte t[]= new byte[1024];
	private int size= 0;
	private int pos= 0;
	
	/**
	 * Gets sure one can write at position i.
	 * @param i
	 */
	private void ensureExists(int i) {
		while (i >= t.length)
		{
			byte newT[]= new byte[t.length*2];
			System.arraycopy(t, 0, newT, 0, size);
			t= newT;
		}
	}

	public void seek(int i) {
		pos= i;
	}
	
	/**
	 * return the cursor position.
	 * @return the cursor position.
	 */
	public int getPos() {
		return pos;
	}
	
	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}
	
	public void writeByte(int b) {
		ensureExists(pos);
		t[pos++]= (byte) b;
		if (size < pos)
			size= pos;
	}
	
	public void writeShortBigEndian(int a) {
		int lsb= a & 0xFF;
		int msb= (a & 0xFF00) >>> 8;
		writeByte(msb);
		writeByte(lsb);
	}
	
	public void writeIntBigEndian(int k) {
		int a= k & 0xFF;
		int b= (k & 0xFF00) >>> 8;
		int c= (k & 0xFF0000) >>> 16;
		int d= (k & 0xFF000000) >>> 24;
		
		writeByte(d);
		writeByte(c);
		writeByte(b);
		writeByte(a);
		//System.out.println("writing " +d +" "+ c +" "+ b + " "+ a + " for "+ (k & 0xFFFFFFFF));
	}
	
	public void writeShortLittleEndian(int a) {
		int lsb= a & 0xFF;
		int msb= (a & 0xFF00) >>> 8;
		writeByte(lsb);
		writeByte(msb);
	}
	
	/**
	 * Writes the buffer to an outputstream.
	 * As one might want to write more than one buffer to the same stream,
	 * the stream is not closed.
	 */
	public void writeToStream(OutputStream out) throws IOException {
		out.write(t, 0, getSize());
	}

	/**
	 * Return the buffer as a byte array.
	 * @return
	 */
	public byte[] getAsArray() {
		byte[] result = new byte[size];
		System.arraycopy(t, 0, result, 0, size);
		return result;
	}
	
	/**
	 * Return a sub-buffer.
	 * @param start
	 * @return
	 */
	public byte[] getAsArray(int start) {
		byte[] result = new byte[size-start];
		System.arraycopy(t, start, result, 0, size-start);
		return result;
	}
	
	/**
	 * Fill the buffer between pos (incl.) and pos+length with 'b' bytes.
	 * @param b
	 * @param pos
	 * @param length
	 */
	public void fill(byte b, int pos, int length) {
		ensureExists(pos+length-1);
		for (int i=pos; i< pos+ length; i++) {
			t[i]= b;
		}
		this.pos= pos+length;
		if (size < pos)
			size= pos;
	}
}
