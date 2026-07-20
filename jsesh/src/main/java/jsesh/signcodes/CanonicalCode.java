package jsesh.signcodes;

/**
 * Canonical code for a Hieroglyph.
 * 
 * They can (only) be retrieved by the method {@link ManuelDeCodage#getCanonicalCode(String)}.
 * 
 * Currently, all signs, save some numbers, have a Gardiner-like code. The code is the canonical code for the sign, and is used to retrieve the shape of the sign in a font.
 * 
 * This interface is supposed to be a <b>value class</b>. Two Canonical codes with the same code are equals.
 * It's not a record, as we want to hide the constructor (its implementation might be).
 * Only the ManuelDeCodage class should be able to create new canonical codes.
 */
public interface CanonicalCode extends Comparable<CanonicalCode> {
    
    String code();

    default int compareTo(CanonicalCode other) {
        return code().compareTo(other.code());
    }
    
    /**
     * The toString method should return the code as is.
     * i.e. should return code(). 
     * @return
     */
    String toString();
    
}
