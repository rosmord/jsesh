package jsesh.render.elements.internal.symboldrawers;

import java.awt.geom.GeneralPath;

/**
 * The combination of a path and a bounding path (often a bounding box).
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
