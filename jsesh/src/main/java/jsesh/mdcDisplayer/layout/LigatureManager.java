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

import jsesh.hieroglyphs.CompositeHieroglyphsManager;
import jsesh.hieroglyphs.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.HieroglyphicFontManager;
import jsesh.resources.ResourcesManager;

/**
 * Manages ligatures.
 * Currently a singleton.
 * @author S. Rosmorduc
 *
 */
public class LigatureManager {

	static private LigatureManager instance = null;

	/**
	 * @return the ligature manager.
	 */
	public static LigatureManager getInstance() {
		if (instance == null) {
			instance = new LigatureManager();
			try {
				instance.readTksesh(
					ResourcesManager.getInstance().getLigatureData());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return instance;
	}
	private final Map<String[], ExplicitPosition[]> ligaturesMap;

	@SuppressWarnings("unchecked")
	public LigatureManager() {
		// Using checked types here would not work easily, as one can't use arrays as 
		// generic type.
		// We would need to build arraylists of strings, which we might do later.
		// meanwhile, we are content with @SuppressWarnings
		ligaturesMap = new TreeMap<>(new LigatureComparator());
	}

	public ExplicitPosition[] getPositions(String codes[]) {
		String c[]= new String[codes.length];
		for (int i= 0; i< codes.length; i++)
			c[i]= CompositeHieroglyphsManager.getInstance().getCanonicalCode(codes[i]);
		return ligaturesMap.get(c);
	}

	public void put(String codes[], ExplicitPosition[] positions) {
		ligaturesMap.put(codes, positions);
	}

	/**
	 * Reads a ligature description file (in tksesh's format).
	 * @param in
	 * @throws IOException
	 * 
	 */
	public void readTksesh(Reader in) throws IOException {
		BufferedReader r = new BufferedReader(in);
		HieroglyphicFontManager fontmanager= DefaultHieroglyphicFontManager.getInstance();
		
		String s;
		while ((s = r.readLine()) != null) {
			int i;
			String parts[] = s.split("\\|");
			String codes[] = parts[0].split("&");
			// replace codes by their canonical equivalents.
			for(i= 0; i< codes.length; i++)
				codes[i]= CompositeHieroglyphsManager.getInstance().getCanonicalCode(codes[i]);
			
			String pos[] = parts[1].split(" ");
			// pos[0] holds the number of signs in the ligature.
			int size = Integer.parseInt(pos[0]);
			ExplicitPosition positions[]= new ExplicitPosition[size];
			int k = 0;
			for (i = 1; i < pos.length; i += 4) {
				// i : sign code
				float x = Float.parseFloat(pos[i + 1]);
				float y = Float.parseFloat(pos[i + 2]);
				int scale = Integer.parseInt(pos[i + 3]);
				// Units are not the same in tksesh and in JSesh.
				x= (x * 1000f)/17f;
				float h= (float) fontmanager.get(pos[i]).getBbox().getHeight();
				y= ((17f - y - h) * 1000f)/17f;
				positions[k] = new ExplicitPosition(x,y,scale);
				k++;
			}
			put(codes, positions);
		}
	}

    private static class LigatureComparator implements Comparator {

        public LigatureComparator() {
        }

        @Override
        public int compare(Object o1, Object o2) {
            String t0[]= (String[]) o1;
            String t1[]= (String[]) o2;
            int result= 0;
            int i= 0;
            while (result == 0 && i < t0.length && i < t1.length) {
                result= t0[i].compareTo(t1[i]);
                i++;
            }
            if (result == 0) {
                // both array are equals until the end of one of them.
                // the shorter is the smaller :
                result= t0.length - t1.length;
            }
            return result;
        }
    }

}
