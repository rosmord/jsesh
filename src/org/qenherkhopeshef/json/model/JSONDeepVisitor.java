package org.qenherkhopeshef.json.model;

/**
 * A convenient generic visitor for JSON structures.
 * @author admin
 */

public class JSONDeepVisitor implements JSONVisitor {

	public void visitArray(JSONArray array) {
		previsitArray(array);
		for (int i= 0; i < array.size(); i ++) {
			preVisitArrayElement(i, array.get(i));
			array.get(i).accept(this);
			postVisitArrayElement(i, array.get(i));
		}
		postVisitArray(array);
	}

	private void postVisitArrayElement(int i, JSONData data) {
	}

	private void preVisitArrayElement(int i, JSONData data) {
	}

	private void postVisitArray(JSONArray array) {
	}

	private void previsitArray(JSONArray array) {
		// TODO Auto-generated method stub
		
	}

	public void visitConstant(Object obj) {
		// Extend if needed
	}

	public void visitNumber(JSONNumber number) {
		// TO EXTEND if needed
	}

	public void visitObject(JSONObject object) {
		preVisitObject(object);
		String[] keys = object.getPropertyList();
		for (int i= 0; i< keys.length; i++) {
			JSONData property = object.getProperty(keys[i]);
			preVisitObjectProperty(i,keys[i],property);
			property.accept(this);
			postVisitObjectProperty(i,keys[i],property);
		}
		postVisitObject(object);
	}

	private void postVisitObjectProperty(int i, String string, JSONData property) {
		
	}

	/**
	 * Called before looking more closely at a given property.
	 * @param i : the position of the property. rather meaningless, but is useful for printing.
	 * @param string
	 * @param property
	 */
	
	private void preVisitObjectProperty(int i, String string, JSONData property) {
		
	}

	private void postVisitObject(JSONObject object) {
		// TODO Auto-generated method stub
		
	}

	private void preVisitObject(JSONObject object) {
		// TODO Auto-generated method stub
		
	}

	public void visitString(JSONString string) {
		// DO NOTHING
	}

}
