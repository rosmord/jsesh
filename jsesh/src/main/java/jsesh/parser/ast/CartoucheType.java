package jsesh.parser.ast;

/**
 * The kinds of cartouche/enclosure a {@link AstCartouche} or old-style
 * cartouche start can denote.
 */
public interface CartoucheType {
    int CARTOUCHE = 'c';
    int SEREKH = 's';
    int HOUT = 'h';
    int CASTLE = 'f';
    int CIRCULAR_ENCLOSURE = 'g';

    int STARTCONSTRUCT = 1000;
    int ENDCONSTRUCT = 1010;
}
