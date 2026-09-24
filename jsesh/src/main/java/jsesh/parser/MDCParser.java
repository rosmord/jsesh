package jsesh.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import jsesh.model.constants.SymbolCodes;
import jsesh.model.constants.ToggleType;
import jsesh.model.constants.WordEndingCode;
import jsesh.parser.ast.AstAbsoluteGroup;
import jsesh.parser.ast.AstAlphabeticText;
import jsesh.parser.ast.AstBasicItemList;
import jsesh.parser.ast.AstCadrat;
import jsesh.parser.ast.AstCartouche;
import jsesh.parser.ast.AstComplexLigature;
import jsesh.parser.ast.AstDocument;
import jsesh.parser.ast.AstHBox;
import jsesh.parser.ast.AstHRule;
import jsesh.parser.ast.AstHieroglyph;
import jsesh.parser.ast.AstHorizontalListElement;
import jsesh.parser.ast.AstInnerGroup;
import jsesh.parser.ast.AstLigature;
import jsesh.parser.ast.AstLineBreak;
import jsesh.parser.ast.AstModifier;
import jsesh.parser.ast.AstModifierList;
import jsesh.parser.ast.AstNode;
import jsesh.parser.ast.AstOption;
import jsesh.parser.ast.AstOptionList;
import jsesh.parser.ast.AstOverwrite;
import jsesh.parser.ast.AstPageBreak;
import jsesh.parser.ast.AstPhilology;
import jsesh.parser.ast.AstStartHieroglyphicText;
import jsesh.parser.ast.AstSubCadrat;
import jsesh.parser.ast.AstSuperscript;
import jsesh.parser.ast.AstTabStop;
import jsesh.parser.ast.AstTabbing;
import jsesh.parser.ast.AstTabbingClear;
import jsesh.parser.ast.AstToggle;
import jsesh.parser.ast.AstTopItemList;
import jsesh.parser.ast.AstZoneStart;
import jsesh.parser.mdwlexer.AlphabeticText;
import jsesh.parser.mdwlexer.Cartouche;
import jsesh.parser.mdwlexer.HRule;
import jsesh.parser.mdwlexer.MdcLexer;
import jsesh.parser.mdwlexer.MdcLexicon;
import jsesh.parser.mdwlexer.MdcSign;
import jsesh.parser.mdwlexer.MdcSymbol;
import jsesh.parser.mdwlexer.MdcSymbolCode;
import jsesh.parser.mdwlexer.Modifier;
import jsesh.parser.mdwlexer.OldCartoucheStart;
import jsesh.parser.mdwlexer.PhilologyKind;
import jsesh.parser.mdwlexer.SignSubType;

import static jsesh.parser.mdwlexer.MdcSymbolCode.*;

/**
 * Hand-written recursive descent parser for Manuel de Codage text, building
 * the {@link jsesh.parser.ast} tree directly (see {@link #parse(String)}).
 * <p>It replaced an earlier CUP-generated parser
 * ({@code jsesh/src/jcup/MDCParse.y}), checked equivalent construct-by-
 * construct before that grammar was retired. It drives the hand-written
 * {@link jsesh.parser.mdwlexer.MdcLexer} (itself the replacement for the
 * retired JFlex-generated {@code MDCLexAux}) directly, translating each
 * {@link jsesh.parser.mdwlexer.MdcSymbol} it reads into the
 * {@link jsesh.parser.ast} tree. The handful of lexeme-to-model-code mappings
 * ({@link #toSignTypeCode}, {@link #philologySubCode}, {@link #toToggleType})
 * live here, not in {@code jsesh.parser.mdwlexer}, since they depend on
 * {@code jsesh.model.constants} and the scanner is deliberately
 * self-contained. The grammar is LALR(1) without conflicts, and its
 * left-recursive rules are all plain lists, so one token of lookahead is
 * enough; each method below documents the grammar rule it implements.
 * <p>Any syntax error aborts the parse with an {@link MDCSyntaxError}: there
 * is no error recovery.
 * <p>A parser object is not thread-safe, but can be reused for successive
 * parses.
 *
 * @author rosmord
 */

/// Recursive descent parser for Manuel de Codage texts.
/// 
/// It builds an [AstDocument] from the input.
/// 
/// Any syntax error aborts the parse with an {@link MDCSyntaxError}: there is no error recovery.
/// The GUI software (JSesh) reads files line-by-line, so it catches errors at the line level.
public class MDCParser {

    private boolean debug = false;
    private boolean philologyAsSigns = true;

    private MdcLexer lexer;

    /// The source text, represented as an array of code points.
    /// 
    /// Used to keep track of line numbers.
    /// 
    private int[] codePoints;

    /// The current lookahead symbol.
    private MdcSymbol token;

    public AstDocument parse(String text) throws MDCSyntaxError {
        return parse(new StringReader(text));
    }

    public AstDocument parse(Reader in) throws MDCSyntaxError {
        String source = readAll(in);
        codePoints = source.codePoints().toArray();
        lexer = MdcLexicon.instance().newLexer(source);
        lexer.setPhilologyAsSigns(philologyAsSigns);
        lexer.setDebug(debug);
        try {
            advance();
            return parseMdcFile();
        } catch (MDCSyntaxError e) {
            throw e;
        } catch (Exception e) {
            MDCSyntaxError err = new MDCSyntaxError("Generic Error", 0, 0, e.getMessage());
            err.setStackTrace(e.getStackTrace());
            throw err;
        } finally {
            lexer = null;
            token = null;
            codePoints = null;
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

   
    /// Are philological markers, such as `[[` and `]]`, considered as simple signs.
    /// 
    /// if false, they work as parenthesis. This was only the case in tksesh.
    /// 
    /// Defaults to true.
    /// 
    /// @return true if philological markers are considered as simple signs.
    /// 
    public boolean isPhilologyAsSigns() {
        return philologyAsSigns;
    }

    public void setPhilologyAsSigns(boolean philologyAsSigns) {
        this.philologyAsSigns = philologyAsSigns;
    }

    // ------------------------------------------------------------------
    // Source buffering / error reporting
    // ------------------------------------------------------------------

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

    /// Line number of a position.
    /// 
    /// 0 based.
    /// @return the line number of the given position.    
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

    private MDCSyntaxError error(String message) {
        int line = lineOf(token.position());
        int charPos = token.position();
        String text = token.text();
        String res = message + " line " + line + " char " + charPos + " at token '" + text + "'";
        return new MDCSyntaxError(res, line, charPos, text);
    }

    // ------------------------------------------------------------------
    // Token handling
    // ------------------------------------------------------------------

    private void advance() {
        token = lexer.nextSymbol().orElseGet(() -> new MdcSymbol(EOF, null, "", lexer.position()));
    }

    private boolean at(MdcSymbolCode sym) {
        return token.code() == sym;
    }

    /**
     * Checks that the current token is of the given type, consumes it,
     * and returns its value.
     */
    private Object expect(MdcSymbolCode sym, String what) throws MDCSyntaxError {
        if (!at(sym)) {
            throw error("expected " + what);
        }
        Object value = token.value();
        advance();
        return value;
    }

    private boolean atHieroglyphStart() {
        return at(GRAMMAR) || at(HIEROGLYPH);
    }

    private boolean atCadratStart() {
        return atHieroglyphStart() || at(BEGIN_PHIL) || at(LPAREN)
                || at(BEGIN_CARTOUCHE) || at(BEGIN_OLD_CARTOUCHE) || at(QUADRAT);
    }

    // ------------------------------------------------------------------
    // Top level
    // ------------------------------------------------------------------

    /**
     * <pre>
     * mdcfile ::= topitems optSeparator EOF
     * topitems ::= ε | topitems optSeparator topitem
     * topitem ::= cadrat optShading | TOGGLE | STARTHIEROGLYPHS | TEXT | TEXTSUPER
     *           | LINEEND | PAGEEND | TABSTOP | TABBING optionList | TABBINGCLEAR
     *           | HRULE | zoneStart
     * </pre>
     * The final optSeparator is the one consumed by the last loop iteration,
     * when it isn't followed by an item.
     */
    private AstDocument parseMdcFile() throws MDCSyntaxError {
        List<AstNode> items = new ArrayList<>();
        while (true) {
            skipOptSeparator();
            AstNode item;
            switch (token.code()) {
                case TOGGLE:
                    item = new AstToggle(toToggleType((jsesh.parser.mdwlexer.ToggleType) token.value()));
                    advance();
                    break;
                case START_HIEROGLYPHS:
                    item = new AstStartHieroglyphicText();
                    advance();
                    break;
                case TEXT:
                    item = buildText((AlphabeticText) token.value());
                    advance();
                    break;
                case TEXT_SUPER:
                    item = new AstSuperscript((String) token.value());
                    advance();
                    break;
                case LINE_END:
                    item = new AstLineBreak((Integer) token.value());
                    advance();
                    break;
                case PAGE_END:
                    item = new AstPageBreak();
                    advance();
                    break;
                case TAB_STOP:
                    item = new AstTabStop((Integer) token.value());
                    advance();
                    break;
                case TABBING:
                    advance();
                    item = new AstTabbing(parseOptionList());
                    break;
                case TABBING_CLEAR:
                    item = new AstTabbingClear();
                    advance();
                    break;
                case HRULE: {
                    HRule rule = (HRule) token.value();
                    item = new AstHRule(rule.type(), rule.start(), rule.end());
                    advance();
                    break;
                }
                case ZONE:
                    item = parseZoneStart();
                    break;
                default:
                    if (atCadratStart()) {
                        item = parseCadratWithShading();
                    } else {
                        item = null;
                    }
            }
            if (item == null) {
                break;
            }
            items.add(item);
        }
        if (!at(EOF)) {
            throw error("unexpected or unknown item.");
        }
        return new AstDocument(AstTopItemList.of(items.toArray(new AstNode[0])));
    }

    /// ~~~
    /// optSeparator ::= ε | SEPARATOR
    /// ~~~
    private void skipOptSeparator() {
        if (at(SEPARATOR)) {
            advance();
        }
    }

    private static AstAlphabeticText buildText(AlphabeticText text) {
        return new AstAlphabeticText(text.code(), text.text());
    }

    /**
     * <pre>
     * zoneStart ::= ZONE | ZONE optionList
     * </pre>
     */
    private AstZoneStart parseZoneStart() throws MDCSyntaxError {
        expect(ZONE, "zone");
        AstOptionList options = null;
        if (at(OPEN_BRACE)) {
            options = parseOptionList();
        }
        return new AstZoneStart(options);
    }

    /**
     * <pre>
     * optionList ::= OPENBRACE options CLOSEBRACE
     * options ::= option | options COMMA option
     * option ::= IDENTIFIER | IDENTIFIER EQUAL IDENTIFIER | IDENTIFIER EQUAL INTEGER
     * </pre>
     */
    private AstOptionList parseOptionList() throws MDCSyntaxError {
        expect(OPEN_BRACE, "'['");
        List<AstOption> options = new ArrayList<>();
        while (true) {
            String name = (String) expect(IDENTIFIER, "option name");
            if (at(EQUAL)) {
                advance();
                if (at(IDENTIFIER)) {
                    options.add(AstOption.of(name, (String) token.value()));
                } else if (at(INTEGER)) {
                    options.add(AstOption.of(name, (Integer) token.value()));
                } else {
                    throw error("expected option value");
                }
                advance();
            } else {
                options.add(AstOption.flag(name));
            }
            if (!at(COMMA)) {
                break;
            }
            advance();
        }
        expect(CLOSE_BRACE, "']'");
        return AstOptionList.of(options.toArray(new AstOption[0]));
    }

    // ------------------------------------------------------------------
    // Cadrats
    // ------------------------------------------------------------------

    /**
     * <pre>
     * cadrat optShading
     * optShading ::= ε | SHADING
     * </pre>
     */
    private AstCadrat parseCadratWithShading() throws MDCSyntaxError {
        AstCadrat.Builder builder = AstCadrat.builder();
        parseCadrat(builder);
        if (at(SHADING)) {
            builder.shading(parseShadingDigits((String) token.value()));
            advance();
        }
        return builder.build();
    }

    /**
     * <pre>
     * cadrat ::= verticalStack
     *          | CADRAT BPAR verticalStack EPAR
     *          | CADRAT BPAR verticalStack EPAR optionList
     * </pre>
     */
    private void parseCadrat(AstCadrat.Builder builder) throws MDCSyntaxError {
        if (at(QUADRAT)) {
            advance();
            expect(LPAREN, "'('");
            parseVerticalStack(builder);
            expect(RPAREN, "')'");
            if (at(OPEN_BRACE)) {
                builder.options(parseOptionList());
            }
        } else {
            parseVerticalStack(builder);
        }
    }

    /**
     * <pre>
     * verticalStack ::= horizontalList | verticalStack COLON horizontalList
     * </pre>
     */
    private void parseVerticalStack(AstCadrat.Builder builder) throws MDCSyntaxError {
        builder.hBox(parseHorizontalList());
        while (at(COLON)) {
            advance();
            builder.hBox(parseHorizontalList());
        }
    }

    /**
     * <pre>
     * horizontalList ::= horizontalListElement | horizontalList STAR horizontalListElement
     * </pre>
     */
    private AstHBox parseHorizontalList() throws MDCSyntaxError {
        List<AstHorizontalListElement> elements = new ArrayList<>();
        elements.add(parseHorizontalListElement());
        while (at(STAR)) {
            advance();
            elements.add(parseHorizontalListElement());
        }
        return AstHBox.of(elements.toArray(new AstHorizontalListElement[0]));
    }

    /**
     * <pre>
     * horizontalListElement ::= complexLigature | innerGroup | cartouche
     * complexLigature ::= innerGroup LIGBEFORE hieroglyph LIGAFTER innerGroup
     *                   | innerGroup LIGBEFORE hieroglyph
     *                   | hieroglyph LIGAFTER innerGroup
     * </pre>
     * A leading hieroglyph is parsed first; the next token then tells which
     * construct it starts.
     */
    private AstHorizontalListElement parseHorizontalListElement() throws MDCSyntaxError {
        if (at(BEGIN_CARTOUCHE) || at(BEGIN_OLD_CARTOUCHE)) {
            return parseCartouche();
        }
        AstInnerGroup first;
        if (atHieroglyphStart()) {
            AstHieroglyph h = parseHieroglyph();
            if (at(LIG_AFTER)) {
                advance();
                return new AstComplexLigature(null, h, parseInnerGroup());
            }
            first = continueInnerGroup(h);
        } else {
            first = parseInnerGroup();
        }
        if (at(LIG_BEFORE)) {
            advance();
            AstHieroglyph middle = parseHieroglyph();
            AstInnerGroup after = null;
            if (at(LIG_AFTER)) {
                advance();
                after = parseInnerGroup();
            }
            return new AstComplexLigature(first, middle, after);
        }
        return first;
    }

    /**
     * <pre>
     * innerGroup ::= ligature | hieroglyph | overwrite | philology | subgroup | absoluteGroup
     * </pre>
     */
    private AstInnerGroup parseInnerGroup() throws MDCSyntaxError {
        if (atHieroglyphStart()) {
            return continueInnerGroup(parseHieroglyph());
        } else if (at(BEGIN_PHIL)) {
            return parsePhilology();
        } else if (at(LPAREN)) {
            return parseSubgroup();
        } else {
            throw error("unexpected or unknown item.");
        }
    }

    /**
     * The inner groups which start with a hieroglyph, once it has been read.
     * <pre>
     * ligature ::= hieroglyph (AMP hieroglyph)+
     * absoluteGroup ::= hieroglyph (DOUBLEAMP hieroglyph)+
     * overwrite ::= hieroglyph OVERWRITE hieroglyph
     * </pre>
     */
    private AstInnerGroup continueInnerGroup(AstHieroglyph first) throws MDCSyntaxError {
        switch (token.code()) {
            case AMP: {
                List<AstHieroglyph> signs = new ArrayList<>();
                signs.add(first);
                while (at(AMP)) {
                    advance();
                    signs.add(parseHieroglyph());
                }
                return AstLigature.of(signs.toArray(new AstHieroglyph[0]));
            }
            case DOUBLE_AMP: {
                List<AstHieroglyph> signs = new ArrayList<>();
                signs.add(first);
                while (at(DOUBLE_AMP)) {
                    advance();
                    signs.add(parseHieroglyph());
                }
                return AstAbsoluteGroup.of(signs.toArray(new AstHieroglyph[0]));
            }
            case OVERWRITE:
                advance();
                return new AstOverwrite(first, parseHieroglyph());
            default:
                return first;
        }
    }

    /**
     * <pre>
     * philology ::= BEGINPHIL basicitems optSeparator ENDPHIL
     * </pre>
     */
    private AstPhilology parsePhilology() throws MDCSyntaxError {
        PhilologyKind opening = (PhilologyKind) expect(BEGIN_PHIL, "philology start");
        AstBasicItemList content = parseBasicItems();
        PhilologyKind closing = (PhilologyKind) expect(END_PHIL, "philology end");
        return new AstPhilology(philologySubCode(opening), philologySubCode(closing), content);
    }

    /**
     * <pre>
     * subgroup ::= BPAR basicitems optSeparator EPAR
     * </pre>
     */
    private AstSubCadrat parseSubgroup() throws MDCSyntaxError {
        expect(LPAREN, "'('");
        AstBasicItemList content = parseBasicItems();
        expect(RPAREN, "')'");
        return new AstSubCadrat(content);
    }

    /**
     * <pre>
     * cartouche ::= BEGINCARTOUCHE basicitems optSeparator ENDCARTOUCHE modifiers
     *             | BEGINOLDCARTOUCHE basicitems optSeparator ENDCARTOUCHE
     * </pre>
     * As in the CUP grammar, the modifiers after a cartouche are parsed, but
     * not kept.
     */
    private AstCartouche parseCartouche() throws MDCSyntaxError {
        if (at(BEGIN_CARTOUCHE)) {
            Cartouche start = (Cartouche) token.value();
            advance();
            AstBasicItemList content = parseBasicItems();
            Cartouche end = (Cartouche) expect(END_CARTOUCHE, "end of cartouche");
            parseModifiers();
            return new AstCartouche(start.type(), start.part(), end.part(), content);
        } else {
            OldCartoucheStart start = (OldCartoucheStart) expect(BEGIN_OLD_CARTOUCHE, "cartouche");
            AstBasicItemList content = parseBasicItems();
            expect(END_CARTOUCHE, "end of cartouche");
            int leftPart;
            int rightPart;
            switch (Character.toLowerCase(start.part())) {
                case 'b':
                    leftPart = 1;
                    rightPart = 0;
                    break;
                case 'm':
                    leftPart = 0;
                    rightPart = 0;
                    break;
                case 'e':
                    leftPart = 0;
                    rightPart = 2;
                    break;
                case 'a':
                default:
                    leftPart = 1;
                    rightPart = 2;
            }
            return new AstCartouche(Character.toLowerCase(start.code()), leftPart, rightPart, content);
        }
    }

    /**
     * <pre>
     * basicitems ::= ε | basicitems optSeparator basicitem
     * basicitem ::= cadrat optShading | TEXT | STARTHIEROGLYPHS | TOGGLE
     * </pre>
     * Also consumes the optSeparator which follows basicitems in all rules
     * using it.
     */
    private AstBasicItemList parseBasicItems() throws MDCSyntaxError {
        List<AstNode> items = new ArrayList<>();
        while (true) {
            skipOptSeparator();
            if (at(TEXT)) {
                items.add(buildText((AlphabeticText) token.value()));
                advance();
            } else if (at(START_HIEROGLYPHS)) {
                items.add(new AstStartHieroglyphicText());
                advance();
            } else if (at(TOGGLE)) {
                items.add(new AstToggle(toToggleType((jsesh.parser.mdwlexer.ToggleType) token.value())));
                advance();
            } else if (atCadratStart()) {
                items.add(parseCadratWithShading());
            } else {
                break;
            }
        }
        return AstBasicItemList.of(items.toArray(new AstNode[0]));
    }

    // ------------------------------------------------------------------
    // Signs
    // ------------------------------------------------------------------

    /**
     * <pre>
     * hieroglyph ::= optGrammar HIEROGLYPH modifiers optPos optWordEnd
     * optGrammar ::= ε | GRAMMAR
     * optPos ::= ε | DOUBLELEFTCURLY INTEGER COMMA INTEGER COMMA INTEGER DOUBLERIGHTCURLY
     * optWordEnd ::= ε | WORDEND | SENTENCEEND
     * </pre>
     */
    private AstHieroglyph parseHieroglyph() throws MDCSyntaxError {
        boolean isGrammar = false;
        if (at(GRAMMAR)) {
            isGrammar = true;
            advance();
        }
        MdcSign sign = (MdcSign) expect(HIEROGLYPH, "sign code");
        AstModifierList modifiers = parseModifiers();
        // Default position, as in the CUP grammar.
        int x = 0;
        int y = 0;
        int scale = 100;
        if (at(DOUBLE_LEFT_CURLY)) {
            advance();
            x = (Integer) expect(INTEGER, "integer");
            expect(COMMA, "','");
            y = (Integer) expect(INTEGER, "integer");
            expect(COMMA, "','");
            scale = (Integer) expect(INTEGER, "integer");
            expect(DOUBLE_RIGHT_CURLY, "'}}'");
        }
        WordEndingCode endingCode = WordEndingCode.NONE;
        if (at(WORD_END)) {
            endingCode = WordEndingCode.WORD_END;
            advance();
        } else if (at(SENTENCE_END)) {
            endingCode = WordEndingCode.SENTENCE_END;
            advance();
        }
        return new AstHieroglyph(isGrammar, toSignTypeCode(sign.subtype()), sign.text(), modifiers, endingCode, x, y,
                scale);
    }

    /**
     * <pre>
     * modifiers ::= ε | modifiers MODIFIER
     * </pre>
     */
    private AstModifierList parseModifiers() {
        List<AstModifier> modifiers = new ArrayList<>();
        while (at(MODIFIER)) {
            Modifier modifier = (Modifier) token.value();
            modifiers.add(new AstModifier(modifier.name(), modifier.value()));
            advance();
        }
        return AstModifierList.of(modifiers.toArray(new AstModifier[0]));
    }

    // ------------------------------------------------------------------
    // Lexical value -> model-code translation
    //
    // These depend on jsesh.model.constants, so they live here rather than in
    // jsesh.parser.mdwlexer, which is deliberately self-contained.
    // ------------------------------------------------------------------

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

    /**
     * Decodes a shading marker's digits (e.g. {@code "1234"} from
     * {@code "#1234"}) into an or-combination of the {@code jsesh.model.ShadingCode}
     * bits ({@code TOP_START=1, TOP_END=2, BOTTOM_START=4, BOTTOM_END=8}).
     * Kept as a local copy rather than a call into {@code jsesh.model}: see
     * {@code 00_Documents/documentation/jsesh-package-dependencies.md}, which
     * tracks {@code jsesh.parser <-> jsesh.model} as the one remaining
     * mutual-dependency pair in the module and asks that it not grow.
     */
    private static int parseShadingDigits(String digits) {
        int sh = 0;
        for (int i = 0; i < digits.length(); i++) {
            switch (digits.charAt(i)) {
                case '1' -> sh |= 1;
                case '2' -> sh |= 2;
                case '3' -> sh |= 4;
                case '4' -> sh |= 8;
                default -> {
                }
            }
        }
        return sh;
    }
}
