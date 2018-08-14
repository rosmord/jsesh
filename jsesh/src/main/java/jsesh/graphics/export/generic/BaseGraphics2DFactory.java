/*
 Copyright Serge Rosmorduc
 contributor(s) : Serge J. P. Thomas for the fonts
 serge.rosmorduc@qenherkhopeshef.org

 This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

 This software is governed by the CeCILL license under French law and
 abiding by the rules of distribution of free software.  You can  use, 
 modify and/ or redistribute the software under the terms of the CeCILL
 license as circulated by CEA, CNRS and INRIA at the following URL
 "http://www.cecill.info". 

 As a counterpart to the access to the source code and  rights to copy,
 modify and redistribute granted by the license, users are provided only
 with a limited warranty  and the software's author,  the holder of the
 economic rights,  and the successive licensors  have only  limited
 liability. 

 In this respect, the user's attention is drawn to the risks associated
 with loading,  using,  modifying and/or developing or reproducing the
 software by the user in light of its specific status of free software,
 that may mean  that it is complicated to manipulate,  and  that  also
 therefore means  that it is reserved for developers  and  experienced
 professionals having in-depth computer knowledge. Users are therefore
 encouraged to load and test the software's suitability as regards their
 requirements in conditions enabling the security of their systems and/or 
 data to be ensured and,  more generally, to use and operate it in the 
 same conditions as regards security. 

 The fact that you are presently reading this means that you have had
 knowledge of the CeCILL license and that you accept its terms.
 */
/*
 * Created on 5 juil. 2005 by rosmord
 */
package jsesh.graphics.export.generic;

import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.io.IOException;

/**
 * Classes who know how to build a graphic of a certain dimension.
 * <p>
 * Used for exporting graphic files.
 *
 * <p>
 * TODO : New system : the factory is also used for multipaged documents.
 *
 * @author rosmord
 */
public interface BaseGraphics2DFactory {

    /**
     * @param deviceDimensions size in device dependent space. For instance,
     * pixels for bitmaps.
     */
    void setDimension(Dimension2D deviceDimensions);

    /**
     * Build a graphic file for the export.
     *
     * @return a StreamGraphics2D
     * @throws java.io.IOException
     */
    Graphics2D buildGraphics() throws IOException;

    /**
     * Method to save the graphic wherever it must be saved.
     * <p>
     * In particular, this method can be called in paginated contexts to draw a
     * complete page.
     * <p>
     * In a number of cases, the method will be empty, as the drawing will
     * directly take place on the destination file.
     * <p>
     * When the picture is built in-memory, however, it might be interesting to
     * be able to call writeGraphics to write them. The method will be called
     * after a graphic has been disposed of.
     *
     * @throws java.io.IOException
     */
    void writeGraphics() throws IOException;
    
    /**
     * Called whenever a new page is explicitly needed.
     * NOTE : the BaseGraphics2DFactory is probably not the 
     * best 
     * @throws IOException 
     */
    void newPage() throws IOException ;
}
