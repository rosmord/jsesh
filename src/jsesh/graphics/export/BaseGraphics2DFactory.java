/*
 * Created on 5 juil. 2005 by rosmord
 *
 * TODO document the file BaseGraphics2DFactory.java
 * 
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package jsesh.graphics.export;

import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.io.IOException;

/**
 * Classes who know how to build a graphic of a certain dimension.
 * <p> Used for exporting graphic files.
 * 
 * <p> TODO : New system : the factory is also used for multipaged documents.
 * @author rosmord
 */
public interface BaseGraphics2DFactory {

	/**
	 * @param deviceDimensions size in device dependent space. For instance, pixels for bitmaps.
	 */
	void setDimension(Dimension2D deviceDimensions);
	
    /**
     * Build a graphic file for the export.
   * 
     * @return a StreamGraphics2D
     */
    Graphics2D buildGraphics() throws IOException;

    /**
     * Method to save the graphic whereever it must be saved.
     * <p>In a number of cases, the method will be empty, as the drawing will directly take place
     * on the destination file.
     * <p> When the picture is built in-memory, however, it might be interesting to be able to 
     * call writeGraphics to write them.
     * The method will be called after a graphic has been disposed of.
     */
	void writeGraphics() throws IOException;

}
