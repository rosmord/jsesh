package jsesh.transducer.automaton;

public interface WritableTransducer extends Transducer {
    /**
     * Build a new node in the automaton.
     * @return the new node.
     */
    Node buildNode();

    void addLink(Node n1, Node n2, Object topLabel, Object bottomLabel);
    
    void setFinal(Node n);
}
