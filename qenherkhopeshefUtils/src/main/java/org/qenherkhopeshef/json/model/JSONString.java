package org.qenherkhopeshef.json.model;

import java.io.IOException;
import java.io.Writer;

public class JSONString extends JSONData {
	private String value = "";

	public JSONString() {
	}

	public JSONString(String value) {
		super();
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void accept(JSONVisitor visitor) {
		visitor.visitString(this);
	}

	public static JSONString buildFromJSON(String txt) {
		return null;
	}

	public String getProtectedValue() {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
			case '"':
			case '\\':
			case '/':
				result.append('\\');
				result.append(c);
				break;
			case '\b':
				result.append('\\');
				result.append('b');
				break;
			case '\f':
				result.append('\\');
				result.append('f');
				break;
			case '\n':
				result.append('\\');
				result.append('n');
				break;
			case '\r':
				result.append('\\');
				result.append('r');
				break;
			case '\t':
				result.append('\\');
				result.append('t');
				break;
			default:
				result.append(c);
				break;
			}
		}
		return result.toString();
	}

	public void write(Writer writer) throws IOException {
		writer.write('"');
		writer.write(getProtectedValue());
		writer.write('"');
	}
}
