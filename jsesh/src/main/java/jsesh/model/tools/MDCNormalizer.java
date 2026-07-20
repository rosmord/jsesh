package jsesh.model.tools;

import jsesh.glyphs.data.coremdc.ManuelDeCodage;
import jsesh.model.Hieroglyph;
import jsesh.model.ModelElementDeepAdapter;
import jsesh.model.TopItemList;

/**
 * An expert able to normalize hieroglyphs in a MDC text (i.e. replace all codes by Gardiner codes for searches).
 * 
 * @author rosmord
 */
public class MDCNormalizer {
	/**
	 * Normalize an existing MDC Text internal representation.
	 * Replaces all codes by the canonical Gardiner codes for the sign. e.g. "xpr" will be replaced by "L1".
	 * @param topItemList
	 */
	public void normalize(TopItemList topItemList) {
		topItemList.accept(new NormalizerAux());
	}

	private class NormalizerAux extends ModelElementDeepAdapter {
		public void visitHieroglyph(Hieroglyph h) {
			String code = ManuelDeCodage.getInstance()
					.getCanonicalCode(h.getCode()).code();
			h.setCode(code);
            
		}
        
	}

}
