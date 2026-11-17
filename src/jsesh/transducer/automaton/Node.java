/*
 * Created on 4 mai 2005 by rosmord
 *
 * 
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package jsesh.transducer.automaton;

import jsesh.transducer.automaton.Automaton.LinkIterator;


/**
 * This interface represents a node in an automaton.
 * <p> As we want to avoid getting limited to a given implementation, this interface is designed so that
 * if the programmer doesn't want to store Node objects in the automaton, but, say, implement the automaton as
 * a bi-dimensional array, it should be possible, and vice-versa. 
 * @author rosmord
 */
public interface Node {
    /**
     * Get the unique label for this node.
     * Currently restricted to ints, but other types might be interesting.
     * @return the unique label for this node.
     */
    int getLabel();
    
    /**
     * Return true if the node is final.
     * @return true if the node is final.
     */
    boolean isFinal();
    
    /**
     * Returns the automaton.
     * @return the automaton.
     */
    Automaton getAutomaton();
    
    /*
     * Return an iterator for links which start (resp. end) with this node.      
     * @param direction  one of FORWARD or BACKWARD
     * @return an iterator for the links.
     */
    LinkIterator getLinkIterator(int direction);
    
    /**
     * Declares a node as final.
     * @param isFinal
     */
    void setFinal(boolean isFinal);

}