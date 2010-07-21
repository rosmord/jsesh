package org.qenherkhopeshef.json.parser;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

import org.qenherkhopeshef.json.model.JSONArray;
import org.qenherkhopeshef.json.model.JSONData;
import org.qenherkhopeshef.json.model.JSONNumber;
import org.qenherkhopeshef.json.model.JSONObject;
import org.qenherkhopeshef.json.model.JSONString;
import org.qenherkhopeshef.json.model.JSONfalse;
import org.qenherkhopeshef.json.model.JSONnull;
import org.qenherkhopeshef.json.model.JSONtrue;

public class JSONParser {

	JSONTokenizer tok;

	public JSONData parseJSON(Reader in) throws JSONException {
		JSONData result = null;
		try {
			try {
				tok = new JSONTokenizer(in);
				result = parseJSONData();
			} finally {
				if (in != null)
					in.close();
				in = null;
			}
		} catch (IOException e) {
			throw new JSONException("Error reading data");
		}
		return result;
	}

	private JSONData parseJSONData() {
		JSONData result = null;
		try {
			switch (tok.type) {
			case '{': {
				JSONObject r = new JSONObject();
				tok.next();
				while (tok.type == JSONTokenizer.STRING) {
					String pname = tok.svalue;
					tok.next();
					if (tok.type != ':')
						throw new JSONException("',' expected");
					tok.next();
					JSONData data = parseJSONData();
					tok.next();
					r.setProperty(pname, data);
					if (tok.type == ',') {
						tok.next();
					} else if (tok.type != '}')
						throw new JSONException("} expected");
				}
				if (tok.type != '}')
					throw new JSONException("} expected");
				result = r;
				break;
			}
			case '[': {
				JSONArray r = new JSONArray();
				tok.next();
				while (tok.isJSONDataStart()) {
					JSONData data = parseJSONData();
					r.add(data);
					tok.next();
					if (tok.type == ',') {
						tok.next();
					} else if (tok.type != ']')
						throw new JSONException("] expected");
				}
				if (tok.type != ']')
					throw new JSONException("} expected");
				result = r;
				break;
			}
			case JSONTokenizer.TRUE: {
				result = new JSONtrue();
				break;
			}
			case JSONTokenizer.FALSE: {
				result = new JSONfalse();
				break;
			}
			case JSONTokenizer.NULL: {
				result = new JSONnull();
				break;
			}
			case JSONTokenizer.NUMBER: {
				result = new JSONNumber(tok.nvalue);
				break;
			}
			case JSONTokenizer.STRING:
				result = new JSONString(tok.svalue);
				break;
			case JSONTokenizer.EOF:
				throw new JSONException("Unexpected end of file");
			default:
				// error
				throw new JSONException("Unexpected token");
			}
		} catch (IOException e) {
			throw new JSONException("Error reading data");
		}
		return result;
	}

	class JSONTokenizer {
		private StreamTokenizer streamTokenizer;

		static final int EOF = -1;
		static final int STRING = -2;
		static final int NUMBER = -3;
		static final int TRUE = -4;
		static final int FALSE = -5;
		static final int NULL = -6;
		static final int ERROR = -7;
		// Other values: '[', '{', ']', '}', ',', ':'.

		// Algorithm: (skip whitespaces once)
		// 
		// read char
		// if end of file => EOF
		// else: if '"' => readString
		// else - or digit: read number
		// else if letter : readString, see if it's true, false or null
		// else if c belongs to the set of special chars, it's c
		// in any other case, error
		// skip whitespaces.

		String svalue;
		double nvalue;
		int type;

		public JSONTokenizer(Reader r) throws IOException {
			streamTokenizer = new StreamTokenizer(r);
			streamTokenizer.resetSyntax();
			// Unicode is irrelevant here, as all unicode data will appear in
			// Strings.
			streamTokenizer.wordChars('A', 'Z');
			streamTokenizer.wordChars('a', 'z');
			streamTokenizer.whitespaceChars(0, 32);
			streamTokenizer.quoteChar('"');
			streamTokenizer.parseNumbers();
			next();
		}

		public boolean isJSONDataStart() {
			boolean result = false;
			switch (type) {
			case STRING:
			case NUMBER:
			case FALSE:
			case TRUE:
			case NULL:
			case '[':
			case '{':
				result = true;
			}
			return result;
		}

		public boolean hasNext() {
			return (type != EOF);
		}

		public void next() throws IOException {
			streamTokenizer.nextToken();
			switch (streamTokenizer.ttype) {
			case '"':
				type = STRING;
				svalue = streamTokenizer.sval;
				break;
			case StreamTokenizer.TT_NUMBER:
				type = NUMBER;
				nvalue = streamTokenizer.nval;
				break;
			case StreamTokenizer.TT_WORD:
				if ("true".equals(streamTokenizer.sval)) {
					type = TRUE;
				} else if ("false".equals(streamTokenizer.sval)) {
					type = FALSE;
				} else if ("null".equals(streamTokenizer.sval)) {
					type = NULL;
				} else {
					type = ERROR;
				}
				break;
			case StreamTokenizer.TT_EOF:
				type = EOF;
				break;
			case '{':
			case '}':
			case '[':
			case ']':
			case ',':
			case ':':
				type = streamTokenizer.ttype;
				break;
			default:
				type = ERROR;
				break;
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		StringReader reader= new StringReader("{\"a\": 34, \"b\" : [3,4,5], \"toto\": \"un \\ttableau avec des \\\"dedans\"}");
		JSONParser parser= new JSONParser();
		JSONData dat = parser.parseJSON(reader);
		OutputStreamWriter out= new OutputStreamWriter(System.out);
		dat.write(out);
		out.write("\n");
		out.flush();
		out.close();
		
	}
}
