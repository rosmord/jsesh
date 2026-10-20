package org.qenherkhopeshef.viewToolKit.demo.uml;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.qenherkhopeshef.viewToolKit.demo.uml.UmlModel.ClassKind;
import org.qenherkhopeshef.viewToolKit.demo.uml.UmlModel.UmlClass;
import org.qenherkhopeshef.viewToolKit.demo.uml.UmlModel.UmlRelation;
import org.qenherkhopeshef.viewToolKit.demo.uml.UmlParser.UmlParseException;
import org.qenherkhopeshef.viewToolKit.drawing.diagram.ConnectorEnd;
import org.qenherkhopeshef.viewToolKit.drawing.diagram.DiagramDrawing;

public class UmlParserTest {

    @Test
    public void classesAndMembers() throws UmlParseException {
        UmlModel model = UmlParser.parse("""
                abstract class Shape {
                  - origin : Point
                  + draw(g : Graphics)
                }
                interface Drawable
                """);
        UmlClass shape = model.getOrCreateClass("Shape");
        assertEquals(ClassKind.ABSTRACT_CLASS, shape.kind);
        assertEquals(1, shape.attributes.size());
        assertEquals(1, shape.operations.size());
        assertEquals(ClassKind.INTERFACE, model.getOrCreateClass("Drawable").kind);
    }

    @Test
    public void relations() throws UmlParseException {
        UmlModel model = UmlParser.parse("""
                Shape <|-- Circle
                Drawing "1" *-- "*" Shape : contains
                A ..|> B
                C --> D
                """);
        UmlRelation inheritance = model.getRelations().get(0);
        assertEquals(ConnectorEnd.HOLLOW_TRIANGLE, inheritance.fromEnd());
        assertEquals("Shape", inheritance.upper());
        UmlRelation composition = model.getRelations().get(1);
        assertEquals("1", composition.fromLabel());
        assertEquals("*", composition.toLabel());
        assertEquals("contains", composition.label());
        assertEquals(ConnectorEnd.FILLED_DIAMOND, composition.fromEnd());
        UmlRelation realization = model.getRelations().get(2);
        assertTrue(realization.dashed());
        assertEquals("B", realization.upper());
        assertEquals("C", model.getRelations().get(3).upper());
    }

    @Test
    public void errorsGiveTheLine() {
        UmlParseException e = assertThrows(UmlParseException.class,
                () -> UmlParser.parse("class A\nthis is not uml\n"));
        assertEquals(2, e.line);
    }

    @Test
    public void layoutPutsGeneralClassesAbove() throws UmlParseException {
        UmlModel model = UmlParser.parse("""
                A <|-- B
                B <|-- C
                C --> A
                """); // the last relation makes a cycle: it must be ignored.
        DiagramDrawing drawing = new DiagramDrawing();
        UmlDiagramPresenter presenter = new UmlDiagramPresenter(drawing);
        presenter.show(model);
        Map<String, Double> y = new HashMap<>();
        for (ClassBoxElement box : presenter.getBoxes())
            y.put(box.getClassName(), box.getBounds().getMinY());
        assertTrue(y.get("A") < y.get("B"));
        assertTrue(y.get("B") < y.get("C"));
        assertEquals(6, drawing.getElements().size()); // 3 boxes, 3 connectors.
    }
}
