# Swing, observers and memory leaks

The observer pattern can lead to nasty memory leaks if the listeners are not unregistered when the observable object is no longer needed. In the case of an application which opens multiple documents and closes them, the creation and deletion of multiple frames could possibly lead to problems.

For instance, in JSesh, almost everybody listens to the `HieroglyphShapeRepository` (the font repository). If an element registers a listener to the repository, and closed or deleted, the listener link will keep the element in memory, leading to a memory leak.

Some components have a **dispose()** method. However, this is limited to components which extend `Window` (JFrame, JDialog, etc.). For other components, one must redefine `addNotify()` and `removeNotify`


## Windows

For windows, one can :

- add listeners in the constructor;
- remove listeners in the `dispose()` method.

## Other components

For other components:

- add listeners in `addNotify()`;
- remove listeners in `removeNotify()`.

If a listener must be added afterwards (for instance, when calling something like `setModel()`), as `addNotify`  has probably already been called, the idea is :

- to unregister the listener to the previous observable (if any);
- to register a listener to the new observable **if the component is displayable** (check with `isDisplayable()`). If not, if the component is displayed later, `addNotify()` will be called, and the listener will be registered at that time.

Example:

~~~java
public class JMDCEditor extends JPanel {
    
    private PropertyChangeListener styleChangeListener = evt -> invalidateView();



    public void setStyleReference(JSeshStyleReference styleReference) {
        Objects.requireNonNull(styleReference);
        // If the operation doesn't change anything, do nothing.
        if (this.styleReference == styleReference) {
            return;
        }
        // Now, remove the previous configuration and install the new one.
        if (this.styleReference != null) {
            this.styleReference.removePropertyChangeListener(styleChangeListener);
        }
        this.styleReference = styleReference;
        // Don't register a listener if the component can't be displayed.
        if (isDisplayable()) {
            this.styleReference.addPropertyChangeListener(styleChangeListener);
        }
        invalidateView();
    }


@Override
    public void addNotify() {
        super.addNotify();
        // Re-register listeners
        if (styleReference != null) {
            styleReference.addPropertyChangeListener(styleChangeListener);
        }
        if (hieroglyphShapeRepository != null) {
            hieroglyphShapeRepository.addListener(shapeRepositoryListener);
        }
    }

    @Override
    public void removeNotify() {
        if (styleReference != null) {
            styleReference.removePropertyChangeListener(styleChangeListener);
        }
        if (hieroglyphShapeRepository != null) {
            hieroglyphShapeRepository.removeListener(shapeRepositoryListener);
        }
        super.removeNotify();
    }
    ...
}
~~~

In this example, the `setStyleReference()` registers a listener **only** if the component is displayable. If it is not:
- while it can't be displayed, it doesn't matter anyway;
- when it becomes displayable, `addNotify()` is called, and the listener is registered.

We have designed the various methods to be idempotent, so calling them (with the same parameters) multiple times will have the same effect as calling them once.

