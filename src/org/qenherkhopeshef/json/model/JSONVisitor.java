package org.qenherkhopeshef.json.model;

public interface JSONVisitor {

	void visitArray(JSONArray array);

	/**
	 * Visit a constant, which may be true, false, or null.
	 * @param obj
	 */
	void visitConstant(Object obj);

	void visitNumber(JSONNumber number);

	void visitObject(JSONObject object);

	void visitString(JSONString string);
}
