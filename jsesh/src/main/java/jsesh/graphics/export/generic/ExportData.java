/*
 * Created on 5 juil. 2005 by rosmord
 *
 * TODO document the file ExportData.java
 * 
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package jsesh.graphics.export.generic;


import jsesh.editor.caret.MDCCaret;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.context.JSeshRenderContext;

/**
 * Data needed for text exportation.
 * 
 * Note that there is currently a small logical bug. It should be possible to state that we are not interested in a range of text.
 * @author rosmord
 */
public class ExportData {
	private JSeshRenderContext renderContext;

	private TopItemList topItemList;

	private MDCPosition start;

	private MDCPosition end;

	private double scale;

	/**
	 * Select export between two positions.
	 * @param renderContext the drawing specifications.
	 * @param start beginning of the zone to export in data
	 * @param end  end of the zone to export in data
	 * @param data data to export (partially)
	 * @param scale scaling information.
	 */
	public ExportData(JSeshRenderContext renderContext,
			MDCPosition start, MDCPosition end, TopItemList data, double scale) {
		super();
		init(renderContext, start, end, data, scale);

	}

	/**
	 * Data needed to draw a text
	 * @param drawingSpecifications
	 * @param caret
	 * @param text the text, as a list of cadrats.
	 * @param scale the export scale. scale 1 means that a cadrat is 18 point high.
	 */
	public ExportData(JSeshRenderContext renderContext,
			MDCCaret caret, TopItemList text, double scale) {	
		MDCPosition start;
		MDCPosition end;
		if (caret.getMark() == null) {
			start= new MDCPosition(text,0);
			end= new MDCPosition(text, text.getNumberOfChildren());
		} else {
			start= caret.getInsert().getPosition();
			end= caret.getMark().getPosition();
		}
		init(renderContext, start, end, text, scale);
	}
	
	private void init(JSeshRenderContext renderContext, MDCPosition start, MDCPosition end, TopItemList data, double scale) {
		this.renderContext = renderContext;
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


	public JSeshRenderContext getRenderContext() {
		return renderContext;
	}

	public TopItemList getTopItemList() {
		return topItemList;
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

	/**
	 * Returns a MDC representation of exported text.
	 * Used mainly when creating comments included in output files.
	 * @return
	 */
	public String getExportedMdC() {
		return topItemList.toMdC(start.getIndex(), end.getIndex());
	}

}
