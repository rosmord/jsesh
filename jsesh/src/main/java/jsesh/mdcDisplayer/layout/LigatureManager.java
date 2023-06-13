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
/*
 * Created on 25 oct. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.layout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import jsesh.hieroglyphs.data.HieroglyphDatabaseInterface;
import jsesh.hieroglyphs.data.coreMdC.ManuelDeCodage;
import jsesh.hieroglyphs.data.HieroglyphDatabaseFactory;
import jsesh.hieroglyphs.graphics.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.graphics.HieroglyphicFontManager;
import jsesh.resources.ResourcesManager;

/**
 * Manages ligatures.
 * 
 * @author S. Rosmorduc
 */
public class LigatureManager {

	private final HieroglyphicFontManager fontmanager;
	private final HieroglyphDatabaseInterface hieroglyphDatabase;

	
	private final Map<String[], ExplicitPosition[]> ligaturesMap;

	@SuppressWarnings("unchecked")
	public LigatureManager(HieroglyphicFontManager fontmanager, HieroglyphDatabaseInterface hieroglyphDatabase) {
		this.fontmanager = fontmanager;
		this.hieroglyphDatabase = hieroglyphDatabase;
		this.ligaturesMap = new TreeMap<>(new LigatureComparator());
		try {
			readTksesh(ResourcesManager.getInstance().getLigatureData());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public ExplicitPosition[] getPositions(String codes[]) {
		String c[] = new String[codes.length];
		for (int i = 0; i < codes.length; i++)
			c[i] = getCanonicalCode(codes[i]);
		return ligaturesMap.get(c);
	}

	public void put(String codes[], ExplicitPosition[] positions) {
		ligaturesMap.put(codes, positions);
	}

	/**
	 * Reads a ligature description file (in tksesh's format).
	 * 
	 * @param in
	 * @throws IOException
	 * 
	 */
	public void readTksesh(Reader in) throws IOException {
		BufferedReader r = new BufferedReader(in);

		String s;
		while ((s = r.readLine()) != null) {
			int i;
			String parts[] = s.split("\\|");
			String codes[] = parts[0].split("&");
			// replace codes by their canonical equivalents.
			for (i = 0; i < codes.length; i++)
				codes[i] =getCanonicalCode(codes[i]);

			String pos[] = parts[1].split(" ");
			// pos[0] holds the number of signs in the ligature.
			int size = Integer.parseInt(pos[0]);
			ExplicitPosition positions[] = new ExplicitPosition[size];
			int k = 0;
			for (i = 1; i < pos.length; i += 4) {
				// i : sign code
				float x = Float.parseFloat(pos[i + 1]);
				float y = Float.parseFloat(pos[i + 2]);
				int scale = Integer.parseInt(pos[i + 3]);
				// Units are not the same in tksesh and in JSesh.
				x = (x * 1000f) / 17f;
				float h = (float) fontmanager.get(pos[i]).getBbox().getHeight();
				y = ((17f - y - h) * 1000f) / 17f;
				positions[k] = new ExplicitPosition(x, y, scale);
				k++;
			}
			put(codes, positions);
		}
	}
	
	private String getCanonicalCode(String code) {
		return ManuelDeCodage.getInstance().getCanonicalCode(code);
	}

	private static class LigatureComparator implements Comparator {

		public LigatureComparator() {
		}

		@Override
		public int compare(Object o1, Object o2) {
			String t0[] = (String[]) o1;
			String t1[] = (String[]) o2;
			int result = 0;
			int i = 0;
			while (result == 0 && i < t0.length && i < t1.length) {
				result = t0[i].compareTo(t1[i]);
				i++;
			}
			if (result == 0) {
				// both array are equals until the end of one of them.
				// the shorter is the smaller :
				result = t0.length - t1.length;
			}
			return result;
		}
	}

}
