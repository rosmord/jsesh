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
/// - [JGraphicalElementDisplayer] provides in a way implementation inheritance,
///    which is frowned upon. If you want something cleaner, you can write your components by
///    using a [GraphicalElementDisplayerDelegate] as an internal, hidden, implementation. You can use the code of 
///    as [JGraphicalElementDisplayer] as an example; it's quite likely that you don't need to make all methods public.
///
/// The toolkit knows nothing about the application's own data. Applications
/// usually add two pieces around it: view updater which builds the drawing from the
/// application's model and updates it when the model changes, and control code
/// code which turns mouse and keyboard events into operations on the model or
/// on the drawing.
///
/// ## Available drawings
///
/// We propose a number of base class for drawings, depending on its expected structure and number of elements.
/// It can be as simple as a list of graphical elements, or it can be more specific.
/// 
/// | Drawing | Use |
/// |---|---|
/// | [org.qenherkhopeshef.viewToolKit.drawing.PlainDrawing] | quick experiments, very small drawings |
/// | [org.qenherkhopeshef.viewToolKit.drawing.GridDrawing] | free placement, spatially indexed (many elements) |
/// | [org.qenherkhopeshef.viewToolKit.drawing.diagram.DiagramDrawing] | nodes and connectors which follow them |
/// | [org.qenherkhopeshef.viewToolKit.drawing.tabularDrawing.TabularDrawing] | elements in cells, with a very large number of columns |
/// | [org.qenherkhopeshef.viewToolKit.drawing.textOriented.TextDrawing] | wrapped text with embedded elements, of any length |
///
/// ## Demos
/// 
/// As a proof of concept, we provide a number of demos in [org.qenherkhopeshef.viewToolKit.demo].
/// See the demos in `org.qenherkhopeshef.viewToolKit.demo`.
package org.qenherkhopeshef.viewToolKit;
