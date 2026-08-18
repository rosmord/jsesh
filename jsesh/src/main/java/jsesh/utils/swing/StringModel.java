/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 * 
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 *
 * Created on 29 oct. 2004
 */
package jsesh.utils.swing;

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