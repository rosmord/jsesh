package jsesh;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gets the version number.
 * @author rosmord
 */
public class Version {

    public static String getVersion() {	    	
        String version;
        try {
            Properties properties = new Properties();
            properties.load(Version.class.getResourceAsStream(
                    "/jsesh/version.properties"));
            version = properties.getProperty("jsesh.version");
        } catch (IOException e) {
            Logger.getLogger(Version.class.getName()).log(Level.WARNING, "No version number available");
            version= "0";
        }
        return version;
    }
}
