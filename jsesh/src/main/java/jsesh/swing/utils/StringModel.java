/*
 * Created on 29 oct. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.swing.utils;

import java.util.Observer;

/**
 * @author S. Rosmorduc
 *
 */
public interface StringModel {
	public abstract String getText();

	/**
	 * Adds an observer to this model.
	 * Can be easily implemented by extending Observable. 
	 * @param obs
	 * 
	 */
	public abstract void addObserver(Observer obs);
}