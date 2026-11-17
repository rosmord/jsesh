/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 7 déc. 2004
 *
 */
package jsesh.utils;

import java.io.File;

/**
 * @author Serge Rosmorduc
 */
public class FileUtils {

    /**
     * Return a file name for a certain type of files, ensuring the extension is correct.
     * @return
     */
    public static File buildFileWithExtension(File file, String type) {
    	File directory = file.getParentFile();
    	String base = file.getName();
    	int extIndex = base.lastIndexOf('.');
    	if (extIndex == -1)
    		extIndex = base.length();
    	file = new File(directory, base.substring(0, extIndex) + "."
    			+ type);
    	return file;
    
    }

}
