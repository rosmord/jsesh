/*
 * Created on 21 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.parser.lex;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;

import java_cup.runtime.Symbol;
import jsesh.model.constants.SymbolCodes;
import jsesh.model.constants.ToggleType;
import jsesh.parser.MDCSyntaxError;
import jsesh.parser.ParserErrorManager;
import jsesh.parser.mdwlexer.AlphabeticText;
import jsesh.parser.mdwlexer.Cartouche;
import jsesh.parser.mdwlexer.HRule;
import jsesh.parser.mdwlexer.MdcLexer;
import jsesh.parser.mdwlexer.MdcLexicon;
import jsesh.parser.mdwlexer.MdcSign;
import jsesh.parser.mdwlexer.MdcSymbol;
import jsesh.parser.mdwlexer.MdcSymbolCode;
import jsesh.parser.mdwlexer.OldCartoucheStart;
import jsesh.parser.mdwlexer.PhilologyKind;
import jsesh.parser.mdwlexer.SignSubType;

/**
 * Lexical analyser for manuel de codage files.
 * @author rosmord
 *
 */

// Implementation note: the actual scanning is done by MdcLexer (jsesh.parser.mdwlexer),
// the hand-written lexer which replaced the JFlex-generated MDCLexAux. This class is an
// adapter: it translates each jsesh.parser.mdwlexer.MdcSymbol into the java_cup.runtime.Symbol
// and jsesh.parser.lex value types (MDCSign, MDCCartouche...) that MDCHandmadeParser and the
// rest of this package already expect, so nothing downstream of MDCLex had to change.

public class MDCLex implements java_cup.runtime.Scanner, ParserErrorManager {

	private final MdcLexer implementation;

	/**
	 * The source text, as code points, kept only to compute the line number
	 * of a given position for {@link #buildError(String)} (MdcLexer itself
	 * only tracks a flat code-point offset, not lines).
	 */
	private final int[] codePoints;

	/**
	 * The last symbol read by the lexer, including a synthesized EOF once
	 * the input is exhausted. Used by {@link #buildError(String)}.
	 */
	private MdcSymbol lastSymbol;

	/**
	 * @param in
	 */
	public MDCLex(Reader in) {
		this(readAll(in));
	}

	/**
	 * @param in
	 */
	public MDCLex(InputStream in) {
		this(readAll(new InputStreamReader(in)));
	}

	private MDCLex(String source) {
		codePoints = source.codePoints().toArray();
		implementation = MdcLexicon.instance().newLexer(source);
	}

	public  static void main(String argv[]) throws java.io.IOException {
		   MDCLex yy = new MDCLex(System.in);
		   java_cup.runtime.Symbol t;
		   while ((t = yy.next_token()).sym != MDCSymbols.EOF)
			   System.out.println(t);
	   }

	/* (non-Javadoc)
	 * @see java_cup.runtime.Scanner#next_token()
	 */
	public Symbol next_token() throws IOException {
		lastSymbol = implementation.nextSymbol()
				.orElseGet(() -> new MdcSymbol(MdcSymbolCode.EOF, null, "", implementation.position()));
		return toCupSymbol(lastSymbol);
	}

	/* (non-Javadoc)
	 * @see jsesh.model.tools.ParserErrorManager#buildError(java.lang.String)
	 */
	public MDCSyntaxError buildError(String message) {
		int line = lineOf(lastSymbol.position());
		int charPos = lastSymbol.position();
		String token = lastSymbol.text();
		String res = message + " line " + line + " char " + charPos + " at token '" + token + "'";
		return new MDCSyntaxError(res, line, charPos, token);
	}

	/**
	 * @return true if we are debugging.
	 */
	public boolean getDebug() {
		return implementation.isDebug();
	}

	/**
	 * @return if philological markers are simple signs.
	 */
	public boolean getPhilologyAsSigns() {
		return implementation.isPhilologyAsSigns();
	}

	/**
	 *
	 */
	public void reset() {
		implementation.reset();
	}

	/**
	 * @param d
	 */
	public void setDebug(boolean d) {
		implementation.setDebug(d);
	}

	/**
	 * @param p
	 */
	public void setPhilologyAsSigns(boolean p) {
		implementation.setPhilologyAsSigns(p);
	}

	/**
	 * The 0-based line number of the given code-point position, counting
	 * newlines the way the original JFlex lexer's {@code %line} did.
	 */
	private int lineOf(int codePointPos) {
		int line = 0;
		int limit = Math.min(codePointPos, codePoints.length);
		for (int i = 0; i < limit; i++) {
			if (codePoints[i] == '\n') {
				line++;
			}
		}
		return line;
	}

	private static String readAll(Reader in) {
		StringBuilder builder = new StringBuilder();
		char[] buffer = new char[4096];
		try {
			int read;
			while ((read = in.read(buffer)) != -1) {
				builder.append(buffer, 0, read);
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return builder.toString();
	}

	// ------------------------------------------------------------------
	// MdcSymbol (jsesh.parser.mdwlexer) -> java_cup.runtime.Symbol translation
	// ------------------------------------------------------------------

	private static Symbol toCupSymbol(MdcSymbol symbol) {
		int sym = toCupCode(symbol.code());
		Object value = toCupValue(symbol);
		return value == null ? new Symbol(sym) : new Symbol(sym, value);
	}

	private static int toCupCode(MdcSymbolCode code) {
		return switch (code) {
			case PAGE_END -> MDCSymbols.PAGEEND;
			case LINE_END -> MDCSymbols.LINEEND;
			case TAB_STOP -> MDCSymbols.TABSTOP;
			case TABBING -> MDCSymbols.TABBING;
			case TABBING_CLEAR -> MDCSymbols.TABBINGCLEAR;
			case HRULE -> MDCSymbols.HRULE;
			case ZONE -> MDCSymbols.ZONE;
			case QUADRAT -> MDCSymbols.CADRAT;
			case START_HIEROGLYPHS -> MDCSymbols.STARTHIEROGLYPHS;
			case TEXT_SUPER -> MDCSymbols.TEXTSUPER;
			case WORD_END -> MDCSymbols.WORDEND;
			case SENTENCE_END -> MDCSymbols.SENTENCEEND;
			case SEPARATOR -> MDCSymbols.SEPARATOR;
			case TOGGLE -> MDCSymbols.TOGGLE;
			case SHADING -> MDCSymbols.SHADING;
			case COLON -> MDCSymbols.COLON;
			case STAR -> MDCSymbols.STAR;
			case OPEN_BRACE -> MDCSymbols.OPENBRACE;
			case BEGIN_OLD_CARTOUCHE -> MDCSymbols.BEGINOLDCARTOUCHE;
			case BEGIN_CARTOUCHE -> MDCSymbols.BEGINCARTOUCHE;
			case END_CARTOUCHE -> MDCSymbols.ENDCARTOUCHE;
			case BEGIN_PHIL -> MDCSymbols.BEGINPHIL;
			case END_PHIL -> MDCSymbols.ENDPHIL;
			case LPAREN -> MDCSymbols.BPAR;
			case RPAREN -> MDCSymbols.EPAR;
			case OVERWRITE -> MDCSymbols.OVERWRITE;
			case AMP -> MDCSymbols.AMP;
			case GRAMMAR -> MDCSymbols.GRAMMAR;
			case MODIFIER -> MDCSymbols.MODIFIER;
			case HIEROGLYPH -> MDCSymbols.HIEROGLYPH;
			case DOUBLE_AMP -> MDCSymbols.DOUBLEAMP;
			case LIG_AFTER -> MDCSymbols.LIGAFTER;
			case LIG_BEFORE -> MDCSymbols.LIGBEFORE;
			case DOUBLE_LEFT_CURLY -> MDCSymbols.DOUBLELEFTCURLY;
			case DOUBLE_RIGHT_CURLY -> MDCSymbols.DOUBLERIGHTCURLY;
			case CLOSE_BRACE -> MDCSymbols.CLOSEBRACE;
			case COMMA -> MDCSymbols.COMMA;
			case EQUAL -> MDCSymbols.EQUAL;
			case INTEGER -> MDCSymbols.INTEGER;
			case IDENTIFIER -> MDCSymbols.IDENTIFIER;
			case TEXT -> MDCSymbols.TEXT;
			case UNKNOWN -> MDCSymbols.UNKNOWN;
			case EOF -> MDCSymbols.EOF;
		};
	}

	/**
	 * Rebuilds the {@code jsesh.parser.lex} value object each symbol code used
	 * to carry, from the {@code jsesh.parser.mdwlexer} value {@link MdcLexer}
	 * actually produces. {@code null}, {@link Integer} and {@link String}
	 * values are already in the shape the parser expects and pass through
	 * unchanged.
	 */
	private static Object toCupValue(MdcSymbol symbol) {
		Object value = symbol.value();
		return switch (symbol.code()) {
			case HRULE -> {
				HRule rule = (HRule) value;
				yield new MDCHRule(rule.type(), rule.start(), rule.end());
			}
			case TOGGLE -> toToggleType((jsesh.parser.mdwlexer.ToggleType) value);
			case SHADING -> new MDCShading((String) value);
			case BEGIN_OLD_CARTOUCHE -> {
				OldCartoucheStart start = (OldCartoucheStart) value;
				yield new MDCStartOldCartouche(start.code(), start.part());
			}
			case BEGIN_CARTOUCHE, END_CARTOUCHE -> {
				Cartouche cartouche = (Cartouche) value;
				yield new MDCCartouche(cartouche.type(), cartouche.part());
			}
			case BEGIN_PHIL, END_PHIL -> new MDCSubType(philologySubCode((PhilologyKind) value));
			case MODIFIER -> MDCModifier.buildMDCModifierFromString((String) value);
			case HIEROGLYPH -> {
				MdcSign sign = (MdcSign) value;
				yield new MDCSign(toSignTypeCode(sign.subtype()), sign.text());
			}
			case TEXT -> {
				AlphabeticText text = (AlphabeticText) value;
				yield new MDCAlphabeticText(text.code(), text.text());
			}
			default -> value;
		};
	}

	/**
	 * The {@link SymbolCodes} constant for a hieroglyph's subtype: the plain
	 * subtypes map directly, while a philological bracket read as a sign
	 * (philologyAsSigns mode) reproduces the original lexer's
	 * {@code subtype * 2} / {@code subtype * 2 + 1} (begin/end) scheme.
	 */
	private static int toSignTypeCode(SignSubType subtype) {
		return switch (subtype) {
			case SignSubType.Plain plain -> switch (plain) {
				case MDC_CODE -> SymbolCodes.MDCCODE;
				case RED_POINT -> SymbolCodes.REDPOINT;
				case BLACK_POINT -> SymbolCodes.BLACKPOINT;
				case SMALL_TEXT -> SymbolCodes.SMALLTEXT;
				case HALF_SPACE -> SymbolCodes.HALFSPACE;
				case FULL_SPACE -> SymbolCodes.FULLSPACE;
				case FULL_SHADE -> SymbolCodes.FULLSHADE;
				case VERTICAL_SHADE -> SymbolCodes.VERTICALSHADE;
				case QUARTER_SHADE -> SymbolCodes.QUATERSHADE;
				case HORIZONTAL_SHADE -> SymbolCodes.HORIZONTALSHADE;
			};
			case SignSubType.Philology(PhilologyKind kind, boolean begin) -> {
				int base = philologySubCode(kind);
				yield begin ? base * 2 : base * 2 + 1;
			}
		};
	}

	/** The {@link SymbolCodes} philology sub-type constant (50-56) for a bracket kind. */
	private static int philologySubCode(PhilologyKind kind) {
		return switch (kind) {
			case ERASED_SIGNS -> SymbolCodes.ERASEDSIGNS;
			case EDITOR_ADDITION -> SymbolCodes.EDITORADDITION;
			case EDITOR_SUPERFLUOUS -> SymbolCodes.EDITORSUPERFLUOUS;
			case PREVIOUSLY_READABLE -> SymbolCodes.PREVIOUSLYREADABLE;
			case SCRIBE_ADDITION -> SymbolCodes.SCRIBEADDITION;
			case MINOR_ADDITION -> SymbolCodes.MINORADDITION;
			case DUBIOUS -> SymbolCodes.DUBIOUS;
		};
	}

	private static ToggleType toToggleType(jsesh.parser.mdwlexer.ToggleType toggle) {
		return switch (toggle) {
			case SHADING_TOGGLE -> ToggleType.SHADINGTOGGLE;
			case SHADING_ON -> ToggleType.SHADINGON;
			case SHADING_OFF -> ToggleType.SHADINGOFF;
			case RED -> ToggleType.RED;
			case BLACK -> ToggleType.BLACK;
			case BLACK_RED -> ToggleType.BLACKRED;
			case LACUNA -> ToggleType.LACUNA;
			case LINE_LACUNA -> ToggleType.LINELACUNA;
			case OMIT -> ToggleType.OMMIT;
		};
	}
}
