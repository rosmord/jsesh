package jsesh.search;

import jsesh.mdc.model.MDCPosition;

import java.nio.file.Path;

/**
 * An occurrence of a search result in a corpus.
 */
public class CorpusSearchHit {
    Path file;
    MDCPosition position;

    public CorpusSearchHit(Path file, MDCPosition position) {
        this.file = file;
        this.position = position;
    }

    public Path getFile() {
        return file;
    }

    public MDCPosition getPosition() {
        return position;
    }
}
