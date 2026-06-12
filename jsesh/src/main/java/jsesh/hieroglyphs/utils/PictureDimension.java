package jsesh.hieroglyphs.utils;


/**
 * Dimensions of a picture, in pixels.
 * 
 * <p> More abstract than `java.awt.Dimension`, independant of awt, and immutable.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
public record PictureDimension(int width, int height) {
}
