package jsesh.render.elements.internal.symboldrawers;

import java.awt.Graphics2D;

import jsesh.render.view.ViewBox;

public interface SymbolDrawerDelegate {

	public abstract void draw(Graphics2D g2d, int angle, ViewBox viewBox, float strokeWidth);
	
	public float getBaseWidth();

}