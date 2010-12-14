/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jsesh.utilitySoftwares.rtfCleaner;

import java.io.File;
import java.io.FilenameFilter;
/**
 *
 * @author admin
 */
class RTFFilter implements FilenameFilter{

    public RTFFilter() {
    }

    public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(".rtf");
    }

}
