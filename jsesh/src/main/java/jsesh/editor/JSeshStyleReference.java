package jsesh.editor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import jsesh.drawingspecifications.JSeshStyle;

/**
 * A reference to a JSeshStyle that can be observed for changes.
 * This class acts as an indirection layer, enabling components to share
 * and observe style settings in a decoupled manner.
 *
 * <p>
 * This class is suitable for both individual and shared settings.
 * It allows components to listen for changes to the referenced style.
 * </p>
 *
 * <p>
 * Since JSeshStyle is immutable, changes to this reference are
 * achieved by updating the reference to a new JSeshStyle instance.
 * </p>
 */
public final class JSeshStyleReference {

    /**
     * The current JSeshStyle referenced by this instance.
     */
    private JSeshStyle style;

    /**
     * Support for property change listeners.
     */
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * Constructs a new JSeshStyleReference with the given initial style.
     *
     * @param initialStyle the initial style to be referenced.
     */
    public JSeshStyleReference(JSeshStyle initialStyle) {
        this.style = initialStyle;
    }

    /**
     * Returns the current JSeshStyle referenced by this instance.
     *
     * @return the current style.
     */
    public JSeshStyle getStyle() {
        return style;
    }

    /**
     * Updates the referenced style to a new instance.
     * Notifies all registered listeners of the change.
     *
     * @param newStyle the new style to be referenced.
     */
    public void setStyle(JSeshStyle newStyle) {
        if (style == newStyle) {
            return; // No change, do nothing
        }

        JSeshStyle oldStyle = this.style;
        this.style = newStyle;

        // Notify listeners of the change
        pcs.firePropertyChange("style", oldStyle, newStyle);
    }

    /**
     * Adds a property change listener to this object.
     * 
     * <p> If the listener was already there, nothing will change.
     * @param listener the listener to be added.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        PropertyChangeListener[] listeners = pcs.getPropertyChangeListeners();
        // Check if the listener is already registered
        for (PropertyChangeListener l : listeners) {
            if (l == listener) {
                return; // Listener already registered, do nothing
            }
        }
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener from this object.
     *
     * @param listener the listener to be removed.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Fires a property change event to all registered listeners.
     * This method is called internally when the referenced style changes.
     *
     * @param propertyName the name of the property that changed.
     * @param oldStyle the old value of the style.
     * @param newStyle the new value of the style.
     */
    void fireStyleChanged(String propertyName, JSeshStyle oldStyle, JSeshStyle newStyle) {
        pcs.firePropertyChange(propertyName, oldStyle, newStyle);
    }
}
