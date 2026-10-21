package org.qenherkhopeshef.viewtoolkit.demo.uml;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.qenherkhopeshef.viewtoolkit.demo.uml.UmlModel.ClassKind;
import org.qenherkhopeshef.viewtoolkit.demo.uml.UmlModel.UmlClass;
import org.qenherkhopeshef.viewtoolkit.drawing.element.Alignment;
import org.qenherkhopeshef.viewtoolkit.drawing.element.CompositeElement;
import org.qenherkhopeshef.viewtoolkit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewtoolkit.drawing.element.Margin;
import org.qenherkhopeshef.viewtoolkit.drawing.element.SpacerElement;
import org.qenherkhopeshef.viewtoolkit.drawing.element.TextElement;
import org.qenherkhopeshef.viewtoolkit.drawing.element.property.ObjectProperty;

/// The box representing a class: a vertical composite of three compartments
/// (name, attributes, operations), each one a vertical composite of texts.
///
/// The frame and the compartment separators are drawn by the box itself.
final class ClassBoxElement extends CompositeElement {

	private static final Font NAME_FONT = new Font("SansSerif", Font.BOLD, 13);
	private static final Font MEMBER_FONT = new Font("SansSerif", Font.PLAIN, 12);
	private static final Font STEREOTYPE_FONT = new Font("SansSerif", Font.PLAIN, 11);

	private final String className;

	private final ObjectProperty<Boolean> selected = new ObjectProperty<Boolean>("selected", this, false, false);

	ClassBoxElement(UmlClass umlClass) {
		this.className = umlClass.name;
		setMargin(Margin.NONE);
		setBackground(umlClass.kind == ClassKind.INTERFACE ? new Color(230, 240, 255) : new Color(255, 250, 222));

		CompositeElement header = compartment();
		if (umlClass.kind == ClassKind.INTERFACE || umlClass.kind == ClassKind.ENUM) {
			String stereotype = umlClass.kind == ClassKind.INTERFACE ? "«interface»" : "«enumeration»";
			header.addElement(centered(new TextElement(stereotype, STEREOTYPE_FONT)));
		}
		Font nameFont = umlClass.kind == ClassKind.ABSTRACT_CLASS || umlClass.kind == ClassKind.INTERFACE
				? NAME_FONT.deriveFont(Font.BOLD | Font.ITALIC)
				: NAME_FONT;
		header.addElement(centered(new TextElement(umlClass.name, nameFont)));
		addElement(pack(header));

		CompositeElement attributes = compartment();
		for (String a : umlClass.attributes)
			attributes.addElement(new TextElement(a, MEMBER_FONT));
		addElement(pack(attributes));

		CompositeElement operations = compartment();
		for (String o : umlClass.operations)
			operations.addElement(new TextElement(o, MEMBER_FONT));
		addElement(pack(operations));
		pack();
	}

	private static CompositeElement compartment() {
		CompositeElement c = new CompositeElement();
		c.setMargin(Margin.NONE);
		return c;
	}

	private static CompositeElement pack(CompositeElement compartment) {
		if (compartment.getNumberOfChildren() == 0)
			compartment.addElement(new SpacerElement(60, 8));
		compartment.pack();
		return compartment;
	}

	private static TextElement centered(TextElement text) {
		text.setAlignment(Alignment.CENTERED);
		return text;
	}

	String getClassName() {
		return className;
	}

	boolean isSelected() {
		return selected.getValue();
	}

	void setSelected(boolean value) {
		selected.setValue(value);
	}

	@Override
	public void drawElement(Graphics2D g) {
		super.drawElement(g);
		Rectangle2D b = getBounds();
		g.setColor(isSelected() ? new Color(30, 90, 220) : Color.DARK_GRAY);
		g.setStroke(new BasicStroke(isSelected() ? 2.5f : 1f));
		g.draw(new Rectangle2D.Double(0.5, 0.5, b.getWidth() - 1, b.getHeight() - 1));
		g.setStroke(new BasicStroke(1f));
		for (int i = 1; i < getNumberOfChildren(); i++) {
			GraphicalElement compartment = getChild(i);
			double y = compartment.getBounds().getMinY();
			g.draw(new Line2D.Double(0, y, b.getWidth(), y));
		}
	}

	@Override
	public String toString() {
		return "[class box " + className + "]";
	}
}
