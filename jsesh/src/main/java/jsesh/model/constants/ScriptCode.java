package jsesh.model.constants;

/// The alphabetic script of a [jsesh.model.AlphabeticCharacter], keyed by the
/// Manuel de Codage letter which follows `+` (as in `+lsome latin text+s`).
///
/// The Manuel de Codage grammar accepts any lowercase letter (except `s`)
/// after `+`. Letters with no known meaning are read as [#OTHER]: the model
/// keeps the original letter, so the text is written back unchanged, and it is
/// rendered with the plain latin font.
///
/// `++` (comments) is not a script: comments are [jsesh.model.MdcComment]s.
///
/// @author rosmord
public enum ScriptCode {

    LATIN('l'),
    BOLD('b'),
    ITALIC('i'),
    TRANSLITERATION('t'),
    COPTIC('c'),
    GREEK('g'),
    HEBREW('h'),
    CYRILLIC('r'),
    /// Any other letter. The letter itself is kept by the character.
    OTHER('?');

    private final char mdcCode;

    ScriptCode(char mdcCode) {
        this.mdcCode = mdcCode;
    }

    /// The Manuel de Codage letter for this script (`?` for [#OTHER], which
    /// has no letter of its own).
    public char getMdcCode() {
        return mdcCode;
    }

    /// Is this script written from right to left?
    ///
    /// A run of such characters is laid out right to left, whatever the
    /// direction of the surrounding hieroglyphic text.
    public boolean isRightToLeft() {
        return this == HEBREW;
    }

    /// The script for a Manuel de Codage letter; [#OTHER] if the letter is not
    /// a known script code.
    public static ScriptCode forMdcCode(char code) {
        for (ScriptCode script : values()) {
            if (script != OTHER && script.mdcCode == code) {
                return script;
            }
        }
        return OTHER;
    }
}
