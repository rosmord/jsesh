package jsesh.search.clientApi;


import java.nio.file.Path;

/**
 * An occurrence of a search result in a corpus: a file, and a top-level index in it.
 */
public class CorpusSearchHit {
    Path file;
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
