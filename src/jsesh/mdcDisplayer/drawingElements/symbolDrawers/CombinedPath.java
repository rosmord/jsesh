package jsesh.mdcDisplayer.drawingElements.symbolDrawers;

import java.awt.geom.GeneralPath;

/**
 * A detailled (or partial) path which comes along a simpler (or fuller) path used for computing bounding boxes. 
 * @author rosmord
 *
 */
public class CombinedPath {
	boolean actualPathUsed;
	private GeneralPath actualPath;
	private GeneralPath boundingPath;
	
	public CombinedPath(GeneralPath actualPath, GeneralPath boundingPath) {
		super();
		this.actualPath = actualPath;
		this.boundingPath = boundingPath;
		actualPathUsed= (actualPath == boundingPath);
	}
	
	public CombinedPath(GeneralPath actualPath) {
		super();
		this.actualPath = actualPath;
		this.boundingPath = actualPath;
		actualPathUsed= true;
	}
	
	
	public GeneralPath getActualPath() {
		return actualPath;
	}
	public GeneralPath getBoundingPath() {
		return boundingPath;
	}
	
	/**
	 * Is the actual path used to compute bounding box ?
	 * @return
	 */
	public boolean isActualPathUsed() {
		return actualPathUsed;
	}
}
