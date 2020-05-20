package jsesh.graphics.glyphs.model;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jsesh.hieroglyphs.graphics.ShapeChar;
import jsesh.utils.TclImporter;

/**
 * Importer for signs created with Tksesh's font editor.
 * 
 * @author rosmord
 * 
 */
public class TmlSignsImporter implements SimpleSignSourceModel {

	private TreeMap signs;
	private Map codeMap;
	/**
	 * List of "user codes" of the form UG0M0N1
	 */
	private String codeList[];
	
	int pos;

	public TmlSignsImporter(Reader r) throws IOException {
		signs= new TreeMap();
		codeMap= new TreeMap();
		List l = new TclImporter().parseTclList(r);
		r.close();
		parseList(l);
		codeList= (String[]) signs.keySet().toArray(new String[signs.keySet().size()]);
		beforeFirst();
	}

	private void parseList(List l) {
		java.util.Iterator iter = l.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			if (o instanceof List) {
				List elt = ((List) o);
				if (elt.size() > 0) { 
					if ("Glyph".equals(elt.get(0).toString())) {
						parseGlyph(elt);
					} else if ("HasCode".equals(elt.get(0).toString())){
						parseHasCode(elt);
					}
				}
			}
		}
	}

	private void parseHasCode(List elt) {
		//		 Sign identifier :
		int uid= ((Double) elt.get(6)).intValue(); 
		int mid= ((Double) elt.get(7)).intValue();
		int id= ((Double) elt.get(8)).intValue();
		// MdC code
		String mdc= (String) elt.get(9);  
		String stdCode= "UG"+ uid+ "M"+ mid+ "N"+ id;
		codeMap.put(stdCode, mdc);
	}
	
	private void parseGlyph(List elt) {		
		ShapeChar newSign = new ShapeChar();
		Area area= new Area();

		// Sign identifier :
		int uid= ((Double) elt.get(1)).intValue(); 
		int mid= ((Double) elt.get(2)).intValue();
		int id= ((Double) elt.get(3)).intValue();
		
		// The actual glyph in a Tml Glyph description
		List glyph = (List) elt.get(6);
		// The drawing itself (after the bounding box, the code and the width.)
		int i = 6;
		while (i < glyph.size()) {
			GeneralPath subPath = new GeneralPath();	
			List contour = (List) glyph.get(i);
			float startx = ((Double) contour.get(2)).floatValue();
			float starty = ((Double) contour.get(3)).floatValue();
			subPath.moveTo(startx, starty);
			int k = 4;
			while (k < contour.size()) {
				List segment = (List) contour.get(k);
				char codeSegment = segment.get(0).toString().charAt(0);
				if (codeSegment == 's') {
					float c1x = ((Double) segment.get(1)).floatValue();
					float c1y = ((Double) segment.get(2)).floatValue();
					float c2x = ((Double) segment.get(3)).floatValue();
					float c2y = ((Double) segment.get(4)).floatValue();
					float x = ((Double) segment.get(5)).floatValue();
					float y = ((Double) segment.get(6)).floatValue();
					subPath.curveTo(c1x, c1y, c2x, c2y, x, y);
				} else if (codeSegment == 'l') {
					float x = ((Double) segment.get(1)).floatValue();
					float y = ((Double) segment.get(2)).floatValue();
					subPath.lineTo(x, y);
				}
				k++;
			}
			subPath.closePath();
			if ("P".equals(contour.get(1).toString())) {
				area.add(new Area(subPath));
			} else {
				area.subtract(new Area(subPath));
			}
			i++;
		}
		GeneralPath finalPath= new GeneralPath(); finalPath.append(area.getPathIterator(AffineTransform.getScaleInstance(1, -1)), false);
		newSign.setShape(finalPath);
		//newSign.fixShape();
	
		String stdCode= "UG"+ uid+ "M"+ mid+ "N"+ id; 
		signs.put(stdCode, newSign);
		if (! codeMap.containsKey(stdCode))
			codeMap.put(stdCode, stdCode);
	}

	public ShapeChar getCurrentShape() {
		return (ShapeChar) signs.get(codeList[pos]);		
	}

	public boolean hasNext() {
		return (pos + 1 < signs.size());
	}

	public boolean hasPrevious() {
		return pos > 0;
	}

	public void next() {
		if (hasNext())
			pos++;
	}

	public void previous() {
		if (hasPrevious())
			pos--;

	}
	
	public String getCurrentCode() {
		return (String) codeMap.get(codeList[pos]);
	}

	public void afterLast() {
		pos= signs.size();
		
	}

	public void beforeFirst() {
		pos= -1;
	}
}
