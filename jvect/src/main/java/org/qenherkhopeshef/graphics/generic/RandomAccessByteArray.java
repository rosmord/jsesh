package org.qenherkhopeshef.graphics.generic;


/**
 * A in-memory version of RandomAccessStream.
 * @author rosmord
 */

public class RandomAccessByteArray implements RandomAccessStream {
	//private static final int CHUNK_SIZE= 2048;
	private static final int CHUNK_SIZE= 1 << 20; // 1 mo.
	
	private int size= 0;
	private int pos= 0;
	private byte data[]= new byte[CHUNK_SIZE];
	
	/**
	 * Create an empty RandomAccessByteArray.
	 */
	public RandomAccessByteArray() {
	}

	/**
	 * Create a RandomAccessByteArray initialized with the content of a plain array.
	 * @param array the array whose content will be copied in the RandomAccessByteArray.
	 */
	public RandomAccessByteArray(byte[] array) {
		size= array.length;
		data= new byte[((array.length / CHUNK_SIZE) + 1) * CHUNK_SIZE];
		System.arraycopy(array, 0, data, 0, size);
	}

	public void seek(int i) {
		ensureExist(i);
		pos= i;
		if (pos > size)
			size= pos;
	}

	public int tell() {
		return pos;
	}
	
	private void ensureExist(int i) {
		if (i >= data.length) {
			byte [] newArray;
			int newSize= data.length;
			while (i>= newSize)
				newSize= newSize << 1; // *2 gave far worse results. Woe to the optimizer !
			newArray= new byte[newSize];
			System.arraycopy(data,0,newArray,0,data.length);
			data= newArray;
		}
	}


	public void close() {
	}
	
	public void write(int s0) {
		ensureExist(pos);
		data[pos]= (byte) s0;
		pos++;
		if (pos > size)
			size= pos;
	}
	
	public byte[] getByteArray() {
		return getByteArray(0);
	}

	public byte[] getByteArray(int first) {
		byte[] result= new byte[size-first];
		System.arraycopy(data,first,result,0,size-first);
		return result;
	}

	
	public void setLength(int i) {
		size= i;
		if (pos > size)
			pos= size;
	}
	
	public static void main(String[] args) {
		RandomAccessByteArray test= new RandomAccessByteArray();
		test.write(0);
		test.write(10);
		test.write(255);
		test.seek(500);
		test.write(128);
		test.seek(0);
		test.write(9);
		byte[] b=test.getByteArray();
		for (int i=0; i < b.length; i++)
			System.out.println(b[i]);
	}
}
