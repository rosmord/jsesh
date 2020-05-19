/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.search.plainText;

import java.util.ArrayList;
import java.util.List;

/**
 * A given text span and the corresponding position(s).
 * <p> Important note : in JSesh, the granularity of text is a bit annoying.
 * Text is not character-based, but "chunk" based or something like that.
 * <p> Hence, a search in text might overlap multiple chunks, and this is somehow
 * problematic. This class allows us to represent multiple consecutive chunks of text
 * as one, and yet return the correct position in case of match.
 * 
 * @author rosmord
 */
class TextAndPosition {
    /**
     * Position of the first chunk in the document.
     */
    private final int startPos;
    
    /**
     * Limits of chunks within the string.
     * Each limit is the (exclusive) end of the corresponding chunk.
     */
    private final List<Integer> limits;
    private final String content;
    
    private TextAndPosition(int startPos, String content, List<Integer> limits){
        this.limits = limits;
        // For now, search is case-insensitive....
        this.content = content.toLowerCase();
        this.startPos = startPos;
    }
    
    /**
     * Returns all Mdc positions inside this text where a particular string is found.
     * 
     * @param toMatch a string to search. If empty, the resulting list will also be empty.
     * @return a list of <em>positions</em> (indexes in the MdC document).
     */
    public List<Integer> match(String toMatch) { 
        // For now, we do case-insensitive search...
        toMatch = toMatch.toLowerCase();
        ArrayList<Integer> result= new ArrayList<>();
        if (toMatch.length() == 0)
            return result;
        int found = 0;        
        while ((found = content.indexOf(toMatch, found)) != -1 ) {
            int actualPos = convertIndexToPos(found);
            result.add(actualPos);
            found++;
        }
        return result;
    }
    
    public static Builder builder(int startPos) {
        return new Builder(startPos);
    }

    private int convertIndexToPos(int found) {
        for (int i = 0; i < limits.size(); i++) {            
            if (found < limits.get(i))
                return i + startPos;
        }
        throw new RuntimeException("not found ???");
    }
    
    public static class Builder {
        final int startPos;
        final StringBuilder stringBuilder;
        final List<Integer> limits;

        
        private Builder(int startPos) {
            this.stringBuilder = new StringBuilder();
            this.startPos = startPos;
            this.limits = new ArrayList<>();
        }
        
        public Builder add(String chunk) {
            stringBuilder.append(chunk);
            limits.add(stringBuilder.length()); // Start position of the chunk
            return this;
        }
        
        public TextAndPosition build() {
            return new TextAndPosition(startPos, stringBuilder.toString(), limits);
        }        
    
    }
}
