package jsesh.mdc;


/**
 * ParserErrorManager.java
 *
 *
 * Created: Thu Jun 13 15:01:39 2002
 *
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC</a>
 * 
 */

public interface ParserErrorManager {
    MDCSyntaxError buildError(String message);
}// ParserErrorManager
