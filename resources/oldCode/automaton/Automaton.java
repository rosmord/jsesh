/*
 * Created on 2 mai 2005 by rosmord
 *
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package jsesh.transducer.automaton;

import java.util.List;

/**
 * @author rosmord
 *
 * Simple interface for automata.
 *
 *<p> Development notes : I wonder if we want a "Node" class.
 * It might be more interesting to always use integers label to represent nodes.
 * On the other hand, as far as internal representation is concerned, having a separate node type
 * is less implementation specific.
 * Restriction on the represented automata :
 * <ul>
 * <li> Only one initial node.
 * <li> 
 * </ul>
 */
public interface Automaton {
    /**
     * constants for links which starts with a given node.
     */
    public static final int FORWARD= 0;
    /**
     * constants for links which ends with a given node.
     */
    public static final int BACKWARD= 1;
    
    /**
     * Return the initial node for this automaton.
     * @return the initial node for this automaton.
     */
    public Node getInitialNode();
   
    public NodeIterator getNodeIterator();
    
    public interface Link {
        Node getStart();
        Node getEnd();
        Object getLabel();
    }
    
    public interface LinkIterator {
        boolean hasNext();
        Link next();        
    }
    
    public interface NodeIterator {

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext();

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public Node next();
    }


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
		 * Returns the list of epsilon links from this automaton.
		 * @param automatonNode
		 * @return
		 */
		List getEpsilonLinksFrom(int automatonNode);

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
	}
}


