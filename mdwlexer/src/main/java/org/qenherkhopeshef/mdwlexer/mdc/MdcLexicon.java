package org.qenherkhopeshef.mdwlexer.mdc;

import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.anyCharacter;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.character;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.characterRange;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.difference;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.literal;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.noneOf;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.oneOf;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.oneOrMore;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.optional;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.repeat;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.sequence;
import static org.qenherkhopeshef.mdwlexer.ExpressionBuilder.union;

import java.io.IOException;
import java.io.Reader;

import org.qenherkhopeshef.mdwlexer.Expression;
import org.qenherkhopeshef.mdwlexer.Lexicon;
import org.qenherkhopeshef.mdwlexer.LexiconBuilder;

/// Immutable, compiled lexicon for the Manuel de Codage syntax, translating every rule of the
/// original `MDCLexAux.l` into an [Expression] via [org.qenherkhopeshef.mdwlexer.ExpressionBuilder]
/// and compiling the result down to the DFAs backing [MdcLexer].
///
/// The library's [Lexicon]/[LexiconBuilder] only model a single start state, but
/// the original grammar has two (`YYINITIAL` and `PROPERTIES`, switched with
/// `yybegin`). So this wraps one [Lexicon] per state instead of one shared lexicon;
/// [MdcLexer] drives whichever one is current and switches between them itself. Both
/// disable automatic whitespace skipping ([LexiconBuilder#noWhitespaceSkipping()]): in MDC,
/// runs of whitespace are themselves meaningful tokens (see [MdcTokenType#WORD_END_CANDIDATE]
/// and [MdcTokenType#PROPERTY_WHITESPACE]), not something to skip blindly.
///
/// A handful of rules in the original are genuinely ambiguous with another rule on an
/// equal-length match; where that happens it's called out below, next to the rule it resolves
/// against. Every other ordering choice here is inherited for free from [MdcTokenType]'s
/// declaration order and doesn't need to be justified rule by rule.
///
/// Compiling every rule into its DFA is pure and only needs doing once, so [#instance()] hands
/// out one shared, immutable instance. Use [#newLexer(String)] or [#newLexer(Reader)] to start
/// scanning actual input with it.
public final class MdcLexicon {
    // ---- Character classes shared by several rules (named after the .l file's own macros) ----

    private static final Expression DIGIT = characterRange('0', '9');
    private static final Expression INTEGER = oneOrMore(DIGIT);
    /// `TRUESPACE=([ \t\n\015_])`; `\015` is octal for CR.
    private static final Expression TRUESPACE = oneOf(" \t\n\r_");
    /// `ESPSO=([ \t\n\015_-]*)`: zero or more of [#TRUESPACE] or `-`.
    private static final Expression ESPSO = repeat(union(TRUESPACE, character('-')));
    private static final Expression UPPER = characterRange('A', 'Z');
    private static final Expression LOWER = characterRange('a', 'z');

    // Must come after the character-class constants above: building the lexicons reads them.
    private static final MdcLexicon INSTANCE = new MdcLexicon(buildInitial(), buildProperties());

    private final Lexicon<MdcTokenType> initial;
    private final Lexicon<MdcTokenType> properties;

    private MdcLexicon(Lexicon<MdcTokenType> initial, Lexicon<MdcTokenType> properties) {
        this.initial = initial;
        this.properties = properties;
    }

    /// The shared, immutable MDC lexicon.
    public static MdcLexicon instance() {
        return INSTANCE;
    }

    /// Creates a new stateful lexer scanning `source`.
    public MdcLexer newLexer(String source) {
        return new MdcLexer(this, source);
    }

    /// Creates a new stateful lexer scanning `reader` (read to completion up front; see [MdcLexer]).
    public MdcLexer newLexer(Reader reader) throws IOException {
        return new MdcLexer(this, reader);
    }

    /// The lexicon for the `YYINITIAL` state.
    Lexicon<MdcTokenType> initial() {
        return initial;
    }

    /// The lexicon for the `PROPERTIES` state.
    Lexicon<MdcTokenType> properties() {
        return properties;
    }

    private static Lexicon<MdcTokenType> buildInitial() {
        return LexiconBuilder.<MdcTokenType>newBuilder()
                .noWhitespaceSkipping()
                .rule(MdcTokenType.PAGE_END, sequence(literal("!!"), ESPSO))
                .rule(MdcTokenType.LINE_END, lineEnd())
                .rule(MdcTokenType.TAB_STOP, sequence(character('?'), INTEGER))
                .rule(MdcTokenType.TABBING, character('%'))
                .rule(MdcTokenType.TABBING_CLEAR, literal("%clear"))
                .rule(MdcTokenType.HRULE_THIN, hrule('l'))
                .rule(MdcTokenType.HRULE_WIDE, hrule('L'))
                .rule(MdcTokenType.ALPHABETIC_TEXT, alphabeticText())
                .rule(MdcTokenType.ZONE, literal("zone"))
                .rule(MdcTokenType.QUADRAT, literal("quadrat"))
                .rule(MdcTokenType.START_HIEROGLYPHS, literal("+s"))
                .rule(MdcTokenType.TEXT_SUPER, sequence(character('|'), repeat(union(noneOf("-"), literal("\\-")))))
                .rule(MdcTokenType.WORD_END_CANDIDATE, TRUESPACE)
                .rule(MdcTokenType.SENTENCE_END_CANDIDATE, oneOrMore(sequence(TRUESPACE, TRUESPACE)))
                .rule(MdcTokenType.SEPARATOR, sequence(character('-'), repeat(union(TRUESPACE, character('-')))))
                .rule(MdcTokenType.SHADING_TOGGLE_LITERAL, union(literal("-#-"), literal("-#")))
                // Ambiguous with HASH_ALONE and SHADING on a bare "#-space"-less prefix? No: both
                // alternatives here need a second character ('-' or ' '), so they never tie with
                // HASH_ALONE/SHADING's bare "#" match; declared before SHADING regardless, for a
                // faithful reading of the original's rule order.
                .rule(MdcTokenType.HASH_DASH_OR_SPACES, union(literal("#-"), sequence(character('#'), oneOrMore(character(' ')))))
                // Ties with SHADING on a bare "#" (both match length 1: HASH_ALONE always, SHADING
                // when no 1-4 digit follows). Declared first, exactly as rule 306 precedes rule 321
                // in the original, so a bare "#" resolves to HASH_ALONE like the original does.
                .rule(MdcTokenType.HASH_ALONE, character('#'))
                .rule(MdcTokenType.SHADING_ON, sequence(optional(character('-')), literal("#b")))
                .rule(MdcTokenType.SHADING_OFF, sequence(optional(character('-')), literal("#e")))
                .rule(MdcTokenType.TOGGLE_RED, literal("$r"))
                .rule(MdcTokenType.TOGGLE_BLACK, literal("$b"))
                .rule(MdcTokenType.TOGGLE_LACUNA, character('?'))
                .rule(MdcTokenType.TOGGLE_LINE_LACUNA, literal("??"))
                .rule(MdcTokenType.TOGGLE_OMIT, character('^'))
                .rule(MdcTokenType.TOGGLE_BLACK_RED, character('$'))
                .rule(MdcTokenType.SHADING, shadingDigits())
                .rule(MdcTokenType.COLON, character(':'))
                .rule(MdcTokenType.STAR, character('*'))
                .rule(MdcTokenType.OPEN_BRACE, character('['))
                .rule(MdcTokenType.OLD_CARTOUCHE_PLAIN, character('<'))
                .rule(MdcTokenType.OLD_CARTOUCHE_TYPED_PART, sequence(character('<'), oneOf("SFH"), oneOf("bme")))
                .rule(MdcTokenType.OLD_CARTOUCHE_TYPED, sequence(character('<'), oneOf("SFHG")))
                .rule(MdcTokenType.OLD_CARTOUCHE_PART, sequence(character('<'), oneOf("bme")))
                .rule(MdcTokenType.CARTOUCHE_START_TYPED_PART, sequence(character('<'), oneOf("sfhg"), oneOf("0123")))
                .rule(MdcTokenType.CARTOUCHE_START_TYPED, sequence(character('<'), oneOf("sfhg")))
                .rule(MdcTokenType.CARTOUCHE_START_PART, sequence(character('<'), oneOf("012")))
                .rule(MdcTokenType.CARTOUCHE_END_DEFAULT, character('>'))
                .rule(MdcTokenType.CARTOUCHE_END_TYPED_PART, sequence(oneOf("sfhg"), oneOf("0123"), character('>')))
                .rule(MdcTokenType.CARTOUCHE_END_TYPED, sequence(oneOf("sfhg"), character('>')))
                .rule(MdcTokenType.CARTOUCHE_END_PART, sequence(oneOf("012"), character('>')))
                .rule(MdcTokenType.PHIL_ERASED_SIGNS_BEGIN, literal("[["))
                .rule(MdcTokenType.PHIL_EDITOR_SUPERFLUOUS_BEGIN, literal("[{"))
                .rule(MdcTokenType.PHIL_PREVIOUSLY_READABLE_BEGIN, literal("[\""))
                .rule(MdcTokenType.PHIL_SCRIBE_ADDITION_BEGIN, literal("['"))
                .rule(MdcTokenType.PHIL_EDITOR_ADDITION_BEGIN, literal("[&"))
                .rule(MdcTokenType.PHIL_ERASED_SIGNS_END, literal("]]"))
                .rule(MdcTokenType.PHIL_EDITOR_SUPERFLUOUS_END, literal("}]"))
                .rule(MdcTokenType.PHIL_PREVIOUSLY_READABLE_END, literal("\"]"))
                .rule(MdcTokenType.PHIL_SCRIBE_ADDITION_END, literal("']"))
                .rule(MdcTokenType.PHIL_EDITOR_ADDITION_END, literal("&]"))
                .rule(MdcTokenType.PHIL_MINOR_ADDITION_END, literal(")]"))
                .rule(MdcTokenType.PHIL_MINOR_ADDITION_BEGIN, literal("[("))
                .rule(MdcTokenType.PHIL_DUBIOUS_END, literal("?]"))
                .rule(MdcTokenType.PHIL_DUBIOUS_BEGIN, literal("[?"))
                .rule(MdcTokenType.LPAREN, character('('))
                .rule(MdcTokenType.RPAREN, character(')'))
                .rule(MdcTokenType.OVERWRITE_DOUBLE_HASH, literal("##"))
                .rule(MdcTokenType.AMP, character('&'))
                .rule(MdcTokenType.GRAMMAR_EQUALS, character('='))
                .rule(MdcTokenType.MODIFIER_QUESTION, literal("\\?"))
                // Ties with MODIFIER_GENERIC on e.g. "\R" alone (both length 2). Declared first,
                // exactly as rule 379 precedes rule 380 in the original.
                .rule(MdcTokenType.MODIFIER_R, modifierR())
                .rule(MdcTokenType.MODIFIER_GENERIC, sequence(character('\\'), repeat(union(UPPER, LOWER)), repeat(DIGIT)))
                .rule(MdcTokenType.SIGN_BACKTICK, character('`'))
                .rule(MdcTokenType.SIGN_RED_POINT, character('o'))
                .rule(MdcTokenType.SIGN_BLACK_POINT, character('O'))
                .rule(MdcTokenType.SIGN_SMALL_TEXT, smallText())
                .rule(MdcTokenType.SIGN_CODE, signCode())
                .rule(MdcTokenType.SIGN_HALF_SPACE, character('.'))
                .rule(MdcTokenType.SIGN_FULL_SPACE, literal(".."))
                .rule(MdcTokenType.SIGN_FULL_SHADE, literal("//"))
                .rule(MdcTokenType.SIGN_VERTICAL_SHADE, literal("v/"))
                .rule(MdcTokenType.SIGN_QUARTER_SHADE, character('/'))
                .rule(MdcTokenType.SIGN_HORIZONTAL_SHADE, literal("h/"))
                .rule(MdcTokenType.DOUBLE_AMP, union(literal("&&"), literal("**")))
                .rule(MdcTokenType.LIG_AFTER, literal("&&&"))
                .rule(MdcTokenType.LIG_BEFORE, union(literal("^^^"), literal("^^")))
                .rule(MdcTokenType.DOUBLE_LEFT_CURLY, literal("{{"))
                .rule(MdcTokenType.UNKNOWN, anyCharacter())
                .build();
    }

    private static Lexicon<MdcTokenType> buildProperties() {
        return LexiconBuilder.<MdcTokenType>newBuilder()
                .noWhitespaceSkipping()
                .rule(MdcTokenType.DOUBLE_RIGHT_CURLY, literal("}}"))
                .rule(MdcTokenType.CLOSE_BRACE, character(']'))
                .rule(MdcTokenType.COMMA, character(','))
                .rule(MdcTokenType.EQUAL, character('='))
                .rule(MdcTokenType.PROPERTY_INTEGER, INTEGER)
                .rule(MdcTokenType.PROPERTY_IDENTIFIER, sequence(union(UPPER, LOWER, character('_')),
                        repeat(union(UPPER, LOWER, DIGIT, character('_')))))
                .rule(MdcTokenType.PROPERTY_WHITESPACE, oneOf(" \t\n\r"))
                .rule(MdcTokenType.UNKNOWN, anyCharacter())
                .build();
    }

    /// `"!"("="{INTEGER}"%")?{ESPSO}`
    private static Expression lineEnd() {
        Expression percentage = sequence(character('='), INTEGER, character('%'));
        return sequence(character('!'), optional(percentage), ESPSO);
    }

    /// `"{" type {INTEGER} "," {INTEGER} "}"`
    private static Expression hrule(char type) {
        return sequence(character('{'), character(type), INTEGER, character(','), INTEGER, character('}'));
    }

    /// `"+"[a-rt-z+](\+|[^+]|"+"[^a-z+])*`
    private static Expression alphabeticText() {
        Expression startLetter = union(characterRange('a', 'r'), characterRange('t', 'z'), character('+'));
        Expression escapedPlus = literal("\\+");
        Expression notPlus = noneOf("+");
        Expression plusThenNotLowerOrPlus =
                sequence(character('+'), difference(anyCharacter(), union(LOWER, character('+'))));
        Expression body = repeat(union(escapedPlus, notPlus, plusThenNotLowerOrPlus));
        return sequence(character('+'), startLetter, body);
    }

    /// `"#""1"?"2"?"3"?"4"?`
    private static Expression shadingDigits() {
        return sequence(character('#'), optional(character('1')), optional(character('2')),
                optional(character('3')), optional(character('4')));
    }

    /// `\R"-"?[0-9]*`: the original also has a stray trailing ")" after "[0-9]*", dropped here (see [MdcTokenType#MODIFIER_R]).
    private static Expression modifierR() {
        return sequence(literal("\\R"), optional(character('-')), repeat(DIGIT));
    }

    /// `\"([^\"\\\]]|\\.)*\"`
    private static Expression smallText() {
        Expression bodyChar = union(noneOf("\"\\]"), sequence(character('\\'), anyCharacter()));
        return sequence(character('"'), repeat(bodyChar), character('"'));
    }

    /// `(([A-Z]|"Aa")[0-9]+A*|[@a-zA-Z0-9]+|"@".)`
    private static Expression signCode() {
        Expression gardinerLike = sequence(union(UPPER, literal("Aa")), INTEGER, repeat(character('A')));
        Expression alphanumericRun = oneOrMore(union(character('@'), UPPER, LOWER, DIGIT));
        Expression escapedAny = sequence(character('@'), anyCharacter());
        return union(gardinerLike, alphanumericRun, escapedAny);
    }
}
