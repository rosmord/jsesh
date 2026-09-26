package jsesh.ui.editor;

import java.util.Optional;

import jsesh.model.constants.ScriptCode;

/// What typed keys produce in the editor: hieroglyphic codes, or text in some
/// alphabetic script.
///
/// @author rosmord
public enum InputMode {

    /// Keys are Manuel de Codage codes for hieroglyphs.
    HIEROGLYPHS(null),
    LATIN(ScriptCode.LATIN),
    ITALIC(ScriptCode.ITALIC),
    BOLD(ScriptCode.BOLD),
    TRANSLITERATION(ScriptCode.TRANSLITERATION),
    /// Uppercase transliteration, for the next character only (the mode then
    /// returns to [#TRANSLITERATION]).
    UPPERCASE_TRANSLITERATION(ScriptCode.TRANSLITERATION),
    /// Line and page numbers (superscript text).
    LINE_NUMBER(null);

    private final ScriptCode script;

    InputMode(ScriptCode script) {
        this.script = script;
    }

    /// The script of the characters typed in this mode, if it produces
    /// alphabetic characters.
    public Optional<ScriptCode> getScript() {
        return Optional.ofNullable(script);
    }

    /// Does this mode produce alphabetic characters?
    public boolean isAlphabetic() {
        return script != null;
    }

    /// The mode for typing characters in a given script, if there is one
    /// (there is no mode for coptic, greek...).
    public static Optional<InputMode> forScript(ScriptCode script) {
        return switch (script) {
            case LATIN -> Optional.of(LATIN);
            case ITALIC -> Optional.of(ITALIC);
            case BOLD -> Optional.of(BOLD);
            case TRANSLITERATION -> Optional.of(TRANSLITERATION);
            default -> Optional.empty();
        };
    }
}
