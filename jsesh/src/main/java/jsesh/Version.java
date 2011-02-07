package jsesh;

import java.io.IOException;
import java.util.Properties;

/**
 * Gets the version number.
 * @author rosmord
 */
public class Version {

    public static String getVersion() {
        String version = "";
        try {
            Properties properties = new Properties();
            properties.load(Version.class.getResourceAsStream(
                    "/jsesh/version.properties"));
            version = properties.getProperty("jsesh.version");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return version;
    }
}
