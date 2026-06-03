package org.qenherkhopeshef.json.model;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JSONArray extends JSONData {
	private List<JSONData> list= new ArrayList<>();
	
	
	public void accept(JSONVisitor visitor) {
		visitor.visitArray(this);
	}


	public void add(int index, JSONData element) {
		list.add(index, element);
	}


	public boolean add(JSONData o) {
		return list.add(o);
	}

	public void clear() {
		list.clear();
	}


	public boolean contains(JSONData o) {
		return list.contains(o);
	}


	public boolean containsAll(Collection<? extends JSONData> c) {
		return list.containsAll(c);
	}


	public JSONData get(int index) {
		return (JSONData) list.get(index);
	}


	public int indexOf(JSONData o) {
		return list.indexOf(o);
	}


	public boolean isEmpty() {
		return list.isEmpty();
	}


	public JSONData remove(int index) {
		return (JSONData) list.remove(index);
	}


	public boolean remove(JSONData o) {
		return list.remove(o);
	}


	public boolean removeAll(Collection<? extends JSONData> c) {
		return list.removeAll(c);
	}


	public JSONData set(int index, JSONData element) {
		return (JSONData) list.set(index, element);
	}


	public int size() {
		return list.size();
	}


	public void write(Writer writer) throws IOException {
		writer.write("[");
		for (int i=0; i < size(); i++) {
			get(i).write(writer);
			if (i < size() - 1)
				writer.write(", ");
		}
		writer.write("]");
	}
}
