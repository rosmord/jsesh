package org.qenherkhopeshef.graphics.generic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


public class RandomAccessFileAdapter implements RandomAccessStream {

	private RandomAccessFile randomAccessFile;

    /**
     * Create a view of a RandomAccessFile as a RandomAccessStream.
     * @param randomAccessFile
     */
	public RandomAccessFileAdapter(RandomAccessFile randomAccessFile) {
		this.randomAccessFile= randomAccessFile;
	}

    /**
     * Open a new or existing file for reading and writing as a RandomAccessStream.
     * Note that in many cases you will need to delete the file first.
     * @param file
     * @throws java.io.FileNotFoundException
     */
    public RandomAccessFileAdapter(File file) throws FileNotFoundException {
        this(new RandomAccessFile(file, "rw"));
    }

	public void seek(int i) {
		try {
			randomAccessFile.seek(i);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int tell() {
		try {
		return (int) randomAccessFile.getFilePointer();
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public void close() {
		try {
			randomAccessFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void write(int s0) {
		try {
			randomAccessFile.write(s0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setLength(int i) {
		try {
			randomAccessFile.setLength(i);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}
