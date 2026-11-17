/*
 * Created on 24 déc. 2003 by rosmord.
 *
 * This code is distributed under the LGPL.
 *  
 */
package jsesh.mdc;

import java.io.Reader;

import jsesh.mdc.model.MDCModelBuilder;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.utils.MDCSyntaxError;

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

	public TopItemList parse(Reader in) throws MDCSyntaxError {
		TopItemList result;
		facade.parse(in);
		return ((MDCModelBuilder) facade.getBuilder()).getResult();
	}
	
	
	
	/**
	 * @return
	 */
	public boolean isDebug() {
		return facade.isDebug();
	}

	/**
	 * if true, philological markers, such as [[ and ]], are considered
	 * as simple signs, and not as constructs.
	 * @return
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
