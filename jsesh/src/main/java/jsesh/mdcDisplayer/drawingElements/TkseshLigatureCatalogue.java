package jsesh.mdcDisplayer.drawingElements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jsesh.hieroglyphs.data.coreMdC.ManuelDeCodage;
import jsesh.mdcDisplayer.layout.ExplicitPosition;
import jsesh.resources.ResourcesManager;

/**
 * A catalogue of pre-defined ligatures inherited from tksesh.
 * 
 * <p>
 * Data here won't be modified, and we can make it a singleton.
 */
class TkseshLigatureCatalogue {
    private static final TkseshLigatureCatalogue instance = new TkseshLigatureCatalogue();

    public static TkseshLigatureCatalogue getInstance() {
        return instance;
    }

    /**
     * Manages old-fashion tksesh ligatures.
     */

    private final Map<List<String>, List<ExplicitPosition>> ligaturesMap = new HashMap<>();

    private TkseshLigatureCatalogue() {
         try (Reader reader = ResourcesManager.getInstance().getLigatureData()) {
			readTksesh(reader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}        
    }


    /**
	 * Reads a ligature description file.
     * 
     * We have decided to use a JSesh-friendly format now.
	 * 
     * The format is :
     * <pre>
     * lig::= ligname '|' ligdef ligdef*
     * ligname ::= CODE ('&' code)*
     * ligdef ::= CODE X Y SCALE
     * <pre>
     * 
     * <ul>
     * <li> X 
     * <li> Y
     * <li> SCALE
     * </ul>
     * 
	 * @param in
	 * @throws IOException
	 * 
	 */
	private void readTksesh(Reader in) throws IOException {
        ManuelDeCodage manuelDeCodage = ManuelDeCodage.getInstance();
		BufferedReader r = new BufferedReader(in);

		String s;
		while ((s = r.readLine()) != null) {
			int i;
			String parts[] = s.split("\\|");
			List<String> codes = Arrays.stream(parts[0].split("&"))
                .map(c -> manuelDeCodage.getCanonicalCode(c)).toList();

            // Split the second part
            String pos[] = parts[1].strip().split(" ");

            List<ExplicitPosition> positions = new ArrayList<>();
			for (i = 0; i < pos.length; i += 4) {
				// i : sign code
				float x = Float.parseFloat(pos[i + 1]);
				float y = Float.parseFloat(pos[i + 2]);
				int scale = Integer.parseInt(pos[i + 3]);
                positions.add(new ExplicitPosition(x, y, scale));
			}
			ligaturesMap.put(codes, positions);
		}
	}


    /**
     * Returns the predefined positions for a ligature, if any.
     * @param normalizedCodes
     * @return
     */
    public List<ExplicitPosition> get(List<String> normalizedCodes) {
        return ligaturesMap.get(normalizedCodes);
    }
}
