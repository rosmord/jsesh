package org.qenherkhopeshef.guiFramework.splash;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.qenherkhopeshef.swingUtils.GraphicsUtils;

public class SplashMessageText {

    private int x, y;
    private String text;
    private Font font;
    private Color color = Color.BLACK;

    public SplashMessageText(int x, int y, String text) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.font = new Font("Dialog", Font.BOLD, 24);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

	public void paint(Graphics2D g2d) {
		Color oldColor= g2d.getColor();
		Font oldFont= g2d.getFont();
		g2d.setColor(color);
		g2d.setFont(font);
		g2d.drawString(text, x, y);
		g2d.setFont(oldFont);
		g2d.setColor(oldColor);
	}
}