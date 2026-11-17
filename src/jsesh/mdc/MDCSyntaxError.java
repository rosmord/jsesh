package jsesh.mdc;

/**
 * MDCSyntaxError.java
 *
 *
 * Created: Thu Jun 13 15:18:50 2002
 *
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC</a>
 * 
 */

public class MDCSyntaxError extends Exception {
    int line;    
    int charPos;
    String token;
    
    public MDCSyntaxError (int line, int charPos, String token){
	this.line= line;
	this.charPos= charPos;
	this.token= token;
    }

    public MDCSyntaxError (String msg, int line, int charPos, String token){
	super(msg);
	this.line= line;
	this.charPos= charPos;
	this.token= token;
    }

    /**
     * Get the value of line.
     * @return value of line.
     */
    public int getLine() {
	return line;
    }

    /**
     * Get the value of charPos.
     * @return value of charPos.
     */
    public int getCharPos() {
	return charPos;
    }
    

    /**
     * Get the value of token.
     * @return value of token.
     */
    public String getToken() {
	return token;
    }
    
}// MDCSyntaxError
