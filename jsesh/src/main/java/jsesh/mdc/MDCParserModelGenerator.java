/*
 * Created on 24 d�c. 2003 by rosmord.
 *
 * This code is distributed under the LGPL.
 *  
 */
package jsesh.mdc;

import java.io.Reader;
import java.io.StringReader;

import jsesh.mdc.constants.Dialect;
import jsesh.mdc.model.MDCModelBuilder;
import jsesh.mdc.model.TopItemList;

/**
 * A Parser for MdC code which generates a model (TopItemList)
 * for the code.
 * 
 * @author rosmord
 * @see jsesh.mdc.model.TopItemList
 */
public class MDCParserModelGenerator {
	private MDCParserFacade facade;

	public MDCParserModelGenerator() {
		facade = new MDCParserFacade(new MDCModelBuilder());
	}

	/**
	 * 
	 * @param dialect a dialect for the Manuel de codage
	 * @see Dialect
	 */
	public MDCParserModelGenerator(Dialect dialect) {
		facade = new MDCParserFacade(new MDCModelBuilder(dialect));
	}

	public TopItemList parse(Reader in) throws MDCSyntaxError {
		facade.parse(in);
		return ((MDCModelBuilder) facade.getBuilder()).getResult();
	}
	
	public TopItemList parse(String text) throws MDCSyntaxError {
		facade.parse(new StringReader(text));
		return ((MDCModelBuilder) facade.getBuilder()).getResult();
	}
	
	
	/**
	 * @return true if we are debugging.
	 */
	public boolean isDebug() {
		return facade.isDebug();
	}

	/**
	 * if true, philological markers, such as [[ and ]], are considered
	 * as simple signs, and not as constructs.
	 * @return true if philological markers are considered as simple signs.
	 */
	public boolean isPhilologyAsSigns() {
		return facade.isPhilologyAsSigns();
	}

	/**
	 * @param v
	 */
	public void setDebug(boolean v) {
		facade.setDebug(v);
	}

	/**
	 * @param v
	 */
	public void setPhilologyAsSigns(boolean v) {
		facade.setPhilologyAsSigns(v);
	}

}
