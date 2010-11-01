package org.qenherkhopeshef.swingUtils.errorHandler;

/**
 * A specific type of exception whose messages are supposed to be read by users.
 * The message is associated with a message key, which should be used by the interface
 * to find the relevant message.
 * @author rosmord
 */

public class UserMessage extends RuntimeException {
	private String messageKey;
	
	public UserMessage() {
		super();
	}

	public UserMessage(String message, Throwable cause) {
		super(message, cause);
	}

	public UserMessage(String message) {
		super(message);
	}

	public UserMessage(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Sets the key used to build a localized message.
	 * @param messageKey
	 */
	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}
	
	/**
	 * Returns
	 **/
	
	public String getMessageKey() {
		return messageKey;
	}
	
}
