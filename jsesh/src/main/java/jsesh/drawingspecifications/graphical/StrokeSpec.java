package jsesh.drawingspecifications.graphical;

import java.awt.Stroke;
import java.awt.BasicStroke;


/**
 * Specification for rendering strokes.
 * 
 * 
 * @param fineLineWidth
 * @param wideLineWidth
 * @param shadingStyle
 */
public record StrokeSpec(
		float fineLineWidth,
		float wideLineWidth,
		ShadingStyle shadingStyle
		) {


	public static StrokeSpec DEFAULT = new StrokeSpec(0.5f, 3.0f, ShadingStyle.GRAY_SHADING);	
	
	public Stroke getFineStroke() {
		return new BasicStroke(fineLineWidth);
	}

	public Stroke getWideStroke() {
		return new BasicStroke(wideLineWidth);
	}

	/**
	 * Copy builder.
	 */


	public Builder copy() {
		return new Builder(this);
	}

	/** copy builder class */
	public static class Builder {
		private float fineLineWidth;
		private float wideLineWidth;
		private ShadingStyle shadingStyle;

		public Builder(StrokeSpec original) {
			this.fineLineWidth= original.fineLineWidth;
			this.wideLineWidth= original.wideLineWidth;
			this.shadingStyle= original.shadingStyle;
		}	

		public Builder fineLineWidth(float fineLineWidth) {
			this.fineLineWidth = fineLineWidth;
			return this;
		}

		public Builder wideLineWidth(float wideLineWidth) {
			this.wideLineWidth = wideLineWidth;
			return this;
		}
		public Builder shadingStyle(ShadingStyle shadingStyle) {
			this.shadingStyle = shadingStyle;
			return this;
		}
		public StrokeSpec build() {
			return new StrokeSpec(fineLineWidth, wideLineWidth, shadingStyle);
		}
	}
	
}
