package org.qenherkhopeshef.json.model;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

public class JSONObject extends JSONData{
	HashMap<String, JSONData> map= new HashMap<>();
	
	public void setProperty(String name, JSONData value) {
		map.put(name, value);
	}
	
	public JSONData getProperty(String name) {
		return (JSONData) map.get(name);
	}
	
	public boolean hasProperty(String name) {
		return map.containsKey(name);
	}
	
	/**
	 * Returns the names of all defined preconditions.
	 * @return a list of Strings
	 */
	public String[] getPropertyList() {
		return (String[]) map.keySet().toArray(new String[map.keySet().size()]);
	}
	
	public void accept(JSONVisitor visitor) {
		visitor.visitObject(this);
	}
	
	public void write(Writer writer) throws IOException {
		writer.write("{");
		String[] keys = getPropertyList();
		for (int i=0; i < keys.length; i++) {
			new JSONString(keys[i]).write(writer);
			writer.write(": ");
			getProperty(keys[i]).write(writer);
			if (i < keys.length - 1)
				writer.write(", ");
		}
		writer.write("}");	
	}
}
