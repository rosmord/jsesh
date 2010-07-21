package org.qenherkhopeshef.json.model;

import java.io.IOException;
import java.io.Writer;

public class JSONtrue extends JSONConstant {

	public void accept(JSONVisitor visitor) {
		visitor.visitConstant(Boolean.TRUE);
	}

	public void write(Writer writer) throws IOException {
		writer.write("true");	
	}
}
