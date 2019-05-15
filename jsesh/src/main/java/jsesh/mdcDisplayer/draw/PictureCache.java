/*
 * Created on 26 oct. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.draw;

import java.awt.image.BufferedImage;
import java.util.TreeMap;

import jsesh.mdc.model.ModelElement;

/**
 * A cache for bitmaps of hieroglyphic groups. This class is quite technical
 * (hence, it's not public). the cache associates MDCViews and bitmaps, to
 * speedup redrawing.
 * 
 * As bitmaps are huge, especially in java, we limit the number of cached
 * elements.
 * 
 * A possible problem is that if the cache is too small, as bitmaps are reused and <b>not drawn immediately</b>, we might share a bitmap between different groups,
 * which is not a good prospect.
 * @author S. Rosmorduc
 *  
 */
class PictureCache {
	static class Element {
		ModelElement model;

		BufferedImage image;

		/**
		 * @param model
		 * @param image
		 */
		public Element(ModelElement model, BufferedImage image) {
			super();
			this.model = model;
			this.image = image;
		}

	}

	private Element elements[];

	private TreeMap viewMap;

	private int maxSize;

	private int pos = 0;

	private int PICSIZE = 60;

	public PictureCache(int size) {
		maxSize = size;
		reset();
	}

	public void reset() {
		// First, clear all existing bitmaps...
		if (elements != null) {
			for (int i= 0; i < elements.length; i++) {
				if (elements[i].image != null)
					elements[i].image.flush();
				elements[i].image= null;
			}
		}
		elements = new Element[maxSize];
		viewMap = new TreeMap();
		pos = 0;
		for (int i = 0; i < maxSize; i++)
			elements[i] = new Element(null, new BufferedImage(PICSIZE, PICSIZE,
					BufferedImage.TYPE_INT_ARGB));
	}

	public void put(ModelElement e, BufferedImage img) {
		// We record a copy of e, not the real e which migh change :
		e= e.deepCopy();
		// The first test should not be needed, but we might change our system.
		if (elements[pos] != null && elements[pos].model != null) {
			remove(elements[pos].model);
		}
		viewMap.put(e, pos);
		elements[pos] = new Element(e, img);
		pos = (pos + 1) % maxSize;
	}

	public BufferedImage get(ModelElement e) {
		BufferedImage result = null;
		Integer i = (Integer) viewMap.get(e);
		if (i != null) {
			result = elements[i.intValue()].image;
		}
		return result;
	}

	public void remove(ModelElement e) {
		BufferedImage result = null;
		Integer i = (Integer) viewMap.get(e);
		if (i != null) {
			viewMap.remove(e);
			// IMPORTANT : don't suppress the following code !
			//elements[i.intValue()]= null;
		}
	}

	
	/**
	 * Creates an image of the required size ; can fail. If the requested image
	 * is too large, the method will return null.
	 * 
	 * @param width
	 * @param height
	 * @return a new BufferedImage.
	 */
	public BufferedImage createImage(int width, int height) {
		if (width > PICSIZE || height > PICSIZE)
			return null;
		else
		    // We used to reuse old buffered images for new entries. However,
		    // due to the delayed processus of image writing, this could lead 
		    // to serious screen garbage is the cache was too small.
		    // So we try a different strategy, just in case.
		    return new BufferedImage(PICSIZE, PICSIZE,
					BufferedImage.TYPE_INT_ARGB);
			//return elements[pos].image;
	}
}