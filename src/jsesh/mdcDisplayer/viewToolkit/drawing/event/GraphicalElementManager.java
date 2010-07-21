package jsesh.mdcDisplayer.viewToolkit.drawing.event;


/**
 * Interface for an object which contains graphical elements and is notified when they change.
 * This is, in a way, the "inside" interface of drawings.
 * 
 * Drawing show a certain interface to the outside world, but they 
 * need to get information from the elements they contain.
 * 
 * This is it.
 * 
 * Normally, abstract drawing provides a GraphicalElementManager 
 * which does the job and can be passed to GraphicalElements.
 * @author rosmord
 *
 */
public interface GraphicalElementManager {
	void eventOccurred(DrawingEvent ev);
}
