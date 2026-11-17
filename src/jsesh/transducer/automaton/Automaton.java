/*
 * Created on 2 mai 2005 by rosmord
 *
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package jsesh.transducer.automaton;

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
    Node getInitialNode();
    
    /**
     * Build a new node in the automaton.
     * @return the new node.
     */
    Node buildNode();
    
    NodeIterator getNodeIterator();
    
    void addLink(Node n1, Node n2, Object label);
    
    public interface Link {
        Node getStart();
        Node getEnd();
        Object getLabel();
    }
    
    public interface LinkIterator {
        void remove();
        boolean hasNext();
        Link next();        
    }
    
    public interface NodeIterator {

        /**
         * Remove the current node.
         * Not always available.
         * @see java.util.Iterator#remove()
         */
        public void remove();

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext();

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public Node next();
    }

}


