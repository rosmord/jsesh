/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 * 
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.utils.swing;

/**
 * A couple key/displayed value for use in comboboxes.
 * This can be used for any graphical object which must display a string, and be selected from
 * a given list. It mirrors, more or less, the use of select and option in HTML.
 * @author rosmord
 *
 */
public class ListOption {
	private Object key="";
	private String displayedValue="";
	
	
	public ListOption(Object key, String displayedValue) {
		super();
		this.key = key;
		this.displayedValue = displayedValue;
	}


	
	public Object getKey() {
		return key;
	}



	public String getDisplayedValue() {
		return displayedValue;
	}



	public String toString() {
		return displayedValue;
	}
}
