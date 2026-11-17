/*
 * Created on 18 févr. 2003
 *
 */
package jsesh.mdcDisplayer.mdcView;

/**
 * represents the margins around an object. 
 *  * @author rosmord
 */
public class Margins {
	private float top, start, end, bottom;
	public Margins(float top, float start, float end, float bottom) {
		this.top= top;
		this.start= start;
		this.end= end;
		this.bottom= bottom;
	}
	
	/**
	 * @return float
	 */
	public float getBottom() {
		return bottom;
	}

	/**
	 * @return float
	 */
	public float getEnd() {
		return end;
	}

	/**
	 * @return float
	 */
	public float getStart() {
		return start;
	}

	/**
	 * @return float
	 */
	public float getTop() {
		return top;
	}

	/**
	 * Sets the bottom.
	 * @param bottom The bottom to set
	 */
	public void setBottom(float bottom) {
		this.bottom= bottom;
	}

	/**
	 * Sets the end.
	 * @param end The end to set
	 */
	public void setEnd(float end) {
		this.end= end;
	}

	/**
	 * Sets the start.
	 * @param start The start to set
	 */
	public void setStart(float start) {
		this.start= start;
	}

	/**
	 * Sets the top.
	 * @param top The top to set
	 */
	public void setTop(float top) {
		this.top= top;
	}

}
