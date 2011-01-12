package org.qenherkhopeshef.graphics.vectorClipboard;

/**
 * Constants for picture format. Should probably be some kind of enum, but we
 * want java 1.4 compatibility. Due to technical problems with cross-platforms
 * copy/paste, most pictures are copied as part of a "Rich Text Format" file.
 * This works rather well. For instance Word or OpenOffice can read them without
 * problem.
 * 
 * <p>
 * We will provide plain pictures too, but they will probably be restricted to
 * some specific platform.
 * 
 * @author rosmord
 * 
 */
public interface PictureFormat {
	/**
	 * WMF embedded in RTF.
	 */
	int WMF = 1;
	/**
	 * EMF embedded in RTF.
	 */
	int EMF = 2;
	/**
	 * MACPICT embedded in RTF.
	 */
	int MACPICT = 3;
	/**
	 * EMF, will probably only work on windows.
	 */
	int DIRECT_EMF = 4;
	
	/**
	 * Attempt at plain MAC PICT
	 */
	int DIRECT_PICT= 5;
	
	/**
	 * PDF export (needs IText for output. Works only on Mac)
	 */
	int PDF= 6;
	
	/**
	 * EPS export (still in work).
	 */
	int EPS= 7;
	
}	
