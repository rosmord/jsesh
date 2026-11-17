/*
 * Created on 5 juil. 2005 by rosmord
 *
 * TODO document the file ExportData.java
 * 
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package jsesh.graphicExport;

import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;
import jsesh.mdcDisplayer.secondaryData.MDCCaret;

/**
 * Data needed for text exportation.
 * 
 * Note that there is currently a small logical bug. It should be possible to state that we are not interested in a range of text.
 * @author rosmord
 */
public class ExportData {
	private DrawingSpecifications drawingSpecifications;

	private TopItemList topItemList;

	private MDCPosition start;

	private MDCPosition end;

	private double scale;

	/**
	 * @param drawingSpecifications
	 * @param caretProvider
	 * @param data
	 */
	public ExportData(DrawingSpecifications drawingSpecifications,
			MDCPosition start, MDCPosition end, TopItemList data, double scale) {
		super();
		init(drawingSpecifications, start, end, data, scale);

	}

	/**
	 * @param drawingSpecifications
	 * @param caretProvider
	 * @param data
	 */
	public ExportData(DrawingSpecifications drawingSpecifications,
			MDCCaret caret, TopItemList data, double scale) {	
		MDCPosition start;
		MDCPosition end;
		if (caret.getMark() == null) {
			start= new MDCPosition(data,0);
			end= new MDCPosition(data, data.getNumberOfChildren());
		} else {
			start= caret.getInsert().getPosition();
			end= caret.getMark().getPosition();
		}
		init(drawingSpecifications, start, end, data, scale);
	}
	
	private void init(DrawingSpecifications drawingSpecifications, MDCPosition start, MDCPosition end, TopItemList data, double scale) {
		this.drawingSpecifications = drawingSpecifications;
		this.topItemList = data;
		this.scale = scale;

		if (start.compareTo(end) > 0) {
			this.start = end;
			this.end = start;
		} else {
			this.start = start;
			this.end = end;
		}
	}



	public TopItemList getTopItemList() {
		return topItemList;
	}

	public DrawingSpecifications getDrawingSpecifications() {
		return drawingSpecifications;
	}

	/**
	 * @return Returns the scale.
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * @param scale
	 *            The scale to set.
	 */
	public void setScale(double scale) {
		this.scale = scale;
	}

	public MDCPosition getEnd() {
		return end;
	}

	public MDCPosition getStart() {
		return start;
	}

}
