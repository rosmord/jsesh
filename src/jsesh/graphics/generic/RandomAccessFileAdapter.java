package jsesh.graphics.generic;

import java.io.IOException;
import java.io.RandomAccessFile;


public class RandomAccessFileAdapter implements RandomAccessStream {

	private RandomAccessFile randomAccessFile;

	public RandomAccessFileAdapter(RandomAccessFile file) {
		randomAccessFile= file;
	}

	public void seek(int i) {
		try {
			randomAccessFile.seek(i);
		} catch (IOException e) {
			e.printStackTrace();
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
