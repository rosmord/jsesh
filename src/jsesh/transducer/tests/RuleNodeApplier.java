/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 9 mai 2005
 *
 */
package jsesh.transducer.tests;

import java.util.Map;
import java.util.Stack;

import jsesh.transducer.automaton.Automaton;
import jsesh.transducer.automaton.SimpleAutomaton;
import jsesh.transducer.transducer.SimpleRuleTransducer;

/**
 * @author Serge Rosmorduc
 */
public class RuleNodeApplier {

    Automaton result= null;
    Map coupleMap;
    Stack toProcess;
    
    /**
     * Applies a transducer to an automaton.
     * @param t
     * @param a
     * @return the automaton just built.
     */
    public Automaton apply(SimpleRuleTransducer t, Automaton a) {
        result= new SimpleAutomaton();
      
        IntCouple couple= new IntCouple(0,0);
        coupleMap.put(couple, null);
        toProcess.push(couple);
        
        // For any node still in the stack
        while (! toProcess.isEmpty()) {
            couple= (IntCouple) toProcess.pop();
           
          
            // extend node depending on its type
        }
        return null;
    }

    private void extendNode() {
        
    }
    
    /**
     * Extends a node which corresponds to partial rule applications.
     */
    private void extendInternalNode() {
        
    }
    
    private static class NodeInfo {
        IntCouple couple;
        int label;
    }
    
}
