/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 * 
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.utils.io;

/**
 * Various utility methods.
 * @author rosmord
 *
 */
public class SystemUtils {
	/**
	 * Return the system default text encoding.
	 * The jdk 1.5 has a method for this, not jdk 1.4. Hence this method. 
	 * @return the system default text encoding.
	 */
	public static String getDefaultEncoding() {
		return System.getProperty("file.encoding");
	}

}
