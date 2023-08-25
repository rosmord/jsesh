package org.qenherkhopeshef.properties.swing.adapters;

import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Segment;

import org.qenherkhopeshef.properties.base.ObjectProperty;

/**
 * An adapter from a property to a <em>read-only</em> text document which can be displayed in a text field.
 * @param <T>
 */
public class ReadonlyTextModelAdapter<T> implements Document {
	
	private ObjectProperty<T> property;
	private ArrayList<DocumentListener> listeners = new ArrayList<>();

	@Override
	public int getLength() {
		return toString().length();
	}

	@Override
	public void addDocumentListener(DocumentListener listener) {
		listeners.add(listener);		
	}

	@Override
	public void removeDocumentListener(DocumentListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void addUndoableEditListener(UndoableEditListener listener) {
		// Does nothing, document is not editable.
	}

	@Override
	public void removeUndoableEditListener(UndoableEditListener listener) {
		// Does nothing, document is not editable.		
	}

	@Override
	public Object getProperty(Object key) {
		return null;
	}

	@Override
	public void putProperty(Object key, Object value) {		
	}

	@Override
	public void remove(int offs, int len) throws BadLocationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getText(int offset, int length) throws BadLocationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getText(int offset, int length, Segment txt) throws BadLocationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Position getStartPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Position getEndPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Position createPosition(int offs) throws BadLocationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element[] getRootElements() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element getDefaultRootElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void render(Runnable r) {
		SwingUtilities.invokeLater(r);		
	}
	
	@Override
	public String toString() {
		if (property.getValue() == null)
			return "null";
		else
			return property.getValue().toString();
	}
}
