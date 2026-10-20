/// Demo applications for the viewToolKit.
///
/// - [org.qenherkhopeshef.viewToolKit.demo.text.TextEditorDemo]: a text editor
///   with embedded pictures, able to handle very long texts (uses
///   `TextDrawing`);
/// - [org.qenherkhopeshef.viewToolKit.demo.picture.PictureDrawerDemo]: a
///   drawer for open and closed lines (uses `GridDrawing` and
///   `PolylineElement`);
/// - [org.qenherkhopeshef.viewToolKit.demo.uml.UmlDiagramDemo]: a UML class
///   diagram displayer (uses `DiagramDrawing`, `CompositeElement` and
///   `ConnectorElement`).
///
/// [org.qenherkhopeshef.viewToolKit.demo.ViewToolKitDemos] launches any of them.
///
/// All three follow the same organisation: a model, a drawing which displays
/// it in a `JGraphicalElementDisplayer`, and a control object which turns
/// mouse and keyboard events into operations on the model or the drawing.
package org.qenherkhopeshef.viewToolKit.demo;
