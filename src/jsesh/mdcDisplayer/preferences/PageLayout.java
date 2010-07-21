package jsesh.mdcDisplayer.preferences;

import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;


/**
 * Description of a typical page layout.
 * Includes the page size, and its margins.
 * The page size can be null, in which case the system
 * is expected to take the text's natural dimensions. 
 * 
 * <p> Units are expressed in points.
 * @author rosmord
 *
 */
public final class PageLayout implements Cloneable {
	private PageFormat pageFormat= null;
	private float textWidth; // In fact, text width ?
	private float textHeight; // In fact, text height.
	private float leftMargin;
	private float topMargin;
	
	
	public  Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// Should not happen.
			throw new RuntimeException(e);
		}
	}
	/**
	 * Get The expected size for a page. If null, the size of the generated
	 * pictures will depend on the contents of the pages. if not, the text
	 * rendering will accomodate the page sizes.
	 * 
	 * @return Returns the pageFormat.
	 */
	public PageFormat getPageFormat() {
		return pageFormat;
	}
	
	/**
	 * @param pageFormat
	 *            The pageFormat to set.
	 */
	public void setPageFormat(PageFormat pageFormat) {
		this.pageFormat = pageFormat;
	}
	
	/**
	 * @param f
	 */
	//void setPageWidth(float f)
	public void setTextWidth(float pageWidth) {
		this.textWidth = pageWidth;
	}
	
	public float getTextWidth() {
		return textWidth;
	}


	public float getLeftMargin() {
		return leftMargin;
	}
	
	public void setLeftMargin(float leftMargin) {
		this.leftMargin = leftMargin;
	}
	
	public float getTopMargin() {
		return topMargin;
	}
	
	public void setTopMargin(float topMargin) {
		this.topMargin = topMargin;
	}
	public float getTextHeight() {
		return textHeight;
	}
	public void setTextHeight(float textHeight) {
		this.textHeight = textHeight;
	}
	
	public boolean hasPageFormat() {
		return pageFormat != null;
	}
	
	/**
	 * Returns the area where the main text content is supposed to be written.
	 * @return
	 */
	public Rectangle2D getDrawingRectangle() {
		if (hasPageFormat()) {
			Rectangle2D rect= new Rectangle2D.Float(leftMargin, topMargin, textWidth, textHeight);
			return rect;
		} else {
			throw new NullPointerException("No drawing rectangle available");
		}
	}
	
}
