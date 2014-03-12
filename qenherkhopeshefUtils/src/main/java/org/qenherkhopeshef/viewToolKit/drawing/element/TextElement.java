package org.qenherkhopeshef.viewToolKit.drawing.element;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Dimension2D;

import org.qenherkhopeshef.swingUtils.DoubleDimensions;
import org.qenherkhopeshef.viewToolKit.drawing.element.property.ObjectProperty;

public class TextElement extends GraphicalElement {

	private final ObjectProperty<String> textProperty;

	private Font font = new Font("SansSerif", Font.PLAIN, 12);

	/**
	 * Inner margin (equivalent of HTML padding). TODO : move this property up;
	 * it should be in Graphical Element already.
	 */
	private final ObjectProperty<Margin> innerMarginProperty = new ObjectProperty<Margin>(
			"margins", this, new Margin(3), true);

	private Dimension2D preferredSize;

	public TextElement(String text) {
		textProperty = new ObjectProperty<String>("text", this, text, true);
	}

	/**
	 * Constructeur qui permet de préciser la font voulue.
	 * 
	 * @param text
	 * @param otherFont
	 */
	public TextElement(String text, Font otherFont) {
		this.font = otherFont;
		textProperty = new ObjectProperty<String>("text", this, text, true);
	}

	@Override
	protected void drawElement(Graphics2D g) {
		if ("".equals(textProperty.getValue()))
			return;
		g.translate(getInnerMargin().getLeft(), getInnerMargin().getTop());
		TextLayout layout = buildLayout();
		double y = layout.getAscent();
		layout.draw(g, 0, (float) y);
	}

	@Override
	public Dimension2D getPreferredSize() {
		if ("".equals(textProperty.getValue()))
			return new DoubleDimensions(0, 0);

		if (preferredSize == null) {
			TextLayout layout = buildLayout();
			preferredSize = new DoubleDimensions(layout.getAdvance()
					+ getInnerMargin().getTotalMarginWidth(), layout
					.getBounds().getHeight()
					+ getInnerMargin().getTotalMarginHeight());
		}
		return preferredSize;
	}

	private TextLayout buildLayout() {
		FontRenderContext fontRenderContext = new FontRenderContext(null, true,
				false);
		return new TextLayout(getText(), font, fontRenderContext);
	}

	/**
	 * Sets the inner margin.
	 * 
	 * @see TextElement#getInnerMargin()
	 * @param margins
	 */
	public void setInnerMargin(Margin margins) {
		preferredSize = null;
		innerMarginProperty.setValue(margins);
	}

	/**
	 * Gets the inner margin.
	 * <p>
	 * This corresponds to HTML padding, i.e. a space between the element border
	 * and the actual drawing of the element content.
	 * 
	 * <p> TODO move this property up to GraphicalElement, where it belongs.
	 * @return the inner margin (padding).
	 */
	public Margin getInnerMargin() {
		return innerMarginProperty.getValue();
	}

	public void setText(String text) {
		preferredSize = null;
		textProperty.setValue(text);
	}

	public String getText() {
		return textProperty.getValue();
	}

	@Override
	public String toString() {
		return "[text " + textProperty + "]";
	}
}
