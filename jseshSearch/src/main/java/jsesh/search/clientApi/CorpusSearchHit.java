package jsesh.search.clientApi;


import java.nio.file.Path;

import jsesh.model.MDCPosition;

/**
 * An occurrence of a search result in a corpus: a file, and a position in it.
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

    @Override
    public String toString() {
        return String.format("%d : %s", position.getIndex(), file);
    }
    
    
}
