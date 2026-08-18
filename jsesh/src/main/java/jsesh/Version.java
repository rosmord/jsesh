/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 * 
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
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
