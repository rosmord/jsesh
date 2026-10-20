/// A small framework for displaying (and editing) structured drawings in Swing.
///
/// ## Concepts
///
/// - A [org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement] is
///   something which can be drawn: a text, a shape, a line, a picture, or a
///   composite of other elements. Its properties fire events when they change.
/// - A [org.qenherkhopeshef.viewToolKit.drawing.Drawing] is a collection of
///   elements, which knows how to find the elements in a given zone quickly.
///   It *manages* its elements: it is notified when they change, updates its
///   own structures, and passes the events to its listeners.
/// - [org.qenherkhopeshef.viewToolKit.JGraphicalElementDisplayer] displays a
///   drawing, asking it only for the elements in the visible area. It also
///   draws the drawing's *decorations* (cursors, selection handles...),
///   which are not part of the drawing itself.
///
/// Applications usually add two objects around this: a *presenter*, which
/// keeps the drawing in sync with the application's model, and a *control*,
/// which turns mouse and keyboard events into operations.
///
/// ## Available drawings
///
/// | Drawing | Use |
/// |---|---|
/// | [org.qenherkhopeshef.viewToolKit.drawing.PlainDrawing] | quick experiments, very small drawings |
/// | [org.qenherkhopeshef.viewToolKit.drawing.GridDrawing] | free placement, spatially indexed (many elements) |
/// | [org.qenherkhopeshef.viewToolKit.drawing.diagram.DiagramDrawing] | nodes and connectors which follow them |
/// | [org.qenherkhopeshef.viewToolKit.drawing.tabularDrawing.TabularDrawing] | elements in cells, with a very large number of columns |
/// | [org.qenherkhopeshef.viewToolKit.drawing.textOriented.TextDrawing] | wrapped text with embedded elements, of any length |
///
/// See the demos in `org.qenherkhopeshef.viewToolKit.demo`.
package org.qenherkhopeshef.viewToolKit;
