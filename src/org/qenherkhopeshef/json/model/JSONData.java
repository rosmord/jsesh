package org.qenherkhopeshef.json.model;

import java.io.IOException;
import java.io.Writer;

public abstract class JSONData {
	abstract public void accept(JSONVisitor visitor);
	
	abstract public void write(Writer writer) throws IOException;
}
