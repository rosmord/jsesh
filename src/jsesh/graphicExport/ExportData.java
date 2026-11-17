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
import jsesh.mdcDisplayer.draw.MDCCaret;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;

/**
 * Data needed for text exportation.
 * 
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

	/**
	 * @param drawingSpecifications
	 * @param caretProvider
	 * @param data
	 */
	public ExportData(DrawingSpecifications drawingSpecifications,
			MDCCaret caret, TopItemList data, double scale) {
		this(drawingSpecifications, caret.getInsert().getPosition(), caret
				.getMark().getPosition(), data, scale);
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
