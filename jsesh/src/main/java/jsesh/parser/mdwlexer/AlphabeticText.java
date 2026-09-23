package jsesh.parser.mdwlexer;

/// A run of free-form alphabetic text, e.g. `+ftranslation`: `code` says which kind
/// of text this is (the letter right after `+`), `text` is the content with its
/// `\+` escapes resolved back into plain `+`.
public record AlphabeticText(char code, String text) {
}
