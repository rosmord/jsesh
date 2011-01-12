package jsesh.mdcDisplayer.viewToolkit.elements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Dimension2D;

import jsesh.mdcDisplayer.viewToolkit.elements.properties.ObjectProperty;
import jsesh.utils.DoubleDimensions;

public class RectangleElement extends GraphicalElement {

	private ObjectProperty<Color> lineColor = new ObjectProperty<Color>(
			"color", this, Color.BLACK, false);

	/**
	 * Should probably be geometric.
	 */
	private ObjectProperty<Integer> lineWidth = new ObjectProperty<Integer>(
			"lineWidth", this, 2, false);

	@Override
	protected void drawElement(Graphics2D g) {
		Color oldColor = g.getColor();
		Stroke oldStroke = g.getStroke();
		g.setColor(lineColor.getValue());
		g.setStroke(new BasicStroke(getLineWidth()));
		g.draw(getBounds());
		g.setColor(oldColor);
		g.setStroke(oldStroke);
	}

	@Override
	public Dimension2D getPreferredSize() {
		return new DoubleDimensions(0, 0);
	}

	public Color getLineColor() {
		return lineColor.getValue();
	}

	public void setLineColor(Color lineColor) {
		this.lineColor.setValue(lineColor);
	}

	public void setLineWidth(int lineWidth) {
		this.lineWidth.setValue(lineWidth);
	}

	public int getLineWidth() {
		return lineWidth.getValue();
	}

}
