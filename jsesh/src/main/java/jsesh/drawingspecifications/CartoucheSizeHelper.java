package jsesh.drawingspecifications;

public class CartoucheSizeHelper {

	/**
	 * Compute the size needed for cartouches starts and ends along the main
	 * axis. Use the basic sizes values declared in this class.
	 * @param jseshStyle the corresponding {@link JSeshStyle}
	 * @param type
	 *            the type of cartouche ('c', 's', 'h', 'f' or 'F')
	 * @param element :
	 *            0,1,2,3, depending on the type of cartouche.
	 * @return a size.
	 */

	//public abstract float computeCartouchePartLength(int type, int element);

	public static float computeCartouchePartLength(JSeshStyle jseshStyle, int type, int element) {
		GeometrySpecification geometry = jseshStyle.geometry();
	    float result = 0;
	    switch (type) {
	    case 'c':
	        switch (element) {
	        case 0:
	            result = 0;
	            break;
	        case 1:
	            result = geometry.cartoucheLoopLength();
	            break;
	        case 2:
	            result = geometry.cartoucheLoopLength() + geometry.cartoucheLineWidth();
	            break;
	        default:
	            throw new RuntimeException("bad value for element " + element);
	        }
	        break;
	    case 's':
	        switch (element) {
	        case 1:
	            result = geometry.hwtSmallMargin()
	                    + geometry.cartoucheLineWidth();
	            break;
	        case 2:
	            result = geometry.hwtSmallMargin()
	                    + geometry.cartoucheLineWidth()
	                    + geometry.serekhDoorSize();
	            break;
	        }
	        break;
	    case 'h':
	        switch (element) {
	        case 0:
	            break;
	        case 1:
	            result = geometry.hwtSmallMargin() + geometry.cartoucheLineWidth();
	            break;
	        case 2:
	        case 3:
	            result = geometry.hwtSmallMargin() + geometry.cartoucheLineWidth()
	                    + geometry.hwtSquareSize();
	            break;
	        default:
	            throw new RuntimeException("bad value for element " + element);
	        }
	        break;
	    case 'F':                
	    case 'f':
	        if (element != 0) {
	            result = geometry.hwtSmallMargin()
	                    + geometry.bastionDepth()
	                    + geometry.cartoucheLineWidth();
	        }
	        break;
	    }
	    return result;
	}

	/**
	 * length along the secondary axis of a cartouche from the cartouche
	 * external side to the text inside.
	 * @param jseshStyle the corresponding drawing specifications.
	 * @param type
	 * @return the length along the secondary axis of a cartouche from the
	 *         cartouche external side to the text inside.
	 */
	public static float computeCartoucheSecondaryLength(JSeshStyle jseshStyle, int type) {
	    float result = 0;
		GeometrySpecification geometry = jseshStyle.geometry();
	    switch (type) {
	    case 'c':
	    case 's':
	    case 'h':
	        result = geometry.cartoucheMargin() + geometry.cartoucheLineWidth();
	        break;
            case 'F':                	                    
	    case 'f':
	        result = geometry.cartoucheMargin() + geometry.cartoucheLineWidth()
	                + geometry.bastionDepth();
	        break;
	    }
	    return result;
	}
	
}
