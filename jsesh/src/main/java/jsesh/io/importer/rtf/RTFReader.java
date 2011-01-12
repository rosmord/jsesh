package jsesh.io.importer.rtf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Very basic Parser for an RTF file.
 * 
 * @author rosmord
 * 
 */
public class RTFReader {
	RTFLexer lexer;
	ArrayList mdcStrings = new ArrayList();

	public RTFReader(InputStream in) throws IOException {
		lexer = new RTFLexer(in);
		lexer.next();
		readGroup();
	}

	/**
	 * Reads the content of a group.
	 * 
	 * @throws IOException
	 */
	private void readGroup() throws IOException {
		boolean isPicture = false;
		StringBuffer pictureBuffer = new StringBuffer();
		// A group may be surounded either by the document start and EOF
		// or by '{' and '}'.
		if (lexer.getTokenType() == RTFTokenType.OPEN_GROUP) {
			lexer.next();
		}
		while (lexer.getTokenType() != RTFTokenType.EOF
				&& lexer.getTokenType() != RTFTokenType.CLOSE_GROUP) {
			switch (lexer.getTokenType()) {
			case RTFTokenType.OPEN_GROUP:
				readGroup();
				break;
			case RTFTokenType.COMMAND:
				// If the command corresponds to an EMF picture, ... isPicture=
				// true;
				if (lexer.getCommand().equals("\\emfblip"))
					isPicture = true;
				lexer.next();
				break;
			case RTFTokenType.NUMERIC_COMMAND:
//				if (lexer.getCommand().equals("\\wmetafile") && lexer.getNumericValue() == 8)
//					isPicture = true;
				lexer.next();
				break;
			default:
				// Ordinary data...
				if (isPicture && Character.isLetterOrDigit(lexer.getCode())) {
					pictureBuffer.append(lexer.getCode());
				}
				lexer.next();
				break;
			}
		}
		if (isPicture) {
			try {
				decodeBuffer(pictureBuffer);
			} catch (Exception e) {
				// Ignore any error... this might be a normal picture, not a
				// JSesh one after all.
				e.printStackTrace();
			}
		}
		lexer.next();
	}

	// We need to recognize "\", control words like \this and {}.
	// controls :
	// '\' (LETTER)+
	//  
	// the space just *after* a control is ignored.
	//
	// Control with numeric data : \(LETTER)+(-)?(0-9)+
	// a space just after a control with numeric data is ignored.
	// (it appears that letters are possible too ?)
	// control symbol : \(^LETTER)

	/**
	 * Decode an EMF image buffer content. If needed, ignore.
	 */
	private void decodeBuffer(StringBuffer pictureBuffer) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		// Decode the binary picture...
		for (int i = 0; i < pictureBuffer.length(); i = i + 2) {
			String s = pictureBuffer.substring(i, i + 2);
			int b = Integer.parseInt(s, 16);
			byteArrayOutputStream.write(b);
		}

		byte[] array = byteArrayOutputStream.toByteArray();

		// Debugging data...
//		try {
//			FileOutputStream out = new FileOutputStream(File.createTempFile(
//					"img", ".emf", new File("/tmp")));
//			out.write(array);
//			out.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		ByteBuffer byteBuffer = ByteBuffer.wrap(array);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		// Extract the message. All data are little endian.
		// Message address is at position 60
		// Message length is at position 64
		int length = byteBuffer.getInt(60);
		int pos = byteBuffer.getInt(64);

		if (length == 0)
			return; // Nothing to do...
		// read and parse the string...

		byteBuffer.position(pos);
		StringBuffer textBuffer = new StringBuffer();

		for (int i = 0; i < length; i++) {
			textBuffer.append(byteBuffer.getChar());
		}
		String comment = textBuffer.toString();
		if (comment.startsWith("JSesh" + '\0')) {
			comment = comment.substring(comment.indexOf(0) + 1);
			int last = comment.length();
			if (comment.indexOf(0) != -1)
				last = comment.indexOf(0);
			comment = comment.substring(0, last);
			mdcStrings.add(comment);
		}
	}

	public String getMDC() {
		StringBuffer finalText = new StringBuffer();
		for (int i = 0; i < mdcStrings.size(); i++) {
			finalText.append(mdcStrings.get(i));
			if (!((String) mdcStrings.get(i)).endsWith("-"))
				finalText.append("-");
			finalText.append("!\n");
		}
		return finalText.toString();
	}

	// We want to extract the content of \pict groups for \emfblip
	// so : read current group. If we meet \pict => this is a pict
	// if we meet \emfblip => this is an emf file
	// if we meet data and this is an emf file, read data. At the end of the
	// pict,
	// store the data in a convenient byte array.
}
