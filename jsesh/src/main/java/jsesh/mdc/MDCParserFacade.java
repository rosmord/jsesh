package jsesh.mdc;

import java.io.Reader;

import java_cup.runtime.Symbol;
import jsesh.mdc.interfaces.MDCBuilder;
import jsesh.mdc.lex.MDCLex;
import jsesh.mdc.parser.MDCParse;


/**
 * Facade for the Parser process for java.  This is still a rather
 * low-level system. It can't do anything without the help of a
 * builder class. It's a brick for advanced programmers.
 * 
 * For most uses, you want MDCParserModelGenerator.
 * 
 * <p> A number of useful simple parser will be provided for usual
 * needs.
 *
 * @see MDCParserModelGenerator
 */
public class MDCParserFacade {

	private MDCParse parser;
	private boolean debug;
	//private MDCBuilder builder;
	private boolean philologyAsSigns;

	/**
	 * Build a Parser for MDC text, but without any builder; normally
	 * that's not the method you want to use.  
	 */

	private MDCParserFacade() {
		parser = new MDCParse();
		setDebug(false);
		setPhilologyAsSigns(true);
	}

	/**
	 * Build a Parser for MDC text. Building will be done through the
	 * methods of the builder class you pass.
	 *
	 * @param builder a <code>MDCBuilder</code> value 
	 */
	public MDCParserFacade(MDCBuilder builder) {
		this();
		setBuilder(builder);
	}

	/**
	 * Get the value of builder.
	 * @return value of builder.
	 */
	public MDCBuilder getBuilder() {
		return parser.getBuilder();
	}

	/**
	 * Set the value of builder.
	 * @param v  Value to assign to builder.
	 */
	public void setBuilder(MDCBuilder v) {
		parser.setBuilder(v);
	}

	public Object parse(Reader in) throws MDCSyntaxError {
		Symbol parse_tree = null;
		MDCLex lex = new MDCLex(in);
		lex.setPhilologyAsSigns(philologyAsSigns);
		lex.setDebug(debug);
		parser.setScanner(lex);
		parser.setErrorManager(lex);
		try {

			if (isDebug()) {
				parse_tree = parser.debug_parse();
			} else {
				parse_tree = parser.parse();
			}
			return parse_tree.value;
		} catch (MDCSyntaxError e) {
			throw e; 
		}
		 catch (Exception e) {
		 	MDCSyntaxError err= 
		 		new MDCSyntaxError("Generic Error", 0, 0,e.getMessage());
		 		err.setStackTrace(e.getStackTrace());
		 		throw err;	
		}
	}

	/**
	 * Is debugging mode enabled?
	 * @return value of debug.
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * Set or unset debugging mode. If debugging is true, the system
	 * will print many information on the text reading process, most
	 * of which is difficult to understand if you don't know anything
	 * about parsing. So this is a programmer's help, not to be used
	 * for final programs.
	 * @param v Value to assign to debug.  
	 */
	public void setDebug(boolean v) {
		this.debug = v;
	}

	/**
	 * If true, treat philology code like [[ and ]] as signs ; else treat them as a grouping command.
	 * @return value of PhilologyAsSigns.
	 */
	public boolean isPhilologyAsSigns() {
		return philologyAsSigns;
	}

	/**
	 * If true, treat philology code like [[ and ]] as signs (as winglyph); else
	 * treat them as a grouping command (as tksesh).
	 * @param v Value to assign to PhilologyAsSigns.  */
	public void setPhilologyAsSigns(boolean v) {
		this.philologyAsSigns = v;
	}

} // MDCParserFacade
