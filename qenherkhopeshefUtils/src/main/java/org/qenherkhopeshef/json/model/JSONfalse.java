package org.qenherkhopeshef.json.model;

import java.io.IOException;
import java.io.Writer;

public class JSONfalse extends JSONConstant {
	public void accept(JSONVisitor visitor) {
		visitor.visitConstant(Boolean.FALSE);
	}
	
	public void write(Writer writer) throws IOException {
		writer.write("false");
	}
}
