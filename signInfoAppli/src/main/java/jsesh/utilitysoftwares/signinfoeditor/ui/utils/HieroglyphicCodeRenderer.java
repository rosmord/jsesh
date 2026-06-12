/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
package jsesh.utilitysoftwares.signinfoeditor.ui.utils;

import java.awt.Component;

import javax.swing.table.DefaultTableCellRenderer;

import jsesh.hieroglyphs.utils.HieroglyphPictureBuilder;
import jsesh.hieroglyphs.utils.IconRenderOptions;
import jsesh.hieroglyphs.utils.PictureDimension;

/**
 * A hieroglyph Renderer for JTable cells, rendering <em>one</em> sign
 * 
 * The sign is given  by its mdc code.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
@SuppressWarnings("serial")
public class HieroglyphicCodeRenderer extends DefaultTableCellRenderer {

	private PictureDimension bitmapSize = new PictureDimension(30,30);
	private int bitmapBorder = 4;
	private HieroglyphPictureBuilder pictureBuilder;
	// Consider for removal when Icon generation is fixed.
	private Component referenceComponent;

	
	/**
	 * Build a renderer.
	 * @param bitmapSize
	 * @param bitmapBorder
	 * @param referenceComponent a Swing component, probably the table, used to get information about rendering depth and so on; can be null.
	 */
	public HieroglyphicCodeRenderer(PictureDimension bitmapSize, int bitmapBorder,
			HieroglyphPictureBuilder pictureBuilder,
		 Component referenceComponent) {
		this.pictureBuilder = pictureBuilder;
		this.bitmapSize = bitmapSize;
		this.bitmapBorder = bitmapBorder;
		this.referenceComponent = referenceComponent;
	}


	protected void setValue(Object value) {
		String code= (String) value;
		setText(code);
		if (code != null && ! "".equals(code)) {
			int width = bitmapSize.width() -2 * bitmapBorder;
			int height = bitmapSize.height() -2 * bitmapBorder;
			IconRenderOptions options = IconRenderOptions.DEFAULT.copy().dimension(width,height).border(bitmapBorder).build();
			setIcon(pictureBuilder.createHieroglyphIcon(code, options));
	}
		else {
			setIcon(null);
		}
	}
}
