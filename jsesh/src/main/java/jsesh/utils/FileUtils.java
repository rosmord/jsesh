/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 7 d�c. 2004
 *
 */
package jsesh.utils;

import java.io.File;
import java.util.Locale;

/**
 * @author Serge Rosmorduc
 */
public class FileUtils {

    /**
     * Return a file name for a certain type of files, ensuring the extensions is correct.
     * <p>If there is no extensions, it will be added.
     * <p>If the extensions is wrong, it will be replaced.
     * <p>If file is null, null will be returned.
     * @param file
     * @param type : the extensions to use (without dot)
     * @return a file name for a certain type of files, ensuring the extensions is correct.
     */
    public static File buildFileWithExtension(File file, String type) {
    	if (file == null)
    		return null;
    	File directory = file.getParentFile();
    	String base = file.getName();
    	int extIndex = base.lastIndexOf('.');
    	if (extIndex == -1)
    		extIndex = base.length();
    	file = new File(directory, base.substring(0, extIndex) + "."
    			+ type);
    	return file;
    
    }
    
    /**
     * Return a file extensions, or null if there is none.
     * @param file
     * @return a file extensions, or null if there is none.
     */
    public static String getExtension(File file) {        
        String name= file.getName();
        int idx= name.lastIndexOf('.');
        String result= null;
        if (idx != -1) {
            result= name.substring(idx+1).toLowerCase(Locale.ENGLISH);
        }
        return result;
    }

}
