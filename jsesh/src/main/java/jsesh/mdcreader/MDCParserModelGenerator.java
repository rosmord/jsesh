
/* 
 * Created on 24 déc. 2003 by rosmord.
 * This code is distributed under the LGPL.
 *  
 */
package jsesh.mdcreader;

import java.io.Reader;

import jsesh.model.TopItemList;
import jsesh.model.constants.Dialect;
import jsesh.parser.MDCParserAstGenerator;
import jsesh.parser.MDCSyntaxError;

/**
 * A Parser for MdC code which generates a model (TopItemList)
 * for the code.
 *
 * <p>Internally, this parses to the literal AST (see {@link jsesh.parser.ast})
 * via {@link MDCParserAstGenerator}, then interprets that AST into the model
 * with {@link AstModelBuilder}.
 *
 * @author rosmord
 * @see jsesh.model.TopItemList
 */
public class MDCParserModelGenerator {
	private final MDCParserAstGenerator astGenerator;
	private final Dialect dialect;

	public MDCParserModelGenerator() {
		this(Dialect.OTHER);
	}

	/**
	 *
	 * @param dialect a dialect for the Manuel de codage
	 * @see Dialect
	 */
	public MDCParserModelGenerator(Dialect dialect) {
		astGenerator = new MDCParserAstGenerator();
		this.dialect = dialect;
	}

	public TopItemList parse(Reader in) throws MDCSyntaxError {
		return new AstModelBuilder(dialect).build(astGenerator.parse(in));
	}

	public TopItemList parse(String text) throws MDCSyntaxError {
		return new AstModelBuilder(dialect).build(astGenerator.parse(text));
	}


	/**
	 * @return true if we are debugging.
	 */
	public boolean isDebug() {
		return astGenerator.isDebug();
	}

	/**
	 * if true, philological markers, such as [[ and ]], are considered
	 * as simple signs, and not as constructs.
	 * @return true if philological markers are considered as simple signs.
	 */
	public boolean isPhilologyAsSigns() {
		return astGenerator.isPhilologyAsSigns();
	}

	/**
	 * @param v
	 */
	public void setDebug(boolean v) {
		astGenerator.setDebug(v);
	}

	/**
	 * @param v
	 */
	public void setPhilologyAsSigns(boolean v) {
		astGenerator.setPhilologyAsSigns(v);
	}

}
