package org.qenherkhopeshef.json.model;

import java.io.IOException;
import java.io.Writer;

public class JSONnull extends JSONConstant {
	public void accept(JSONVisitor visitor) {
		visitor.visitConstant(null);
	}
	
	
	public void write(Writer writer) throws IOException {
		writer.write("null");
	}
}
