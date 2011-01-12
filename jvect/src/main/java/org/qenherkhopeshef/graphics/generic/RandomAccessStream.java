package org.qenherkhopeshef.graphics.generic;

/**
 * An adapter interface to unify random access files and ByteArrays.
 * @author Serge Rosmorduc
 *
 */
public interface RandomAccessStream {

	void seek(int i);
	
	int tell() ;
	
	void close();

	void write(int s0);

	void setLength(int i);

}
