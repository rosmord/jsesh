package jsesh.mdcDisplayer.drawingElements.symbolDrawers;

import java.awt.Graphics2D;

import jsesh.mdcDisplayer.drawingElements.ViewBox;

public interface SymbolDrawerDelegate {

	public abstract void draw(Graphics2D g2d, int angle, ViewBox viewBox, float strokeWidth);
	
	public float getBaseWidth();

}