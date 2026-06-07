package org.qenherkhopeshef.json.model;

/**
 * A convenient generic visitor for JSON structures.
 * 
 * When visiting a JSON structure, it will automatically visit all the substructures, including arrays.
 * 
 * <p> It defines a bunch of methods called <code>preVisitXXX</code> and <code>postVisitXXX</code>, 
 * which are called before and after visiting a given structure.
 * By default, they do nothing, but they can be overridden to do something useful.
 * 
 * <p> Typically, if you want to build java objects from this, you will create some kind of stack. The previsit methods
 * can be used to push incomplete objects (or builders) on the stack, and the postvisit methods can be used to 
 * pop them.
 * @author Serge Rosmorduc
 */

public class JSONDeepVisitor implements JSONVisitor {

	public final void visitArray(JSONArray array) {
		previsitArray(array);
		for (int i= 0; i < array.size(); i ++) {
			preVisitArrayElement(i, array.get(i));
			array.get(i).accept(this);
			postVisitArrayElement(i, array.get(i));
		}
		postVisitArray(array);
	}


	public void visitConstant(Object obj) {
		// Extend if needed
	}

	public void visitNumber(JSONNumber number) {
		// Extend if needed
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


	public void visitString(JSONString string) {
		// Extend if needed
	}


	/**
	 * Called after visiting an array element.
	 * @param i the position of the element in the array.
	 * @param data the representation of the array element.
	 */
	protected void postVisitArrayElement(int i, JSONData data) {
		// Extend if needed
	}


	/**
	 * Called before visiting an array element.
	 * @param i the position of the element in the array.
	 * @param data the representation of the array element.
	 */
	protected void preVisitArrayElement(int i, JSONData data) {
		// Extend if needed
	}

	/**
	 * Called after visiting an array.
	 * @param array
	 */
	protected void postVisitArray(JSONArray array) {
		// Extend if needed
	}

	/**
	 * Called before visiting an array.
	 * @param array
	 */
	protected void previsitArray(JSONArray array) {
		// does nothing.
		
	}

	/**
	 * Called after visiting an object property.
	 * @param i the position of the property in the object. Not very meaningful.
	 * @param propertyName the name of the property.
	 * @param property the value of the property.
	 */
	protected void postVisitObjectProperty(int i, String propertyName, JSONData property) {
		// does nothing.
	}

	/**
	 * Called before visiting an object property.
	 * @param i the position of the property in the object. Not very meaningful.
	 * @param propertyName the name of the property.
	 * @param property the value of the property.
	 */
	protected void preVisitObjectProperty(int i, String propertyName, JSONData property) {
		// Extend if needed
	}


	/**
	 * Called after visiting an object.
	 * @param object
	 */
	protected void postVisitObject(JSONObject object) {
		// Extend if needed	
	}

	/**
	 * Called before visiting an object.
	 * @param object
	 */
	protected void preVisitObject(JSONObject object) {
		// Extend if needed
	}


}
