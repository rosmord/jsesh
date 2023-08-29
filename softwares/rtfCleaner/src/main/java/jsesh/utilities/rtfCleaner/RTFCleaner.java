package jsesh.utilities.rtfCleaner;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Clean a RTF which contains MACPICT images. suppress any ff00 code included by
 * word.
 * This software is used to allow the import of word documents made with JSesh
 * in InDesign
 * 
 * @author Serge Rosmorduc
 * 
 */

public class RTFCleaner {

	// reads the text, one token at a time
	// relevant token are:
	// \macpict
	// \ + non alphabetic code.
	// { and } (make the grammar context sensitive
	// and of course ff00

	public void cleanRTF(InputStream in, OutputStream out) throws IOException {
		int c;
		this.out = out;
		try {
			SimpleTokenizer tokenizer = new SimpleTokenizer();

			while ((c = in.read()) != -1) {
				tokenizer.inputCode(c);
			}
			tokenizer.inputCode(-1);
		} finally {
			if (out != null) out.close();
			if (in != null) in.close();
			out = null;
		}
	}

	private interface TokenizerNode {
		TokenizerNode inputCode(int code);
	}

	public OutputStream out;

	private class SimpleTokenizer {
		ArrayList currentTokens = new ArrayList();
		private int depth = 0;
		private int imageDepth = -1; // Not in image scope
		private TokenizerNode currentNode;

		TokenizerNode startNode = new TokenizerNode() {
			public TokenizerNode inputCode(int code) {
				currentTokens.add(code);
				TokenizerNode next = this;
				switch (code) {
				case '\\':
					next = backSlashRead;
					break;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					next = digitRead;
					break;
				case '{':
					openAccolade();
					break;
				case '}':
					closeAccolade();
					break;
				default:
					outToken();
					break;
				}
				return next;
			}

		};

		TokenizerNode backSlashRead = new TokenizerNode() {

			public TokenizerNode inputCode(int code) {
				currentTokens.add(code);
				TokenizerNode next = this;
				if ((code >= 'a' && code <= 'z')
						|| (code >= 'A' && code <= 'Z')) {
					next = inCommand;
				} else {
					outToken();
					next = startNode;
				}
				return next;
			}

		};

		TokenizerNode inCommand = new TokenizerNode() {

			public TokenizerNode inputCode(int code) {
				TokenizerNode next;
				if ((code >= 'a' && code <= 'z')
						|| (code >= 'A' && code <= 'Z')) {
					currentTokens.add(code);
					next = this;
				} else {
					processCommand();
					next = startNode.inputCode(code);
				}
				return next;
			}

		};

		TokenizerNode digitRead = new TokenizerNode() {

			public TokenizerNode inputCode(int code) {
				TokenizerNode next = this;
				if (isHexDigit(code) || isSpace(code)) {
					currentTokens.add(code);
					next = digitRead;
				} else if (code == '}') {
					clearTokenEndIfNeeded();
					currentTokens.add(code);
					closeAccolades();
					next = startNode;
				} else {
					outToken();
					next = startNode.inputCode(code);
				}
				return next;
			}
		};

		public SimpleTokenizer() {
			currentNode = startNode;
		}

		protected boolean isSpace(int code) {
			return code == ' ' || code == '\t' || code == '\r' || code == '\n'; 
		}

		protected void processCommand() {
			String macpict = "\\macpict";

			if (tokenIsEqualsTo(macpict)) {
				imageDepth = depth;
			}
			outToken();
		}

		private boolean tokenIsEqualsTo(String text) {
			boolean stringEquals;

			if (currentTokens.size() == text.length()) {
				int i = 0;
				stringEquals = true;
				while (stringEquals && i < currentTokens.size()) {
					stringEquals = getTokenChar(i) == text
							.charAt(i);
					i++;
				}
			} else {
				stringEquals = false;
			}
			return stringEquals;
		}

		public void inputCode(int c) {
			currentNode = currentNode.inputCode(c);
		}

		protected void closeAccolades() {
			if (depth == imageDepth)
				imageDepth = -1;
			depth--;
			outToken();
		}

		protected void clearTokenEndIfNeeded() {
			if (depth == imageDepth) {
				int i = currentTokens.size()-1;
				boolean onlyZeroMet = true;
				while (i >= 0 && onlyZeroMet) {
					int code = getTokenChar(i);
					if (code == '0') {
						currentTokens.remove(i);
					} else if (isHexDigit(code)) {
						onlyZeroMet = false;
					}
					i--;
				}
			}
		}

		private boolean isHexDigit(int code) {
			int code1= Character.toLowerCase((char) code);
			return (code1 >= '0' && code1 <= '9') || (code1 >= 'a' && code1 <= 'f');
		}

		private void outToken() {
			for (int i = 0; i < currentTokens.size(); i++)
				print(getTokenChar(i));
			currentTokens.clear();
		}

		private int getTokenChar(int i) {
			return ((Integer) currentTokens.get(i)).intValue();
		}

		private void print(int code) {
			try {
				if (code != -1)
					out.write(code);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private void openAccolade() {
			depth++;
			outToken();
		}

		private void closeAccolade() {
			depth--;
			outToken();
		}
		
		public String getCurrentTokenAsString() {
			StringBuffer buffer= new StringBuffer();
			for (int i=0; i< currentTokens.size(); i++) {
				int code= getTokenChar(i);
				buffer.append((char)code);
			}
			return buffer.toString();
		}
		
		public String toString() {
			return getCurrentTokenAsString();
		}
	}
	
	public static void main(String[] args) throws IOException {
		FileInputStream in= new FileInputStream(args[0]);
		FileOutputStream out= new FileOutputStream(args[1]);
		RTFCleaner rtCleaner= new RTFCleaner();
		rtCleaner.cleanRTF(in, out);
	}
}
