package jsesh.model;

import java.util.ArrayList;
import java.util.List;

import jsesh.model.constants.ScriptCode;
import jsesh.model.transliteration.TransliterationEncoding;
import jsesh.model.transliteration.TransliterationUtilities;

/// One character of alphabetic text (latin, transliteration, coptic, greek...).
///
/// In the Manuel de Codage, alphabetic text is written as a block, e.g.
/// `+lsome text+s`. In the model, each character is a separate element, so
/// that the caret can move inside the text and the text can be edited letter
/// by letter. Writers and exporters gather consecutive characters back into
/// runs (see [jsesh.model.tools.AlphabeticRuns]).
///
/// For transliteration, the character is the MdC letter (e.g. `x` for ḫ), and
/// [#isUppercase()] tells whether it is the uppercase form: MdC `^x` is *one*
/// character. For other scripts, the case is part of the code point itself, and
/// [#isUppercase()] is always false.
///
/// Instances are immutable, apart from their [TopItemState].
///
/// @author rosmord
public final class AlphabeticCharacter extends BasicItem {

    private static final long serialVersionUID = -2410582279264918217L;

    private final ScriptCode script;

    /// The actual MdC letter for the script. Only differs from
    /// `script.getMdcCode()` for [ScriptCode#OTHER].
    private final char mdcScriptCode;

    private final int codePoint;

    private final boolean uppercase;

    /// Creates a character from its MdC script letter.
    ///
    /// @param mdcScriptCode the letter after `+` in the MdC text (e.g. `l`,
    /// `t`, or an unknown letter, which is kept as is).
    /// @param codePoint the character. For transliteration, the MdC letter
    /// (`x`, `A`...), without the `^` marker.
    /// @param uppercase uppercase transliteration; ignored (forced to false) for
    /// other scripts.
    public AlphabeticCharacter(char mdcScriptCode, int codePoint, boolean uppercase) {
        this.script = ScriptCode.forMdcCode(mdcScriptCode);
        this.mdcScriptCode = mdcScriptCode;
        this.codePoint = codePoint;
        this.uppercase = uppercase && script == ScriptCode.TRANSLITERATION;
    }

    /// Creates a character in a known script.
    ///
    /// @param script the script; not [ScriptCode#OTHER], which needs an actual
    /// letter (use [#AlphabeticCharacter(char, int, boolean)]).
    /// @param codePoint the character.
    public AlphabeticCharacter(ScriptCode script, int codePoint) {
        this(script.getMdcCode(), codePoint, false);
    }

    /// Splits the content of a MdC text block into characters.
    ///
    /// - For transliteration (`t`), the non-standard "Utrecht font" encoding
    ///   is first converted into MdC (see
    ///   [TransliterationUtilities#convertWindowsTranslitteration(String)]),
    ///   then `^` is folded into the following letter; a trailing lone `^` is
    ///   dropped.
    /// - Line breaks and tabulations become spaces.
    ///
    /// @param mdcScriptCode the letter after `+` (not `+`: comments are
    /// [MdcComment]s).
    /// @param text the text, with MdC escapes already resolved.
    /// @return the characters, in order.
    public static List<AlphabeticCharacter> fromMdcText(char mdcScriptCode, String text) {
        List<AlphabeticCharacter> result = new ArrayList<>();
        boolean transliteration = ScriptCode.forMdcCode(mdcScriptCode) == ScriptCode.TRANSLITERATION;
        if (transliteration) {
            text = TransliterationUtilities.convertWindowsTranslitteration(text);
        }
        boolean nextIsUpper = false;
        for (int i = 0; i < text.length();) {
            int c = text.codePointAt(i);
            i += Character.charCount(c);
            if (transliteration && c == '^') {
                nextIsUpper = true;
                continue;
            }
            if (c == '\n' || c == '\r' || c == '\t') {
                c = ' ';
            }
            result.add(new AlphabeticCharacter(mdcScriptCode, c, nextIsUpper));
            nextIsUpper = false;
        }
        return result;
    }

    public ScriptCode getScript() {
        return script;
    }

    /// The MdC letter for this character's script (the original letter for
    /// [ScriptCode#OTHER]).
    public char getMdcScriptCode() {
        return mdcScriptCode;
    }

    public int getCodePoint() {
        return codePoint;
    }

    /// Is this an uppercase transliteration letter (MdC `^x`)?
    /// Always false for other scripts.
    public boolean isUppercase() {
        return uppercase;
    }

    /// Is this character a white space?
    public boolean isSpace() {
        return Character.isWhitespace(codePoint);
    }

    /// Is this character in the same run of text as `other`, i.e. written in
    /// the same script (states such as red or shaded are not considered)?
    public boolean isSameScriptAs(AlphabeticCharacter other) {
        return mdcScriptCode == other.mdcScriptCode;
    }

    /// The character as it is written in a MdC text block, before escaping:
    /// `^x` for uppercase transliteration, the plain character otherwise.
    public String getMdcText() {
        String s = Character.toString(codePoint);
        return uppercase ? "^" + s : s;
    }

    /// The string to display for this character.
    ///
    /// For transliteration, this is the actual glyph (e.g. ḫ for `x`), which
    /// may be several `char`s long. For other scripts, the character itself.
    ///
    /// @param transliterationEncoding how transliteration is displayed.
    public String getDisplayString(TransliterationEncoding transliterationEncoding) {
        if (script == ScriptCode.TRANSLITERATION && codePoint < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
            return TransliterationUtilities.getActualTransliterationString((char) codePoint, uppercase,
                    transliterationEncoding);
        }
        return Character.toString(codePoint);
    }

    @Override
    public void accept(ModelElementVisitor v) {
        v.visitAlphabeticCharacter(this);
    }

    @Override
    public String toString() {
        return "(char " + mdcScriptCode + " " + getMdcText() + ")";
    }

    @Override
    protected int compareToAux(ModelElement e) {
        AlphabeticCharacter other = (AlphabeticCharacter) e;
        int result = Character.compare(mdcScriptCode, other.mdcScriptCode);
        if (result == 0) {
            result = Integer.compare(codePoint, other.codePoint);
        }
        if (result == 0) {
            result = Boolean.compare(uppercase, other.uppercase);
        }
        if (result == 0) {
            result = getState().compareTo(other.getState());
        }
        return result;
    }

    @Override
    public HorizontalListElement buildHorizontalListElement() {
        return new SubCadrat(deepCopy());
    }

    @Override
    public AlphabeticCharacter deepCopy() {
        AlphabeticCharacter copy = new AlphabeticCharacter(mdcScriptCode, codePoint, uppercase);
        copyStateTo(copy);
        return copy;
    }

    @Override
    protected boolean equalsIgnoreIdAux(ModelElement other) {
        AlphabeticCharacter o = (AlphabeticCharacter) other;
        return mdcScriptCode == o.mdcScriptCode
                && codePoint == o.codePoint
                && uppercase == o.uppercase;
    }
}
