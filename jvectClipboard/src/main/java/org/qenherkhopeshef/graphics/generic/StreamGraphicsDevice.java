/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 28 juin 2005
 *
 */
package org.qenherkhopeshef.graphics.generic;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;

/**
 * @author Serge Rosmorduc
 */
public class StreamGraphicsDevice extends GraphicsDevice {

    /* (non-Javadoc)
     * @see java.awt.GraphicsDevice#getType()
     */
    public int getType() {        
        return GraphicsDevice.TYPE_PRINTER;
    }

    /* (non-Javadoc)
     * @see java.awt.GraphicsDevice#getIDstring()
     */
    public String getIDstring() {      
        return "Dummy";
    }

    /* (non-Javadoc)
     * @see java.awt.GraphicsDevice#getConfigurations()
     */
    public GraphicsConfiguration[] getConfigurations() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see java.awt.GraphicsDevice#getDefaultConfiguration()
     */
    public GraphicsConfiguration getDefaultConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }

}
