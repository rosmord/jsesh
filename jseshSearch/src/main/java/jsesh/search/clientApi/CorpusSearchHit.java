package jsesh.search.clientApi;


import java.nio.file.Path;

/**
 * An occurrence of a search result in a corpus.
 * I have removed MDCPosition from this class - but in fact, MDCPosition should
 * be independent from the text it refers to. 
 * <p> With MDC Position as it is, we have a major memory leak. All texts are kept
 * in memory.
 */
public class CorpusSearchHit {
    Path file;
    //MDCPosition position;
    int position;

    public CorpusSearchHit(Path file, int position) {
        this.file = file;
        this.position = position;
    }

    public Path getFile() {
        return file;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return String.format("%d : %s", position, file);
    }
    
    
}
