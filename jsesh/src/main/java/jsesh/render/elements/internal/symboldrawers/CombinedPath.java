package jsesh.render.elements.internal.symboldrawers;

import java.awt.geom.Path2D;

/**
 * The combination of a path and a bounding path (often a bounding box).
 * @author rosmord
 *
 */
public class CombinedPath {
	boolean actualPathUsed;
	private Path2D.Double actualPath;
	private Path2D.Double boundingPath;
	
	public CombinedPath(Path2D.Double actualPath, Path2D.Double boundingPath) {
		super();
		this.actualPath = actualPath;
		this.boundingPath = boundingPath;
		actualPathUsed= (actualPath == boundingPath);
	}
	
	public CombinedPath(Path2D.Double actualPath) {
		super();
		this.actualPath = actualPath;
		this.boundingPath = actualPath;
		actualPathUsed= true;
	}
	
	
	public Path2D.Double getActualPath() {
		return actualPath;
	}
	public Path2D.Double getBoundingPath() {
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
