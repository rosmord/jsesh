/// Definition of a drawing class for displaying text and similar stuff.
/// The goal is to have a class good enough for creating an desktop publishing software.
///
/// - [org.qenherkhopeshef.viewToolKit.drawing.textOriented.TextDocument] is the
///   model: paragraphs of text, in which any
///   [org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement] (a
///   picture, a small drawing...) can be embedded;
/// - [org.qenherkhopeshef.viewToolKit.drawing.textOriented.TextDrawing] is the
///   drawing: it lays out the paragraphs lazily, which allows it to display
///   very long texts, and manages a caret and a selection;
/// - [org.qenherkhopeshef.viewToolKit.drawing.textOriented.ParagraphElement] and
///   [org.qenherkhopeshef.viewToolKit.drawing.textOriented.TextLineElement] are
///   the graphical elements built for the paragraphs.
///
/// The keyboard and mouse handling is left to the application (see the
/// text editor in `org.qenherkhopeshef.viewToolKit.demo.text` for an example).
package org.qenherkhopeshef.viewToolKit.drawing.textOriented;
