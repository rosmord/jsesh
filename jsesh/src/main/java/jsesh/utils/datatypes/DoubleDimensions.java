/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 * 
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 *
 * Created on 29 mai 2004
 */
package jsesh.utils.datatypes;

import java.awt.geom.Dimension2D;

/**
 * DoubleDimensions
 * @author rosmord
 *
 *(a similar class also exists in jvectClipboard. But this introduces a quite artificial dependency between the two packages.
 * As basically, those objects are simple implementations of Dimension2D (and should be part of the standard java library in the first place),
 * this is not a huge issue.
 *This code is published under the GNU LGPL.
 */
public class DoubleDimensions extends Dimension2D {

	private double height;
	private double width;
	
	public DoubleDimensions(double width, double height) {
		setSize(width, height);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.geom.Dimension2D#setSize(double, double)
	 */
	public void setSize(double width, double height) {
		this.width= width;
		this.height= height;
	}
	

	/**
	 * @return the height
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * @return the width
	 */
	public double getWidth() {
		return width;
	}

	public String toString() {
		return "("+ width + ", "+ height+ ")";
	}
}
