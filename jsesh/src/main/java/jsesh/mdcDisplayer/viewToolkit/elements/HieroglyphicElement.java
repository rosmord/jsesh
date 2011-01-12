package jsesh.mdcDisplayer.viewToolkit.elements;

import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

import jsesh.mdc.MDCSyntaxError;
import jsesh.mdcDisplayer.draw.MDCDrawingFacade;
import jsesh.mdcDisplayer.viewToolkit.elements.properties.ObjectProperty;
import jsesh.utils.DoubleDimensions;

public class HieroglyphicElement extends GraphicalElement {
	
	ObjectProperty<String> mdcProperty;

	public HieroglyphicElement(String mdc) {
		super();
		this.mdcProperty = new ObjectProperty<String>("mdc",this,mdc, true);
	}

	@Override
	public void drawElement(Graphics2D g) {
		MDCDrawingFacade facade= getDrawingFacade();
		try {
			facade.draw(mdcProperty.getValue(), g, 0f,0f);
		} catch (MDCSyntaxError e) {
			// Ignore error and continue.
			e.printStackTrace();
		}
	}
	
	@Override
	public Dimension2D getPreferredSize() {
		Rectangle2D textBounds;
		try {
			textBounds = getDrawingFacade().getBounds(mdcProperty.getValue(), 0, 0);
		} catch (MDCSyntaxError e) {
			textBounds= new Rectangle2D.Double();
			e.printStackTrace();
			// Ignore error and continues drawing.
		}
		return new DoubleDimensions(textBounds.getWidth(), textBounds.getHeight());
	}
	
	private MDCDrawingFacade getDrawingFacade() {
		return new MDCDrawingFacade();
	}
	
	public void setMdc(String mdc) {
		this.mdcProperty.setValue(mdc);
	}
	
	public String getMdc() {
		return mdcProperty.getValue();
	}
	
	@Override
	public String toString() {
		return "[mdc "+ mdcProperty + "]";
	}
}
