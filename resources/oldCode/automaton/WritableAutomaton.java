package jsesh.transducer.automaton;


/**
 * An automaton which can be modified.
 * @author rosmord
 */

public interface WritableAutomaton extends Automaton {
	 /**
     * Build a new node in the automaton.
     * @return the new node.
     */
    Node buildNode();
    
    void addLink(Node n1, Node n2, Object label);
   
    void setFinal(Node n);
    
    void remove(Node n);
    
    void remove(Link l);
}
