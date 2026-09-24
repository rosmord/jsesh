package jsesh.parser.lexer;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;
import java.util.Optional;

import org.qenherkhopeshef.mdwlexer.CharacterClassifier;
import org.qenherkhopeshef.mdwlexer.Lexicon;
import org.qenherkhopeshef.mdwlexer.automata.DeterministicFiniteAutomaton;
import org.qenherkhopeshef.mdwlexer.automata.State;

/// A scanner for the Manuel de Codage syntax.
///
/// There is a main lexicon, and a secondary one which deals with properties,
/// to separate the way numbers and identifiers are dealt with after braces and double curly braces.
///
/// It currently requires a different approach than when we use a single automaton,
/// because we have to share the input between the two automata.
///
/// **Buffering.** The constructor reads its [Reader] to completion up front, unlike
/// [org.qenherkhopeshef.mdwlexer.Lexer] which streams. This is what makes switching DFAs
/// banana-skin-free: two independent stream-backed lexers can't hand off a pushed-back lookahead
/// character to each other, but two scans sharing one code point array and one cursor can. MDC
/// source is small text, so the trade-off is a reasonable one here.
///
/// **`fixExpect`.** In the original, `expectSpace`/`justAfterSign` are
/// updated by a public `fixExpect(Symbol)` method that the surrounding parser was expected
/// to call after every returned symbol. Here that call is folded into [#nextSymbol()]
/// itself, since skipping it was never a legitimate option - one fewer thing for a caller to get
/// wrong.
///
///
/// Not thread-safe. Built with [MdcLexicon#newLexer(String)] / [MdcLexicon#newLexer(Reader)].
public final class MdcLexer {
    private final int[] codePoints;
    private final MdcLexicon lexicon;
    private int position;
    private LexerState state = LexerState.YYINITIAL;

    /// Are philological brackets (`[[...]]` and friends) treated as plain signs?
    private boolean philologyAsSigns;
    /// Is a run of whitespace right now significant (word/sentence end) or just filler?
    private boolean expectSpace;
    /// Did the last emitted symbol denote a sign, making a following `#` an overwrite?
    private boolean justAfterSign;
    /// Kept for parity with the original's field; no rule in this grammar consults it.
    private boolean ignoreStars;
    private boolean debug;

    private enum LexerState {
        YYINITIAL, PROPERTIES
    }

    MdcLexer(MdcLexicon lexicon, Reader reader) throws IOException {
        this(lexicon, readAll(Objects.requireNonNull(reader, "reader")));
    }

    MdcLexer(MdcLexicon lexicon, String source) {
        this.lexicon = Objects.requireNonNull(lexicon, "lexicon");
        this.codePoints = Objects.requireNonNull(source, "source").codePoints().toArray();
    }

    /// The number of code points consumed so far, i.e. where the next symbol starts.
    public int position() {
        return position;
    }

    public boolean isPhilologyAsSigns() {
        return philologyAsSigns;
    }

    /// Choose whether philological parentheses are read back as plain signs.
    public void setPhilologyAsSigns(boolean philologyAsSigns) {
        this.philologyAsSigns = philologyAsSigns;
    }

    public boolean isIgnoreStars() {
        return ignoreStars;
    }

    public void setIgnoreStars(boolean ignoreStars) {
        this.ignoreStars = ignoreStars;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /// Resets scanning flags and returns to the initial state, without moving [#position()].
    public void reset() {
        ignoreStars = false;
        expectSpace = false;
        justAfterSign = false;
        state = LexerState.YYINITIAL;
    }

    /// Reads the next symbol, or [Optional#empty()] at end of input (corresponding to the
    /// original's `EOF` symbol). Silently-discarded matches (insignificant whitespace, the
    /// `PROPERTIES`-state layout whitespace) are skipped internally; a caller never sees them.
    public Optional<MdcSymbol> nextSymbol() {
        while (position < codePoints.length) {
            Lexicon<MdcTokenType> current = state == LexerState.PROPERTIES ? lexicon.properties() : lexicon.initial();
            RawMatch match = scanRaw(current).orElseThrow(() -> new IllegalStateException(
                    "No rule matched at position " + position + " (unreachable: UNKNOWN matches any character)"));
            int start = position;
            position += match.length();
            printDebug(match.type(), match.text());

            Optional<MdcSymbol> symbol = interpret(match.type(), match.text(), start);
            if (symbol.isPresent()) {
                fixExpect(symbol.get());
                return symbol;
            }
        }
        return Optional.empty();
    }

    /// Mirrors the original `fixExpect(Symbol)`, driven by the emitted symbol's code.
    private void fixExpect(MdcSymbol symbol) {
        expectSpace = false;
        justAfterSign = false;
        switch (symbol.code()) {
            case HIEROGLYPH, MODIFIER, DOUBLE_RIGHT_CURLY -> expectSpace = true;
            default -> {
            }
        }
        if (symbol.code() == MdcSymbolCode.HIEROGLYPH) {
            justAfterSign = true;
        }
    }

    /// Reproduces each rule's action block, in the same order as `MDCLexAux.l`.
    private Optional<MdcSymbol> interpret(MdcTokenType type, String text, int start) {
        return switch (type) {
            case PAGE_END -> emit(MdcSymbolCode.PAGE_END, null, text, start);
            case LINE_END -> emit(MdcSymbolCode.LINE_END, extractLineSkip(text), text, start);
            case TAB_STOP -> emit(MdcSymbolCode.TAB_STOP, Integer.parseInt(text.substring(1)), text, start);
            case TABBING -> emit(MdcSymbolCode.TABBING, null, text, start);
            case TABBING_CLEAR -> emit(MdcSymbolCode.TABBING_CLEAR, null, text, start);
            case HRULE_THIN -> emit(MdcSymbolCode.HRULE, parseHRule('l', text), text, start);
            case HRULE_WIDE -> emit(MdcSymbolCode.HRULE, parseHRule('L', text), text, start);
            case ALPHABETIC_TEXT -> emit(MdcSymbolCode.TEXT, parseAlphabeticText(text), text, start);
            case ZONE -> emit(MdcSymbolCode.ZONE, null, text, start);
            case QUADRAT -> emit(MdcSymbolCode.QUADRAT, null, text, start);
            case START_HIEROGLYPHS -> emit(MdcSymbolCode.START_HIEROGLYPHS, null, text, start);
            case TEXT_SUPER -> emit(MdcSymbolCode.TEXT_SUPER, text.substring(1), text, start);
            case WORD_END_CANDIDATE ->
                    expectSpace ? emit(MdcSymbolCode.WORD_END, null, text, start) : Optional.empty();
            case SENTENCE_END_CANDIDATE ->
                    expectSpace ? emit(MdcSymbolCode.SENTENCE_END, null, text, start) : Optional.empty();
            case SEPARATOR -> emit(MdcSymbolCode.SEPARATOR, null, text, start);
            case SHADING_TOGGLE_LITERAL -> emit(MdcSymbolCode.TOGGLE, ToggleType.SHADING_TOGGLE, text, start);
            case HASH_DASH_OR_SPACES -> justAfterSign
                    ? emit(MdcSymbolCode.SHADING, "#1234", text, start)
                    : emit(MdcSymbolCode.TOGGLE, ToggleType.SHADING_TOGGLE, text, start);
            case HASH_ALONE -> justAfterSign
                    ? emit(MdcSymbolCode.OVERWRITE, null, text, start)
                    : emit(MdcSymbolCode.TOGGLE, ToggleType.SHADING_TOGGLE, text, start);
            case SHADING_ON -> emit(MdcSymbolCode.TOGGLE, ToggleType.SHADING_ON, text, start);
            case SHADING_OFF -> emit(MdcSymbolCode.TOGGLE, ToggleType.SHADING_OFF, text, start);
            case TOGGLE_RED -> emit(MdcSymbolCode.TOGGLE, ToggleType.RED, text, start);
            case TOGGLE_BLACK -> emit(MdcSymbolCode.TOGGLE, ToggleType.BLACK, text, start);
            case TOGGLE_LACUNA -> emit(MdcSymbolCode.TOGGLE, ToggleType.LACUNA, text, start);
            case TOGGLE_LINE_LACUNA -> emit(MdcSymbolCode.TOGGLE, ToggleType.LINE_LACUNA, text, start);
            case TOGGLE_OMIT -> emit(MdcSymbolCode.TOGGLE, ToggleType.OMIT, text, start);
            case TOGGLE_BLACK_RED -> emit(MdcSymbolCode.TOGGLE, ToggleType.BLACK_RED, text, start);
            case SHADING -> emit(MdcSymbolCode.SHADING, text, text, start);
            case COLON -> emit(MdcSymbolCode.COLON, null, text, start);
            case STAR -> emit(MdcSymbolCode.STAR, null, text, start);
            case OPEN_BRACE -> {
                state = LexerState.PROPERTIES;
                yield emit(MdcSymbolCode.OPEN_BRACE, null, text, start);
            }
            case OLD_CARTOUCHE_PLAIN -> emit(MdcSymbolCode.BEGIN_OLD_CARTOUCHE, new OldCartoucheStart('c', 'a'), text, start);
            case OLD_CARTOUCHE_TYPED_PART ->
                    emit(MdcSymbolCode.BEGIN_OLD_CARTOUCHE, new OldCartoucheStart(text.charAt(1), text.charAt(2)), text, start);
            case OLD_CARTOUCHE_TYPED ->
                    emit(MdcSymbolCode.BEGIN_OLD_CARTOUCHE, new OldCartoucheStart(text.charAt(1), 'a'), text, start);
            case OLD_CARTOUCHE_PART ->
                    emit(MdcSymbolCode.BEGIN_OLD_CARTOUCHE, new OldCartoucheStart('c', text.charAt(1)), text, start);
            case CARTOUCHE_START_TYPED_PART ->
                    emit(MdcSymbolCode.BEGIN_CARTOUCHE, new Cartouche(text.charAt(1), text.charAt(2) - '0'), text, start);
            case CARTOUCHE_START_TYPED -> emit(MdcSymbolCode.BEGIN_CARTOUCHE, new Cartouche(text.charAt(1), 1), text, start);
            case CARTOUCHE_START_PART ->
                    emit(MdcSymbolCode.BEGIN_CARTOUCHE, new Cartouche('c', text.charAt(1) - '0'), text, start);
            case CARTOUCHE_END_DEFAULT -> emit(MdcSymbolCode.END_CARTOUCHE, new Cartouche('c', 2), text, start);
            case CARTOUCHE_END_TYPED_PART ->
                    emit(MdcSymbolCode.END_CARTOUCHE, new Cartouche(text.charAt(0), text.charAt(1) - '0'), text, start);
            case CARTOUCHE_END_TYPED -> emit(MdcSymbolCode.END_CARTOUCHE, new Cartouche(text.charAt(0), 2), text, start);
            case CARTOUCHE_END_PART -> emit(MdcSymbolCode.END_CARTOUCHE, new Cartouche('c', text.charAt(0) - '0'), text, start);
            case PHIL_ERASED_SIGNS_BEGIN -> philology(PhilologyKind.ERASED_SIGNS, true, text, start);
            case PHIL_ERASED_SIGNS_END -> philology(PhilologyKind.ERASED_SIGNS, false, text, start);
            case PHIL_EDITOR_SUPERFLUOUS_BEGIN -> philology(PhilologyKind.EDITOR_SUPERFLUOUS, true, text, start);
            case PHIL_EDITOR_SUPERFLUOUS_END -> philology(PhilologyKind.EDITOR_SUPERFLUOUS, false, text, start);
            case PHIL_PREVIOUSLY_READABLE_BEGIN -> philology(PhilologyKind.PREVIOUSLY_READABLE, true, text, start);
            case PHIL_PREVIOUSLY_READABLE_END -> philology(PhilologyKind.PREVIOUSLY_READABLE, false, text, start);
            case PHIL_SCRIBE_ADDITION_BEGIN -> philology(PhilologyKind.SCRIBE_ADDITION, true, text, start);
            case PHIL_SCRIBE_ADDITION_END -> philology(PhilologyKind.SCRIBE_ADDITION, false, text, start);
            case PHIL_EDITOR_ADDITION_BEGIN -> philology(PhilologyKind.EDITOR_ADDITION, true, text, start);
            case PHIL_EDITOR_ADDITION_END -> philology(PhilologyKind.EDITOR_ADDITION, false, text, start);
            case PHIL_MINOR_ADDITION_END -> philology(PhilologyKind.MINOR_ADDITION, false, text, start);
            case PHIL_MINOR_ADDITION_BEGIN -> philology(PhilologyKind.MINOR_ADDITION, true, text, start);
            case PHIL_DUBIOUS_END -> philology(PhilologyKind.DUBIOUS, false, text, start);
            case PHIL_DUBIOUS_BEGIN -> philology(PhilologyKind.DUBIOUS, true, text, start);
            case LPAREN -> emit(MdcSymbolCode.LPAREN, null, text, start);
            case RPAREN -> emit(MdcSymbolCode.RPAREN, null, text, start);
            case OVERWRITE_DOUBLE_HASH -> emit(MdcSymbolCode.OVERWRITE, null, text, start);
            case AMP -> emit(MdcSymbolCode.AMP, null, text, start);
            case GRAMMAR_EQUALS -> emit(MdcSymbolCode.GRAMMAR, null, text, start);
            case MODIFIER_QUESTION, MODIFIER_R, MODIFIER_GENERIC ->
                    emit(MdcSymbolCode.MODIFIER, parseModifier(text), text, start);
            case SIGN_BACKTICK, SIGN_CODE -> sign(SignSubType.Plain.MDC_CODE, text, start);
            case SIGN_RED_POINT -> sign(SignSubType.Plain.RED_POINT, text, start);
            case SIGN_BLACK_POINT -> sign(SignSubType.Plain.BLACK_POINT, text, start);
            case SIGN_SMALL_TEXT -> sign(SignSubType.Plain.SMALL_TEXT, text, start);
            case SIGN_HALF_SPACE -> sign(SignSubType.Plain.HALF_SPACE, text, start);
            case SIGN_FULL_SPACE -> sign(SignSubType.Plain.FULL_SPACE, text, start);
            case SIGN_FULL_SHADE -> sign(SignSubType.Plain.FULL_SHADE, text, start);
            case SIGN_VERTICAL_SHADE -> sign(SignSubType.Plain.VERTICAL_SHADE, text, start);
            case SIGN_QUARTER_SHADE -> sign(SignSubType.Plain.QUARTER_SHADE, text, start);
            case SIGN_HORIZONTAL_SHADE -> sign(SignSubType.Plain.HORIZONTAL_SHADE, text, start);
            case DOUBLE_AMP -> emit(MdcSymbolCode.DOUBLE_AMP, null, text, start);
            case LIG_AFTER -> emit(MdcSymbolCode.LIG_AFTER, null, text, start);
            case LIG_BEFORE -> emit(MdcSymbolCode.LIG_BEFORE, null, text, start);
            case DOUBLE_LEFT_CURLY -> {
                state = LexerState.PROPERTIES;
                yield emit(MdcSymbolCode.DOUBLE_LEFT_CURLY, null, text, start);
            }
            case DOUBLE_RIGHT_CURLY -> {
                state = LexerState.YYINITIAL;
                yield emit(MdcSymbolCode.DOUBLE_RIGHT_CURLY, null, text, start);
            }
            case CLOSE_BRACE -> {
                state = LexerState.YYINITIAL;
                yield emit(MdcSymbolCode.CLOSE_BRACE, null, text, start);
            }
            case COMMA -> emit(MdcSymbolCode.COMMA, null, text, start);
            case EQUAL -> emit(MdcSymbolCode.EQUAL, null, text, start);
            case PROPERTY_INTEGER -> emit(MdcSymbolCode.INTEGER, Integer.parseInt(text), text, start);
            case PROPERTY_IDENTIFIER -> emit(MdcSymbolCode.IDENTIFIER, text, text, start);
            case PROPERTY_WHITESPACE -> Optional.empty();
            case UNKNOWN -> emit(MdcSymbolCode.UNKNOWN, text, text, start);
        };
    }

    private Optional<MdcSymbol> philology(PhilologyKind kind, boolean begin, String text, int start) {
        if (philologyAsSigns) {
            return emit(MdcSymbolCode.HIEROGLYPH, new MdcSign(new SignSubType.Philology(kind, begin), text), text, start);
        }
        return emit(begin ? MdcSymbolCode.BEGIN_PHIL : MdcSymbolCode.END_PHIL, kind, text, start);
    }

    private Optional<MdcSymbol> sign(SignSubType.Plain subtype, String text, int start) {
        return emit(MdcSymbolCode.HIEROGLYPH, new MdcSign(subtype, text), text, start);
    }

    private static Optional<MdcSymbol> emit(MdcSymbolCode code, Object value, String text, int start) {
        return Optional.of(new MdcSymbol(code, value, text, start));
    }

    /// `buildHRule`: parses `"{" type start "," end "}"`.
    private static HRule parseHRule(char type, String text) {
        int commaIndex = text.indexOf(',');
        int start = Integer.parseInt(text.substring(2, commaIndex));
        int end = Integer.parseInt(text.substring(commaIndex + 1, text.indexOf('}')));
        return new HRule(type, start, end);
    }

    /// The action for [MdcTokenType#MODIFIER_QUESTION]/[MdcTokenType#MODIFIER_R]/
    /// [MdcTokenType#MODIFIER_GENERIC]: `name` is the letters (or `?`) right after
    /// the leading `\`, `value` is the optional trailing integer.
    private static Modifier parseModifier(String text) {
        int endPos = 1; // Skip the initial "\"
        char c;
        while (endPos < text.length()
                && (Character.isLetter(c = text.charAt(endPos)) || c == '?')) {
            endPos++;
        }
        String name = text.substring(1, endPos);
        Integer value = endPos < text.length() ? Integer.parseInt(text.substring(endPos)) : null;
        return new Modifier(name, value);
    }

    /// The action for [MdcTokenType#ALPHABETIC_TEXT]: resolves `\+`/`\\` escapes.
    private static AlphabeticText parseAlphabeticText(String text) {
        String body = text.length() > 2 ? text.substring(2) : "";
        body = body.replaceAll("\\\\\\+", "+").replaceAll("\\\\\\\\", "\\\\");
        return new AlphabeticText(text.charAt(1), body);
    }

    /// `extractIntFromLineSkip`: percentage of normal line skip, defaulting to 100.
    private static int extractLineSkip(String text) {
        int equalsIndex = text.indexOf('=');
        if (equalsIndex == -1) {
            return 100;
        }
        return Integer.parseInt(text.substring(equalsIndex + 1, text.indexOf('%')));
    }

    private void printDebug(MdcTokenType type, String text) {
        if (debug) {
            System.err.println("token: " + type + " " + text);
        }
    }

    /// One raw rule match: which [MdcTokenType] rule, its text, and its length in code points.
    private record RawMatch(MdcTokenType type, String text, int length) {
    }

    /// Longest-prefix match of `lexicon` against the input, starting at [#position].
    private Optional<RawMatch> scanRaw(Lexicon<MdcTokenType> lexicon) {
        DeterministicFiniteAutomaton<Integer, MdcTokenType> automaton = lexicon.automaton();
        CharacterClassifier classifier = lexicon.classifier();
        State state = automaton.initialState();
        int acceptedLength = -1;
        MdcTokenType acceptedType = null;

        int index = position;
        while (index < codePoints.length) {
            Optional<State> next = automaton.transition(state, classifier.classOf(codePoints[index]));
            if (next.isEmpty()) {
                break;
            }
            state = next.get();
            index++;
            Optional<MdcTokenType> tokenType = automaton.acceptingTokenType(state);
            if (tokenType.isPresent()) {
                acceptedLength = index - position;
                acceptedType = tokenType.get();
            }
        }
        if (acceptedType == null) {
            return Optional.empty();
        }
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < acceptedLength; i++) {
            text.appendCodePoint(codePoints[position + i]);
        }
        return Optional.of(new RawMatch(acceptedType, text.toString(), acceptedLength));
    }

    private static String readAll(Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[4096];
        int read;
        while ((read = reader.read(buffer)) != -1) {
            builder.append(buffer, 0, read);
        }
        return builder.toString();
    }
}
