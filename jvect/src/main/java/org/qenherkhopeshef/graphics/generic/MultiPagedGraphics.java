package org.qenherkhopeshef.graphics.generic;

import java.awt.Graphics2D;
// TODO : use this interface instead of putting openPage in BaseGraphics2D.
public interface MultiPagedGraphics {
	void openPage();
	Graphics2D getGraphics();
	void closePage();	
}
