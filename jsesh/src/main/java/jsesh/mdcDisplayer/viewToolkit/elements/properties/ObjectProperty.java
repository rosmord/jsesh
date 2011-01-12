package jsesh.mdcDisplayer.viewToolkit.elements.properties;

import jsesh.mdcDisplayer.viewToolkit.drawing.event.PropertyChangedEvent;
import jsesh.mdcDisplayer.viewToolkit.elements.GraphicalElement;

/**
 * A property whose value is an object. Note that the object should have "value"
 * semantics and be immutable. The ObjectProperty only responds to "setValue".
 * The value *must* not be null at the moment..
 * 
 * @author rosmord
 * 
 * @param <T>
 */
public class ObjectProperty<T> extends Property {
	GraphicalElement graphicalElement;
	T value;
	
	public ObjectProperty(String name,GraphicalElement graphicalElement, T value, boolean geometric) {
		super(name, geometric);
		this.graphicalElement= graphicalElement;
		testNotNull(value);
		this.value = value;
	}

	private void testNotNull(T value) {
		if (value == null)
			throw new NullPointerException();
	}

	public void setValue(T newValue) {
		testNotNull(newValue);
		if (! newValue.equals(value)) {
			this.value = newValue;
			graphicalElement.fireDrawingEvent(new PropertyChangedEvent(graphicalElement,this));
			
			//graphicalElement.fireDrawingEvent(new DrawingEvent(graphicalElement));
		}
	}

	public T getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return getName() + ": "+ getValue();
	}
}
