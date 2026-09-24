package jsesh.parser.ast;

/**
 * Whether a sign is followed by a word-end ({@code \}) or sentence-end
 * marker, as parsed.
 *
 * @author rosmord
 */
public enum WordEnding {
    NONE,
    WORD_END,
    SENTENCE_END
}
