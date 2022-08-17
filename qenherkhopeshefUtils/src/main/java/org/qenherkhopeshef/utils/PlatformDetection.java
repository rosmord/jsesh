/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package org.qenherkhopeshef.utils;

import java.util.Locale;

/**
 * @author rosmord
 *
 */
public class PlatformDetection {
	public static final int UNIX= 0;
	public static final int MACOSX= 1;
	public static final int WINDOWS= 2;
	
	public static int getPlatform() {
		int result= UNIX;
		String osName= System.getProperty("os.name").toLowerCase(Locale.ENGLISH); 
		if (osName.startsWith(
				"windows")) {
			result= WINDOWS;
		} else if (osName.startsWith(
				"mac os x")) {
			result= MACOSX;
		}
		return result;
	}

	public static String getMacUserFontDir() {
		return System.getProperty("user.home") + "/Library/Fonts";
	}
	

}
