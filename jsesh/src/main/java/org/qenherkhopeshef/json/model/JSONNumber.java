package org.qenherkhopeshef.json.model;

import java.io.IOException;
import java.io.Writer;

public class JSONNumber extends JSONData {
	double number;
	
	public JSONNumber(double nvalue) {
		number= nvalue;
	}

	int getIntValue() {
		return (int) Math.floor(number);
	}
	
	double getDoubleValue() {
		return number;
	}

	public void accept(JSONVisitor visitor) {
		visitor.visitNumber(this);
	}
	
	public boolean isInteger() {
		return number - Math.floor(number) < 1e-6;
	}
	
	public void write(Writer writer) throws IOException {
		if (isInteger())
			writer.write(""+getIntValue());
		else
			writer.write(""+number);
	}
}
