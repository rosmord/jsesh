package org.qenherkhopeshef.viewToolKit.drawing.element;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Dimension2D;

import org.qenherkhopeshef.swingUtils.DoubleDimensions;
import org.qenherkhopeshef.viewToolKit.drawing.element.property.ObjectProperty;

/**
 * A filled shape element.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
public class ShapeElement extends GraphicalElement {

	private ObjectProperty<Color> color = new ObjectProperty<Color>(
			"color", this, Color.BLACK, false);

	private ObjectProperty<Shape> shape;
	
	public ShapeElement(Shape shape) {
		this.shape= new ObjectProperty<Shape>("shape", this, shape, true);
	}

	@Override
	protected void drawElement(Graphics2D g) {
		Color oldColor = g.getColor();
		Stroke oldStroke = g.getStroke();
		g.setColor(color.getValue());
		g.fill(shape.getValue());
		g.setColor(oldColor);
		g.setStroke(oldStroke);
	}

	@Override
	public Dimension2D getPreferredSize() {
		return new DoubleDimensions(0, 0);
	}

	public Color getColor() {
		return color.getValue();
	}

	public void setColor(Color lineColor) {
		this.color.setValue(lineColor);
	}

}
