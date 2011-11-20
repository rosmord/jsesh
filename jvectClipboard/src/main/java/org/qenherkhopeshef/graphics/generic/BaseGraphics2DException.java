/*
 * Created on 4 juil. 2005 by rosmord
 *
 * TODO document the file BaseGraphics2DException.java
 * 
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package org.qenherkhopeshef.graphics.generic;

/**
 * An error occuring while manipulating a BaseGraphics2D.
 * <p>Can encapsulate another exception, which 
 * will probably be really a IOException. 
 * @author rosmord
 */
public class BaseGraphics2DException extends Exception {
    private Exception baseException=null;
    
    /**
     * 
     */
    public BaseGraphics2DException(Exception baseException) {
        super(baseException.getMessage());
        this.baseException= baseException;
    }
    
    public BaseGraphics2DException(String message) {
        super(message);
    }
    
    public BaseGraphics2DException(String message, Exception baseException) {
        super(message);
        this.baseException= baseException; 
    }
    
    /**
     * Return the exception that created the present one, if any.
     * <p> Can return null.
     * @return Returns the baseException.
     */
    public Exception getBaseException() {
        return baseException;
    }
}
