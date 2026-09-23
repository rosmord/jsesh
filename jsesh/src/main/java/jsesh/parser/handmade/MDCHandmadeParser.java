package jsesh.parser.handmade;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import java_cup.runtime.Symbol;
import jsesh.model.constants.ToggleType;
import jsesh.model.constants.WordEndingCode;
import jsesh.parser.MDCSyntaxError;
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
import jsesh.parser.lex.MDCAlphabeticText;
import jsesh.parser.lex.MDCCartouche;
import jsesh.parser.lex.MDCHRule;
import jsesh.parser.lex.MDCLex;
import jsesh.parser.lex.MDCModifier;
import jsesh.parser.lex.MDCShading;
import jsesh.parser.lex.MDCSign;
import jsesh.parser.lex.MDCStartOldCartouche;
import jsesh.parser.lex.MDCSubType;

import static jsesh.parser.lex.MDCSymbols.*;

/**
 * Hand-written recursive descent parser for Manuel de Codage text, building
 * the {@link jsesh.parser.ast} tree directly (see {@link #parse(String)}).
 * <p>It replaced an earlier CUP-generated parser
 * ({@code jsesh/src/jcup/MDCParse.y}), checked equivalent construct-by-
 * construct before that grammar was retired. It reuses the {@link MDCLex}
 * lexer adapter unchanged, itself now backed by the hand-written
 * {@link jsesh.parser.mdwlexer.MdcLexer} rather than the retired
 * JFlex-generated {@code MDCLexAux}. The grammar is LALR(1) without conflicts, and
 * its left-recursive rules are all plain lists, so one token of lookahead is
 * enough; each method below documents the grammar rule it implements.
 * <p>Any syntax error aborts the parse with an {@link MDCSyntaxError}: there
 * is no error recovery.
 * <p>A parser object is not thread-safe, but can be reused for successive
 * parses.
 *
 * @author rosmord
 */
public class MDCHandmadeParser {

    private boolean debug = false;
    private boolean philologyAsSigns = true;

    private MDCLex lexer;

    /**
     * The current lookahead token.
     */
    private Symbol token;

    public AstDocument parse(String text) throws MDCSyntaxError {
        return parse(new StringReader(text));
    }

    public AstDocument parse(Reader in) throws MDCSyntaxError {
        lexer = new MDCLex(in);
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
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * if true, philological markers, such as [[ and ]], are considered
     * as simple signs, and not as constructs. Default is true, as in
     * {@link jsesh.parser.MDCParserFacade}.
     * @return true if philological markers are considered as simple signs.
     */
    public boolean isPhilologyAsSigns() {
        return philologyAsSigns;
    }

    public void setPhilologyAsSigns(boolean philologyAsSigns) {
        this.philologyAsSigns = philologyAsSigns;
    }

    // ------------------------------------------------------------------
    // Token handling
    // ------------------------------------------------------------------

    private void advance() throws IOException {
        token = lexer.next_token();
    }

    private boolean at(int sym) {
        return token.sym == sym;
    }

    /**
     * Checks that the current token is of the given type, consumes it,
     * and returns its value.
     */
    private Object expect(int sym, String what) throws IOException, MDCSyntaxError {
        if (!at(sym)) {
            throw error("expected " + what);
        }
        Object value = token.value;
        advance();
        return value;
    }

    private MDCSyntaxError error(String message) {
        return lexer.buildError(message);
    }

    private boolean atHieroglyphStart() {
        return at(GRAMMAR) || at(HIEROGLYPH);
    }

    private boolean atCadratStart() {
        return atHieroglyphStart() || at(BEGINPHIL) || at(BPAR)
                || at(BEGINCARTOUCHE) || at(BEGINOLDCARTOUCHE) || at(CADRAT);
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
    private AstDocument parseMdcFile() throws IOException, MDCSyntaxError {
        List<AstNode> items = new ArrayList<>();
        while (true) {
            skipOptSeparator();
            AstNode item;
            switch (token.sym) {
                case TOGGLE:
                    item = new AstToggle((ToggleType) token.value);
                    advance();
                    break;
                case STARTHIEROGLYPHS:
                    item = new AstStartHieroglyphicText();
                    advance();
                    break;
                case TEXT:
                    item = buildText((MDCAlphabeticText) token.value);
                    advance();
                    break;
                case TEXTSUPER:
                    item = new AstSuperscript((String) token.value);
                    advance();
                    break;
                case LINEEND:
                    item = new AstLineBreak((Integer) token.value);
                    advance();
                    break;
                case PAGEEND:
                    item = new AstPageBreak();
                    advance();
                    break;
                case TABSTOP:
                    item = new AstTabStop((Integer) token.value);
                    advance();
                    break;
                case TABBING:
                    advance();
                    item = new AstTabbing(parseOptionList());
                    break;
                case TABBINGCLEAR:
                    item = new AstTabbingClear();
                    advance();
                    break;
                case HRULE: {
                    MDCHRule rule = (MDCHRule) token.value;
                    item = new AstHRule(rule.getLineType(), rule.getStartPos(), rule.getEndPos());
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

    /**
     * <pre>
     * optSeparator ::= ε | SEPARATOR
     * </pre>
     */
    private void skipOptSeparator() throws IOException {
        if (at(SEPARATOR)) {
            advance();
        }
    }

    private static AstAlphabeticText buildText(MDCAlphabeticText text) {
        return new AstAlphabeticText(text.getScriptCode(), text.getText());
    }

    /**
     * <pre>
     * zoneStart ::= ZONE | ZONE optionList
     * </pre>
     */
    private AstZoneStart parseZoneStart() throws IOException, MDCSyntaxError {
        expect(ZONE, "zone");
        AstOptionList options = null;
        if (at(OPENBRACE)) {
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
    private AstOptionList parseOptionList() throws IOException, MDCSyntaxError {
        expect(OPENBRACE, "'['");
        List<AstOption> options = new ArrayList<>();
        while (true) {
            String name = (String) expect(IDENTIFIER, "option name");
            if (at(EQUAL)) {
                advance();
                if (at(IDENTIFIER)) {
                    options.add(AstOption.of(name, (String) token.value));
                } else if (at(INTEGER)) {
                    options.add(AstOption.of(name, (Integer) token.value));
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
        expect(CLOSEBRACE, "']'");
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
    private AstCadrat parseCadratWithShading() throws IOException, MDCSyntaxError {
        AstCadrat.Builder builder = AstCadrat.builder();
        parseCadrat(builder);
        if (at(SHADING)) {
            builder.shading(((MDCShading) token.value).getShading());
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
    private void parseCadrat(AstCadrat.Builder builder) throws IOException, MDCSyntaxError {
        if (at(CADRAT)) {
            advance();
            expect(BPAR, "'('");
            parseVerticalStack(builder);
            expect(EPAR, "')'");
            if (at(OPENBRACE)) {
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
    private void parseVerticalStack(AstCadrat.Builder builder) throws IOException, MDCSyntaxError {
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
    private AstHBox parseHorizontalList() throws IOException, MDCSyntaxError {
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
    private AstHorizontalListElement parseHorizontalListElement() throws IOException, MDCSyntaxError {
        if (at(BEGINCARTOUCHE) || at(BEGINOLDCARTOUCHE)) {
            return parseCartouche();
        }
        AstInnerGroup first;
        if (atHieroglyphStart()) {
            AstHieroglyph h = parseHieroglyph();
            if (at(LIGAFTER)) {
                advance();
                return new AstComplexLigature(null, h, parseInnerGroup());
            }
            first = continueInnerGroup(h);
        } else {
            first = parseInnerGroup();
        }
        if (at(LIGBEFORE)) {
            advance();
            AstHieroglyph middle = parseHieroglyph();
            AstInnerGroup after = null;
            if (at(LIGAFTER)) {
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
    private AstInnerGroup parseInnerGroup() throws IOException, MDCSyntaxError {
        if (atHieroglyphStart()) {
            return continueInnerGroup(parseHieroglyph());
        } else if (at(BEGINPHIL)) {
            return parsePhilology();
        } else if (at(BPAR)) {
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
    private AstInnerGroup continueInnerGroup(AstHieroglyph first) throws IOException, MDCSyntaxError {
        switch (token.sym) {
            case AMP: {
                List<AstHieroglyph> signs = new ArrayList<>();
                signs.add(first);
                while (at(AMP)) {
                    advance();
                    signs.add(parseHieroglyph());
                }
                return AstLigature.of(signs.toArray(new AstHieroglyph[0]));
            }
            case DOUBLEAMP: {
                List<AstHieroglyph> signs = new ArrayList<>();
                signs.add(first);
                while (at(DOUBLEAMP)) {
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
    private AstPhilology parsePhilology() throws IOException, MDCSyntaxError {
        MDCSubType opening = (MDCSubType) expect(BEGINPHIL, "philology start");
        AstBasicItemList content = parseBasicItems();
        MDCSubType closing = (MDCSubType) expect(ENDPHIL, "philology end");
        return new AstPhilology(opening.getSubType(), closing.getSubType(), content);
    }

    /**
     * <pre>
     * subgroup ::= BPAR basicitems optSeparator EPAR
     * </pre>
     */
    private AstSubCadrat parseSubgroup() throws IOException, MDCSyntaxError {
        expect(BPAR, "'('");
        AstBasicItemList content = parseBasicItems();
        expect(EPAR, "')'");
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
    private AstCartouche parseCartouche() throws IOException, MDCSyntaxError {
        if (at(BEGINCARTOUCHE)) {
            MDCCartouche start = (MDCCartouche) token.value;
            advance();
            AstBasicItemList content = parseBasicItems();
            MDCCartouche end = (MDCCartouche) expect(ENDCARTOUCHE, "end of cartouche");
            parseModifiers();
            return new AstCartouche(start.getCartoucheType(), start.getPart(), end.getPart(), content);
        } else {
            MDCStartOldCartouche start = (MDCStartOldCartouche) expect(BEGINOLDCARTOUCHE, "cartouche");
            AstBasicItemList content = parseBasicItems();
            expect(ENDCARTOUCHE, "end of cartouche");
            int leftPart;
            int rightPart;
            switch (start.getPart()) {
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
            return new AstCartouche(start.getCartoucheType(), leftPart, rightPart, content);
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
    private AstBasicItemList parseBasicItems() throws IOException, MDCSyntaxError {
        List<AstNode> items = new ArrayList<>();
        while (true) {
            skipOptSeparator();
            if (at(TEXT)) {
                items.add(buildText((MDCAlphabeticText) token.value));
                advance();
            } else if (at(STARTHIEROGLYPHS)) {
                items.add(new AstStartHieroglyphicText());
                advance();
            } else if (at(TOGGLE)) {
                items.add(new AstToggle((ToggleType) token.value));
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
    private AstHieroglyph parseHieroglyph() throws IOException, MDCSyntaxError {
        boolean isGrammar = false;
        if (at(GRAMMAR)) {
            isGrammar = true;
            advance();
        }
        MDCSign sign = (MDCSign) expect(HIEROGLYPH, "sign code");
        AstModifierList modifiers = parseModifiers();
        // Default position, as in the CUP grammar.
        int x = 0;
        int y = 0;
        int scale = 100;
        if (at(DOUBLELEFTCURLY)) {
            advance();
            x = (Integer) expect(INTEGER, "integer");
            expect(COMMA, "','");
            y = (Integer) expect(INTEGER, "integer");
            expect(COMMA, "','");
            scale = (Integer) expect(INTEGER, "integer");
            expect(DOUBLERIGHTCURLY, "'}}'");
        }
        WordEndingCode endingCode = WordEndingCode.NONE;
        if (at(WORDEND)) {
            endingCode = WordEndingCode.WORD_END;
            advance();
        } else if (at(SENTENCEEND)) {
            endingCode = WordEndingCode.SENTENCE_END;
            advance();
        }
        return new AstHieroglyph(isGrammar, sign.getType(), sign.getString(), modifiers, endingCode, x, y,
                scale);
    }

    /**
     * <pre>
     * modifiers ::= ε | modifiers MODIFIER
     * </pre>
     */
    private AstModifierList parseModifiers() throws IOException {
        List<AstModifier> modifiers = new ArrayList<>();
        while (at(MODIFIER)) {
            MDCModifier modifier = (MDCModifier) token.value;
            modifiers.add(new AstModifier(modifier.getName(), modifier.getIntValue()));
            advance();
        }
        return AstModifierList.of(modifiers.toArray(new AstModifier[0]));
    }
}
