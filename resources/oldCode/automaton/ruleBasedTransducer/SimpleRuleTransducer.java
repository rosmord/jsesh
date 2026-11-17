/*
 * Created on 10 avr. 2005
 *
 */
package jsesh.finiteState.ruleBasedTransducer;

import java.util.HashSet;

import jsesh.finiteState.automaton.SimpleAutomaton;
import jsesh.finiteState.tests.SimpleTransducer;

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
    private SimpleTransducer automaton;

    private HashSet ruleLimitsNodes; 
    
    /**
     * Build a rule transducer with only one node.
     */
    public SimpleRuleTransducer() {
        automaton= new SimpleTransducer();
        ruleLimitsNodes= new HashSet();
    }
    
    /* (non-Javadoc)
	 * @see jsesh.finiteState.tests.SimpleTransducer#createNode()
	 */
	public int createNode() {
		return automaton.createNode();
	}

	/* (non-Javadoc)
	 * @see jsesh.finiteState.tests.SimpleTransducer#getNumberOfNodes()
	 */
	public int getNumberOfNodes() {
		return automaton.getNumberOfNodes();
	}

	/* (non-Javadoc)
	 * @see jsesh.finiteState.tests.SimpleTransducer#isFinal(int)
	 */
	public boolean isFinal(int nodeLabel) {
		return automaton.isFinal(nodeLabel);
	}

	/* (non-Javadoc)
	 * @see jsesh.finiteState.tests.SimpleTransducer#setFinal(int)
	 */
	public void setFinal(int nodeLabel) {
		automaton.setFinal(nodeLabel);
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

    public void addLink(int n1, int n2, Object topLabel, Object bottomLabel, RuleCost cost) {
        automaton.addLink(n1, n2, topLabel, bottomLabel);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return automaton.equals(obj);
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
    public void setRuleLimit(int n) {
        this.ruleLimitsNodes.add(new Integer(n));
    }

    /**
     * tells if a given node is a rule limit.
     * @param n
     * @return true if n is a rule limit.
     */
    public boolean isRuleLimit(int n) {
        return ruleLimitsNodes.contains(new Integer(n));
    }


}
