package jsesh.mdc.lex;

public class MDCSign {
	public MDCSign(int type, String string) {
		setType(type);
		setString(string);
	}

	private int type;
	/**
	 * Gets the value of type
	 *
	 * @return the value of type
	 */
	public int getType() {
		return this.type;
	}

	/**
	 * Sets the value of type
	 *
	 * @param argType Value to assign to this.type
	 */
	public void setType(int argType) {
		this.type = argType;
	}

	/**
	 * Gets the value of string
	 *
	 * @return the value of string
	 */
	public String getString() {
		return this.string;
	}

	/**
	 * Sets the value of string
	 *
	 * @param argString Value to assign to this.string
	 */
	public void setString(String argString) {
		this.string = argString;
	}

	private String string;

}
