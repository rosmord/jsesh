package org.qenherkhopeshef.viewToolKit.demo.text;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.qenherkhopeshef.viewToolKit.JGraphicalElementDisplayer;
import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewToolKit.drawing.textOriented.Paragraph;
import org.qenherkhopeshef.viewToolKit.drawing.textOriented.TextDocument;
import org.qenherkhopeshef.viewToolKit.drawing.textOriented.TextDrawing;
import org.qenherkhopeshef.viewToolKit.drawing.textOriented.TextPosition;

/// Keyboard and mouse handling for the text editor demo.
///
/// The control translates user actions into operations on the document
/// (which the drawing follows) and into caret moves on the drawing.
final class TextEditorControl {

	private final JGraphicalElementDisplayer<TextDrawing> display;

	/// Horizontal position kept during vertical moves (null when not moving
	/// vertically).
	private Double verticalMoveX = null;

	TextEditorControl(JGraphicalElementDisplayer<TextDrawing> display) {
		this.display = display;
		display.setFocusable(true);
		display.setFocusTraversalKeysEnabled(false); // we want to receive tabs.
		display.addKeyListener(new Keys());
		Mouse mouse = new Mouse();
		display.addMouseListener(mouse);
		display.addMouseMotionListener(mouse);
		display.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				drawing().setCaretVisible(true);
			}

			@Override
			public void focusLost(FocusEvent e) {
				drawing().setCaretVisible(false);
			}
		});
	}

	private TextDrawing drawing() {
		return display.getDrawing();
	}

	private TextDocument document() {
		return drawing().getDocument();
	}

	// ------------------------------------------------------------------
	// Editing operations.

	/// Replaces the selection (if any) with a text.
	void insertText(String text) {
		deleteSelection();
		TextPosition end = document().insertText(drawing().getCaret(), text);
		moveTo(end, false);
	}

	/// Replaces the selection (if any) with a graphical element.
	void insertElement(GraphicalElement element) {
		deleteSelection();
		TextPosition end = document().insertElement(drawing().getCaret(), element);
		moveTo(end, false);
	}

	private boolean deleteSelection() {
		if (!drawing().hasSelection())
			return false;
		TextPosition start = drawing().getSelectionStart();
		document().delete(start, drawing().getSelectionEnd());
		moveTo(start, false);
		return true;
	}

	private void backspace() {
		if (!deleteSelection()) {
			TextPosition caret = drawing().getCaret();
			TextPosition previous = document().previous(caret);
			if (previous != null) {
				document().delete(previous, caret);
				moveTo(previous, false);
			}
		}
	}

	private void deleteForward() {
		if (!deleteSelection()) {
			TextPosition caret = drawing().getCaret();
			TextPosition next = document().next(caret);
			if (next != null) {
				document().delete(caret, next);
				moveTo(caret, false);
			}
		}
	}

	void copy() {
		if (drawing().hasSelection()) {
			String text = document().getText(drawing().getSelectionStart(), drawing().getSelectionEnd());
			clipboard().setContents(new StringSelection(text), null);
		}
	}

	void cut() {
		copy();
		deleteSelection();
	}

	void paste() {
		try {
			if (clipboard().isDataFlavorAvailable(DataFlavor.stringFlavor)) {
				insertText((String) clipboard().getData(DataFlavor.stringFlavor));
			}
		} catch (UnsupportedFlavorException | IOException | IllegalStateException e) {
			Toolkit.getDefaultToolkit().beep();
		}
	}

	void selectAll() {
		drawing().selectAll();
	}

	private static Clipboard clipboard() {
		return Toolkit.getDefaultToolkit().getSystemClipboard();
	}

	// ------------------------------------------------------------------
	// Moves.

	private void moveTo(TextPosition pos, boolean extend) {
		verticalMoveX = null;
		drawing().moveCaret(pos, extend);
	}

	private void moveVertically(boolean down, boolean extend) {
		TextDrawing drawing = drawing();
		if (verticalMoveX == null)
			verticalMoveX = drawing.getCaretRectangle(drawing.getCaret()).getX();
		TextPosition pos = down ? drawing.getPositionBelow(drawing.getCaret(), verticalMoveX)
				: drawing.getPositionAbove(drawing.getCaret(), verticalMoveX);
		drawing.moveCaret(pos, extend);
	}

	private void movePage(boolean down, boolean extend) {
		TextDrawing drawing = drawing();
		Rectangle2D caret = drawing.getCaretRectangle(drawing.getCaret());
		if (verticalMoveX == null)
			verticalMoveX = caret.getX();
		double page = display.getVisibleRect().getHeight() / display.getScale();
		double y = caret.getCenterY() + (down ? page : -page);
		// Scroll by a page too, so that the caret stays at the same place on screen.
		Rectangle visible = display.getVisibleRect();
		visible.translate(0, (int) Math.round((down ? page : -page) * display.getScale()));
		display.scrollRectToVisible(visible);
		drawing.moveCaret(drawing.getPositionAt(new Point2D.Double(verticalMoveX, y)), extend);
	}

	/// Selects the word around a position.
	private void selectWord(TextPosition pos) {
		Paragraph p = document().getParagraph(pos.paragraph());
		int start = pos.offset(), end = pos.offset();
		while (start > 0 && Character.isLetterOrDigit(p.charAt(start - 1)))
			start--;
		while (end < p.length() && Character.isLetterOrDigit(p.charAt(end)))
			end++;
		drawing().select(new TextPosition(pos.paragraph(), start), new TextPosition(pos.paragraph(), end));
	}

	private class Keys extends KeyAdapter {

		private boolean isMenuShortcut(KeyEvent e) {
			return (e.getModifiersEx() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) != 0;
		}

		@Override
		public void keyPressed(KeyEvent e) {
			TextDrawing drawing = drawing();
			boolean shift = e.isShiftDown();
			boolean menu = isMenuShortcut(e);
			TextPosition caret = drawing.getCaret();
			switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				if (!shift && drawing.hasSelection())
					moveTo(drawing.getSelectionStart(), false);
				else if (menu)
					moveTo(drawing.getLineStart(caret), shift);
				else if (document().previous(caret) != null)
					moveTo(document().previous(caret), shift);
				break;
			case KeyEvent.VK_RIGHT:
				if (!shift && drawing.hasSelection())
					moveTo(drawing.getSelectionEnd(), false);
				else if (menu)
					moveTo(drawing.getLineEnd(caret), shift);
				else if (document().next(caret) != null)
					moveTo(document().next(caret), shift);
				break;
			case KeyEvent.VK_UP:
				if (menu)
					moveTo(TextPosition.START, shift);
				else
					moveVertically(false, shift);
				break;
			case KeyEvent.VK_DOWN:
				if (menu)
					moveTo(document().getEnd(), shift);
				else
					moveVertically(true, shift);
				break;
			case KeyEvent.VK_HOME:
				moveTo(menu ? TextPosition.START : drawing.getLineStart(caret), shift);
				break;
			case KeyEvent.VK_END:
				moveTo(menu ? document().getEnd() : drawing.getLineEnd(caret), shift);
				break;
			case KeyEvent.VK_PAGE_UP:
				movePage(false, shift);
				break;
			case KeyEvent.VK_PAGE_DOWN:
				movePage(true, shift);
				break;
			case KeyEvent.VK_BACK_SPACE:
				backspace();
				break;
			case KeyEvent.VK_DELETE:
				deleteForward();
				break;
			case KeyEvent.VK_ENTER:
				insertText("\n");
				break;
			case KeyEvent.VK_TAB:
				insertText("    ");
				break;
			default:
				return; // not consumed.
			}
			e.consume();
		}

		@Override
		public void keyTyped(KeyEvent e) {
			char c = e.getKeyChar();
			if (isMenuShortcut(e) || e.isControlDown())
				return;
			if (c >= ' ' && c != KeyEvent.VK_DELETE && c != KeyEvent.CHAR_UNDEFINED) {
				insertText(String.valueOf(c));
				e.consume();
			}
		}
	}

	private class Mouse extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			display.requestFocusInWindow();
			TextPosition pos = drawing().getPositionAt(display.toDrawingPoint(e.getPoint()));
			if (e.getClickCount() == 2)
				selectWord(pos);
			else if (e.getClickCount() >= 3)
				drawing().select(new TextPosition(pos.paragraph(), 0),
						new TextPosition(pos.paragraph(), document().getParagraph(pos.paragraph()).length()));
			else
				moveTo(pos, e.isShiftDown());
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			// Scroll when dragging out of the visible area.
			display.scrollRectToVisible(new Rectangle(e.getX(), e.getY(), 1, 1));
			moveTo(drawing().getPositionAt(display.toDrawingPoint(e.getPoint())), true);
		}
	}
}
