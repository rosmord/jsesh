package jsesh.mdcDisplayer.preferences;

import java.awt.print.PageFormat;

public record PageSpecifications(
		float textWidth,
		float textHeight,
		float leftMargin,
		float rightMargin,
		PageFormat format // Maybe replace with something not java.awt related.		
		) {

}
