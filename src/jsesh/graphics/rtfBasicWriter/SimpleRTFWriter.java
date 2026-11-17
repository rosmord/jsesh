/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.graphics.rtfBasicWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;

/**
 * A class to create simple RTF files.
 * This is far less ambitious than IText. However, we are writing it because 
 * we want a firm handling of WMF file inclusion (with which we have had problems in IText).
 */
public class SimpleRTFWriter {
	
	private HashMap fonts= new HashMap();
	private BufferedWriter writer;
	private int maxFontNum= -1;
	
	
	public SimpleRTFWriter(OutputStream outputStream) {
		try {
			this.writer= new BufferedWriter(new OutputStreamWriter(outputStream, "US-ASCII"));
			//this.writer= new OutputStreamWriter(outputStream, "MacRoman");
		}
		catch (UnsupportedEncodingException e) {
			// Normally impossible. ASCII is universally supported by java.
			throw new RuntimeException(e);
		}
	}
	
	
	public void writeMacPictPicture(byte[] data, int width, int height) throws IOException {
		//writer.write("{\\*\\shppict{\\pict"); // We can even use old version of word.
		writer.write("{{\\pict");
		writer.write("\\macpict");
		writer.write("\\picw"+ width);
		writer.write("\\pich"+ height);

		writer.newLine();
		for (int i=0; i < data.length; i++) {
			if (i % 64 == 0)
				writer.newLine();
			int v= (data[i] + 0x100) % 0x100; 
			//System.out.println(v+ " => " + Integer.toHexString(v));
			String hex= Integer.toHexString(v);
			if (hex.length() == 1)
				hex= "0"+hex;
			writer.write(hex);
		}
		writer.write("}}");		
		writer.newLine();
	}

	
	
	public void writeHeader() throws IOException {
		String header="{\\rtf1\\ansi";
		header= "{\\rtf1\\ansi\\uc0\\deff0\\stshfdbch0\\stshfloch0\\stshfhich0\\stshfbi0\\deflang1036\\deflangfe1036{\\upr{\\fonttbl{\\f0\\fnil\\fcharset256\\fprq2 Times New Roman;}}}\\pard\\plain";
		header= "{\\rtf1\\ansi\\ansicpg1252\\deff0{\\fonttbl{\\f0\\froman\\fcharset0 Times New Roman;}}";
		writer.write(header);
		// Non Unicode reader will ignore Unicode chars.
		//writer.write("\\uc0");
	}
	
	public void writeTail() throws IOException {
		writer.write("}");
		//writer.write("}"); // ???? word seems to write an extraneous "}". So do we.
	
		//writer.write(0); // For mac. Does it work elsewhere ?
		writer.close();
	}
	
	/**
	 * Sets the text style.
	 * the code for style might be:
	 * <ul>
	 * <li> p : plain text
	 * <li> i : italic text
	 * <li> b : bold text
	 * </ul>
	 * @param style
	 * @throws IOException
	 */
	public void startStyle(char style ) throws IOException {
		String code= "plain";
		switch (style) {
		case 'i':
			code= "i";
			break;
		case 'b':
			code= "b";
		}
		writer.write("{\\" +code+ " ");
	}
	
	public void writeString(String text) throws IOException {
		for (int i=0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c < 128) {
				writer.write(c);
			} else {
				writer.write("\\u");
				writer.write(Integer.toString(c));
			}
		}
	}
	
	public void newParagraph() throws IOException {
		writer.write("\\par ");
	}
	public void endBlock() throws IOException {
		writer.write("}");
	}
	
	public void declareFont(String fontName, RTFFontFamily fontFamily) {
		fonts.put(fontName, new RTFFontData(++maxFontNum, fontName, fontFamily));
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void newPage() throws IOException {
		writer.write("");
	}
	
	private static class RTFFontData {
		int fontNum; 
		String fontName;
		RTFFontFamily fontFamily;
		public RTFFontData(int fontNum, String fontName, RTFFontFamily fontFamily) {
			super();
			this.fontNum = fontNum;
			this.fontName = fontName;
			this.fontFamily = fontFamily;
		} 
		
		
	}

	public void writeEmfPicture(byte[] data, int width, int height) throws IOException {
//		writer.write("{\\*\\shppict{\\pict"); // We can even use old version of word.
		writer.write("{{\\pict");
		writer.write("\\emfblip");
		
		writer.write("\\picw"+ width);
		writer.write("\\pich"+ height);
	
		writer.write("\\picscalex100");
		writer.write("\\picscaley100");
	
		for (int i=0; i < data.length; i++) {
			if (i % 20 == 0)
				writer.newLine();
			int v= (data[i] + 0x100) % 0x100; 
			//System.out.println(v+ " => " + Integer.toHexString(v));
			String hex= Integer.toHexString(v);
			if (hex.length() == 1)
				hex= "0"+hex;
			writer.write(hex);
		}
		
//		FileOutputStream out= new FileOutputStream("/tmp/truc.emf");
//		out.write(data);
//		out.close();
		
		writer.write("}}");		
	}


	public void writeWmfPicture(byte[] data, int width, int height) throws IOException {
		writer.write("{\\*\\shppict{\\pict");
		writer.write("\\wmetafile8");
		
		writer.write("\\picw"+ width);
		writer.write("\\pich"+ height);
	
		writer.write("\\picscalex100");
		writer.write("\\picscaley100");
	
		for (int i=0; i < data.length; i++) {
			if (i % 64 == 0)
				writer.newLine();
			int v= (data[i] + 0x100) % 0x100; 
			System.out.println(v+ " => " + Integer.toHexString(v));
			String hex= Integer.toHexString(v);
			if (hex.length() == 1)
				hex= "0"+hex;
			writer.write(hex);
		}
		
		
		writer.write("}}");
		
	}
}
