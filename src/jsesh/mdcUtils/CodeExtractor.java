package jsesh.mdcUtils;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import jsesh.hieroglyphs.GardinerCode;
import jsesh.hieroglyphs.HieroglyphsManager;
import jsesh.mdc.MDCParserFacade;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.interfaces.HieroglyphInterface;
import jsesh.mdc.interfaces.MDCBuilderAdapter;
import jsesh.mdc.interfaces.ModifierListInterface;

public class CodeExtractor {
	private boolean normalize= true;
	
	public String[] getCodes(String manuelDeCodageText) throws MDCSyntaxError {

		SignListBuilder builder = new SignListBuilder();
		MDCParserFacade parser = new MDCParserFacade(builder);
		parser.parse(new StringReader(manuelDeCodageText));
		return (String[]) builder.result.toArray(new String[builder.result.size()]);
	}
	

	
	private class SignListBuilder extends MDCBuilderAdapter {
		
		public List result;

		/* (non-Javadoc)
		 * @see jsesh.mdc.interfaces.MDCBuilder#reset()
		 */
		public void reset() {
			result= new LinkedList();
		}
	
		/* (non-Javadoc)
		 * @see jsesh.mdc.interfaces.MDCBuilder#buildHieroglyph(boolean, int, java.lang.String, jsesh.mdc.interfaces.ListInterface, int)
		 */
		public HieroglyphInterface buildHieroglyph(boolean isGrammar, int type, String code, ModifierListInterface m, int isEnd) {
			String actualCode= code;
			if (GardinerCode.isCanonicalCode(code))
				result.add(actualCode);
			else if (HieroglyphsManager.getInstance().isKnownCode(actualCode)) {
				result.add(HieroglyphsManager.getInstance().getCanonicalCode(code));
			}
			return null;
		}

	}



	/**
	 * Should we normalize the codes to "Gardiner" code ?
	 * @return the normalize
	 */
	public boolean isNormalize() {
		return normalize;
	}



	/**
	 * Should we normalize the codes to "Gardiner" code ?
	 * @param normalize the normalize to set
	 */
	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}
	
	
	public static void main(String[] args) throws MDCSyntaxError {
		CodeExtractor codeExtractor= new CodeExtractor();
		String[] l = codeExtractor.getCodes("i-w-r:a-ra-[[-m-]]-p*t:pt");
		for (int i=0; i < l.length; i++)
			System.out.print(l[i]+ " ");
		System.out.println();
	}
}
