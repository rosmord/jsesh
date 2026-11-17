/*
 * Created on 10 avr. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package jsesh.transducer.transducer;

import java.util.HashSet;

import jsesh.transducer.automaton.Node;
import jsesh.transducer.automaton.SimpleAutomaton;
import jsesh.transducer.automaton.Automaton.NodeIterator;

/**
 * A Rule-oriented transducer.
 * <p>
 * TODO add explanation about transducers.
 * TODO add explanation about costs.
 * TODO add explanation about rule limits
 * <p>
 * Note that a transducer is not an automaton (as far as classes are concerned).
 * For instance, there are limitations to the kind of possible labels for a
 * transducer. Now, this would be solved with java 1.5, but it won't be
 * available on all versions of the mac.
 * 
 * @author rosmord
 */
public class SimpleRuleTransducer {
    private SimpleAutomaton automaton;

    private HashSet ruleLimitsNodes; 
    
    /**
     * Build a rule transducer with only one node.
     */
    public SimpleRuleTransducer() {
        automaton= new SimpleAutomaton();
        ruleLimitsNodes= new HashSet();
    }
    
    /**
     * Add a link to the transducer.
     * 
     * @param n1
     * @param n2
     * @param topLabel
     * @param bottomLabel
     * @param cost the cost of the link.
     */

    public void addLink(Node n1, Node n2, Object topLabel, Object bottomLabel, RuleCost cost) {
        automaton.addLink(n1, n2, new TransducerLabel(topLabel, bottomLabel));
    }

    /**
     * return a new transducer node.
     * 
     * @return a new transducer node.
     */
    public Node buildNode() {
        return automaton.buildNode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return automaton.equals(obj);
    }

    /**
     * return the initial node.
     * 
     * @return the initial node.
     */

    public Node getInitialNode() {
        return automaton.getInitialNode();
    }

    /**
     * @return an iterator for the nodes.
     */
    public NodeIterator getNodeIterator() {
        return automaton.getNodeIterator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return automaton.hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return automaton.toString();
    }

    /**
     * Declares a node in the Transducer as a rule limit (rule begin or rule
     * end).
     * 
     * @param n
     */
    public void setRuleLimit(Node n) {
        this.ruleLimitsNodes.add(n);
    }

    /**
     * tells if a given node is a rule limit.
     * @param n
     * @return true if n is a rule limit.
     */
    public boolean isRuleLimit(Node n) {
        return ruleLimitsNodes.contains(n);
    }


}
