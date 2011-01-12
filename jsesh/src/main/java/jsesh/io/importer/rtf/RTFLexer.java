package jsesh.io.importer.rtf;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Lexical analyser for RTF.
 * 
 * @author rosmord
 */

public class RTFLexer {

	private int tokenType;
	private StringBuffer command = new StringBuffer();
	private StringBuffer numericValue = new StringBuffer();
	private char charValue;

	private Reader reader;

	private LexerState currentState;

	private LexerState initial = new InitialState();
	private LexerState finalState = new SimpleFinalState();
	private LexerState controlStart = new ControlStartState();
	private LexerState inControl = new InControlState();
	private LexerState inNumber = new InNumberState();

	private int nextChar;

	public RTFLexer(InputStream in) throws IOException {
		this(new InputStreamReader(in, "ISO-8859-1"));
	}

	public RTFLexer(Reader in) throws IOException {
		this.reader = in;
		nextChar = in.read();
	}

	/**
	 * Reads the next token.
	 * 
	 * @throws IOException
	 */
	public void next() throws IOException {
		currentState = initial;
		LexerState nextState;
		do {
			nextState = currentState.accept(nextChar);
			if (nextState != null) {
				nextChar = reader.read();
				currentState = nextState;
			}
		} while (nextState != null);
	}

	private interface LexerState {
		LexerState accept(int charCode);
	}

	/**
	 * Returns the type of the current token.
	 * 
	 * @see RTFTokenType
	 * @return
	 */
	public int getTokenType() {
		return tokenType;
	}

	/**
	 * The command just read. Precondition: getTokenType() is COMMAND or
	 * NUMERIC_COMMAND.
	 * 
	 * @return
	 */
	public String getCommand() {
		if (getTokenType() != RTFTokenType.COMMAND
				&& getTokenType() != RTFTokenType.NUMERIC_COMMAND) {
			throw new IllegalStateException("No command available");
		}
		return command.toString();
	}

	/**
	 * The numeric value associated with a command. Precondition: getTokenType
	 * is NUMERIC_COMMAND.
	 * 
	 * @return
	 */
	public int getNumericValue() {
		if (getTokenType() != RTFTokenType.NUMERIC_COMMAND) {
			throw new IllegalStateException("No command available");
		}
		return Integer.parseInt(numericValue.toString());
	}

	/**
	 * The char just read. Precondition: getTokenType is CHAR
	 * 
	 * @return
	 */
	public char getCode() {
		if (getTokenType() != RTFTokenType.CHAR) {
			throw new IllegalStateException("No command available");
		}
		return charValue;
	}

	// Definitions for the various states available for this automaton.

	/**
	 * Final state without any followers.
	 */
	private class SimpleFinalState implements LexerState {
		public LexerState accept(int charCode) {
			return null;
		}
	}

	/**
	 * First state. Does a lot of work.
	 * 
	 * @author rosmord
	 * 
	 */
	private class InitialState implements LexerState {

		public LexerState accept(int charCode) {
			LexerState nextState = null;
			switch (charCode) {
			case '{':
				tokenType = RTFTokenType.OPEN_GROUP;
				nextState = finalState;
				break;
			case '}':
				tokenType = RTFTokenType.CLOSE_GROUP;
				nextState = finalState;
				break;
			case '\\':
				command.setLength(0);
				command.append("\\");
				nextState = controlStart;
				break;
			case -1:
				tokenType = RTFTokenType.EOF;
				break;
			default:
				tokenType = RTFTokenType.CHAR;
				charValue = (char) charCode;
				nextState = finalState;
				break;
			}
			return nextState;
		}
	}

	/**
	 * @author rosmord
	 * 
	 */
	public class InNumberState implements LexerState {

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.io.importer.rtf.RTFLexer.LexerState#accept(int)
		 */
		public LexerState accept(int charCode) {
			// TODO Auto-generated method stub
			return null;
		}

	}

	/**
	 * @author rosmord
	 * 
	 */
	public class InControlState implements LexerState {

		public LexerState accept(int charCode) {
			LexerState nextState = null;
			if (charCode >= 'a' && charCode <= 'z') {
				command.append((char) charCode);
				nextState = inControl;
			} else if ((charCode >= '0' && charCode <= '9') || charCode == '-') {
				numericValue.setLength(0);
				numericValue.append((char) charCode);
				tokenType = RTFTokenType.NUMERIC_COMMAND;
				nextState = inNumber;
			} else if (charCode == ' ') {
				nextState = finalState;
			}

			return nextState;
		}

	}

	/**
	 * @author rosmord
	 * 
	 */
	public class ControlStartState implements LexerState {

		public LexerState accept(int charCode) {
			command.append((char) charCode);
			if (charCode >= 'a' && charCode <= 'z') {
				tokenType = RTFTokenType.COMMAND;
				return inControl;
			} else {
				tokenType = RTFTokenType.COMMAND;
				return finalState;
			}
		}
	}

	public String getTokenRepresentation() {
		switch (tokenType) {
		case RTFTokenType.CHAR:
			return "char " + getCode();
		case RTFTokenType.COMMAND:
			return "command " + getCommand();

		case RTFTokenType.NUMERIC_COMMAND:
			return "command " + getCommand() + " arg " + getNumericValue();
		case RTFTokenType.EOF:
			return "EOF";
		case RTFTokenType.OPEN_GROUP:
			return "{";
		case RTFTokenType.CLOSE_GROUP:
			return "}";
		default:
			throw new RuntimeException("Unexpected char ???");
		}
	}
}
