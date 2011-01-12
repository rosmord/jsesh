package jsesh.mdcDisplayer.viewToolkit.elements;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Dimension2D;

import jsesh.mdcDisplayer.viewToolkit.elements.properties.ObjectProperty;
import jsesh.utils.DoubleDimensions;

public class TextElement extends GraphicalElement {

	private ObjectProperty<String> textProperty;

	private Font font= new Font("SansSerif", Font.PLAIN, 12);
	
	private ObjectProperty<SideDimensions> marginsProperty;
	
	private Dimension2D preferredSize;
	
	public TextElement(String text) {
		textProperty= new ObjectProperty<String>("text", this,text, true);
		marginsProperty= new ObjectProperty<SideDimensions>("margins",this, new SideDimensions(3), true);
	}

	@Override
	protected void drawElement(Graphics2D g) {
		if ("".equals(textProperty.getValue())) return;
		g.translate(getMargins().getLeft(), getMargins().getTop());
		TextLayout layout = buildLayout();
		double y=  layout.getAscent();
		layout.draw(g, 0, (float)y);
	}

	@Override
	public Dimension2D getPreferredSize() {
		if ("".equals(textProperty.getValue())) return new DoubleDimensions(0, 0);

		if (preferredSize==null) {
			TextLayout layout = buildLayout();
			preferredSize = new DoubleDimensions(layout.getAdvance()
					+ getMargins().getWidth(), layout.getBounds().getHeight()
					+ getMargins().getHeight());
		}
		return preferredSize;
	}

	private TextLayout buildLayout() {
		FontRenderContext fontRenderContext= new FontRenderContext(null,true,false);
		return new TextLayout(getText(), font, fontRenderContext);
	}
	
	public void setMargins(SideDimensions margins) {
		preferredSize= null;
		marginsProperty.setValue(margins);
	}
	
	public SideDimensions getMargins() {
		return marginsProperty.getValue();
	}
	
	public void setText(String text) {
		preferredSize= null;
		textProperty.setValue(text);	
	}
	
	public String getText() {
		return textProperty.getValue();
	}
	
	@Override
	public String toString() {
		return "[text "+ textProperty+ "]";
	}
}

      