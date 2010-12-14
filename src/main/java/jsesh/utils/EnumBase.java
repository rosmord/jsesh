/*
 * Created on 9 ao�t 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.utils;

import java.io.Serializable;

/**
 * Base type for enum-like classes, for jdk 1.4.
 *   @author rosmord
 *
 */
public class EnumBase implements Serializable {
	private final String designation; 
	private final int id;
	
	public EnumBase(int id, String designation) {
		this.id= id;
		this.designation= designation;
	}
	
	
	/**
	 * @return the designation of the Enum member.
	 */
	public String getDesignation() {
		return designation;
	}
	
	/* 
	 * You should prefer using equals to compare EnumBase objects,
	 * because plain address comparison might not work with network objects.
	 */
	public boolean equals(Object obj) {
		EnumBase e= (EnumBase) obj;
		return id == e.id;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return id;
	}
	
	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return id;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return designation;
	}

}
