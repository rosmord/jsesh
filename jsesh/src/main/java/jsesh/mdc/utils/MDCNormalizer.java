package jsesh.mdc.utils;

import java.io.StringReader;
import java.io.StringWriter;

import jsesh.hieroglyphs.data.HieroglyphDatabaseRepository;
import jsesh.mdc.MDCParserModelGenerator;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElementDeepAdapter;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.output.MdCModelWriter;

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
			String code = HieroglyphDatabaseRepository.getHieroglyphDatabase()
					.getCanonicalCode(h.getCode());
			h.setCode(code);
            
		}
        
	}

	/**
	 * Build a normalized Text representation.
	 * The glyphs will have their correct Gardiner codes. 
	 * Note that some superfluous information may disappear as a result of parsing.
	 * @param mdc
	 * @return
	 * @throws MDCSyntaxError
	 */
	public String normalize(String mdc) throws MDCSyntaxError {
		// Get the representation
		MDCParserModelGenerator mdcParser = new MDCParserModelGenerator();
		TopItemList result = mdcParser.parse(new StringReader(mdc));
		normalize(result);
		MdCModelWriter mdCModelWriter = new MdCModelWriter();
		StringWriter out = new StringWriter();
		mdCModelWriter.write(out, result);
		return out.toString();
	}

	public static void main(String[] args) throws MDCSyntaxError {
		String mdc= "nn-nTrw-R-N-i-w-r:a-O-$r-ra-[[-m-]]-p*t:pt-$b-10";
		String mdcNorm= new MDCNormalizer().normalize(mdc);
		System.out.println(mdcNorm);
	}
}
