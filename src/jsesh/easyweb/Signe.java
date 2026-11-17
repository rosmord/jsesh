/*
 * Created on 15 déc. 2003
 *
 */
package jsesh.easyweb;

/**
 * Représente un signe dans un mot.
 * @author rosmord
 *
 */

public class Signe {
	private String code;
	private boolean isDeterminative;
	
	
	public Signe(String code) {
		this.code= code;
		isDeterminative= false;
	}
	/**
	 * Retourne le code du signe.
	 * @return le code du signe.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * précise si le signe est un déterminatif.
	 * 
	 * @return if the sign is a determinative
	 */
	public boolean isDeterminative() {
		return isDeterminative;
	}

	/**
	 * @param string
	 */
	public void setCode(String string) {
		code = string;
	}

	/**
	 * @param b
	 */
	public void setDeterminative(boolean b) {
		isDeterminative = b;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String result= code;
		if (isDeterminative)
			result+= "(DET)";
		return result;	
	}

}
