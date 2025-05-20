package jsesh.mdcDisplayer.preferences;

public class CartoucheSizeHelper {

	/**
	 * Compute the size needed for cartouches starts and ends along the main
	 * axis. Use the basic sizes values declared in this class.
	 * @param drawingSpecification the corresponding {@link DrawingSpecification}
	 * @param type
	 *            the type of cartouche ('c', 's', 'h', 'f' or 'F')
	 * @param element :
	 *            0,1,2,3, depending on the type of cartouche.
	 * @return a size.
	 */

	//public abstract float computeCartouchePartLength(int type, int element);

	public static float computeCartouchePartLength(DrawingSpecification drawingSpecification, int type, int element) {
	    float result = 0;
	    switch (type) {
	    case 'c':
	        switch (element) {
	        case 0:
	            result = 0;
	            break;
	        case 1:
	            result = drawingSpecification.getCartoucheLoopLength();
	            break;
	        case 2:
	            result = drawingSpecification.getCartoucheLoopLength() + drawingSpecification.getCartoucheknotLength();
	            break;
	        default:
	            throw new RuntimeException("bad value for element " + element);
	        }
	        break;
	    case 's':
	        switch (element) {
	        case 1:
	            result = drawingSpecification.getHwtSmallMargin()
	                    + drawingSpecification.getCartoucheLineWidth();
	            break;
	        case 2:
	            result = drawingSpecification.getHwtSmallMargin()
	                    + drawingSpecification.getCartoucheLineWidth()
	                    + drawingSpecification.getSerekhDoorSize();
	            break;
	        }
	        break;
	    case 'h':
	        switch (element) {
	        case 0:
	            break;
	        case 1:
	            result = drawingSpecification.getHwtSmallMargin() + drawingSpecification.getCartoucheLineWidth();
	            break;
	        case 2:
	        case 3:
	            result = drawingSpecification.getHwtSmallMargin() + drawingSpecification.getCartoucheLineWidth()
	                    + drawingSpecification.getHwtSquareSize();
	            break;
	        default:
	            throw new RuntimeException("bad value for element " + element);
	        }
	        break;
	    case 'F':                
	    case 'f':
	        if (element != 0) {
	            result = drawingSpecification.getHwtSmallMargin()
	                    + drawingSpecification.getEnclosureBastionDepth()
	                    + drawingSpecification.getCartoucheLineWidth();
	        }
	        break;
	    }
	    return result;
	}

	/**
	 * length along the secondary axis of a cartouche from the cartouche
	 * external side to the text inside.
	 * @param drawingSpecification the corresponding drawing specifications.
	 * @param type
	 * @return the length along the secondary axis of a cartouche from the
	 *         cartouche external side to the text inside.
	 */
	public static float computeCartoucheSecondaryLength(DrawingSpecification drawingSpecification, int type) {
	    float result = 0;
	    switch (type) {
	    case 'c':
	    case 's':
	    case 'h':
	        result = drawingSpecification.getCartoucheMargin() + drawingSpecification.getCartoucheLineWidth();
	        break;
            case 'F':                	                    
	    case 'f':
	        result = drawingSpecification.getCartoucheMargin() + drawingSpecification.getCartoucheLineWidth()
	                + drawingSpecification.getEnclosureBastionDepth();
	        break;
	    }
	    return result;
	}
	
}
