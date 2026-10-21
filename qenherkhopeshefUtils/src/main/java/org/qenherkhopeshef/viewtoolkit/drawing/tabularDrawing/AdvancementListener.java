package org.qenherkhopeshef.viewtoolkit.drawing.tabularDrawing;

public interface AdvancementListener {

	/**
	 * Notify some degree of completion for the layout process.
	 * @param percent
	 */
	void notifyCompletion(int percent);

	/**
	 * Notify the end of the layout process.
	 */
	void notifyCompletion();

}
