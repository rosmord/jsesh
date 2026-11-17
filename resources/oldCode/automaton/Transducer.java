package jsesh.transducer.automaton;

import java.util.List;

/**
 * An interface which represents a simple transducer.
 * In java 1.5, we will probably create some kind of FinalStateInterface, which will summarize 
 * what this and Automata have in common. Currently, with java 1.4 compatibility, we can not do it cleanly.
 * @author rosmord
 *
 */
public interface Transducer {
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
    
    
    NodeIterator getNodeIterator();
    
    public interface Link {
        Node getStart();
        Node getEnd();
        Object getUpperLabel();
        Object getLowerLabel();
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
	 * @param automatonNode
	 * @return
	 */
	List getEpsilonLinksFrom(int transducerNode);

	public interface Node {
		    int getLabel();
		    boolean isFinal();
		    
		    Transducer getTransducer();
		    /*
		     * Return an iterator for links which start (resp. end) with this node.      
		     * @param direction  one of FORWARD or BACKWARD
		     * @return an iterator for the links.
		     */
		    LinkIterator getLinkIterator(int direction);
		
	}
}
