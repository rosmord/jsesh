package jsesh.mdc.model;

/**
 * This file is free Software
 * (c) Serge Rosmorduc
 * @author rosmord
 *
 */
public class NoSuchModifierException extends Exception {

	String modifierName;
	
	/**
	 * Constructor for NoSuchModifierException.
	 */
	public NoSuchModifierException(String modifierName) {
		super();
		this.modifierName= modifierName;
	}

	/**
	 * Constructor for NoSuchModifierException.
	 * @param message
	 */
	public NoSuchModifierException(String message, String modifierName) {
		super(message);
		this.modifierName= modifierName;
	}

	/**
	 * Constructor for NoSuchModifierException.
	 * @param message
	 * @param cause
	 */
	public NoSuchModifierException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor for NoSuchModifierException.
	 * @param cause
	 */
	public NoSuchModifierException(Throwable cause) {
		super(cause);
	}

}
