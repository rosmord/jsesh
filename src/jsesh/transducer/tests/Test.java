/*
 * Created on 30 avr. 2005 by rosmord
 * 
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package jsesh.transducer.tests;

import jsesh.transducer.automaton.Automaton;
import jsesh.transducer.automaton.Epsilon;
import jsesh.transducer.automaton.Node;
import jsesh.transducer.automaton.SimpleAutomaton;
import jsesh.transducer.label.expression.SimpleLeftExpression;
import jsesh.transducer.label.expression.SimpleRightExpression;
import jsesh.transducer.label.simple.SimpleLabel;
import jsesh.transducer.transducer.SimpleRuleTransducer;

/**
 * @author rosmord
 *
 * Test class for  transducer-related classes.
 */
public class Test {

    public static void displayAutomaton(Automaton a) {
        Automaton.NodeIterator nodeIterator= a.getNodeIterator();
        while (nodeIterator.hasNext()) {
            Node n= nodeIterator.next();
            System.out.println("node "+n.getLabel());
            Automaton.LinkIterator linkiter= n.getLinkIterator(Automaton.BACKWARD);
            System.out.println("links toward " + n.getLabel());
            while (linkiter.hasNext()) {
                Automaton.Link link= linkiter.next();
                System.out.println(link.getStart() +" -" + link.getLabel().toString() + "-> "+ link.getEnd());
            }
        }
    }
    
    public static void main(String[] args) {
        /*
         * A) Create a simple automaton 
         * representing input : U23, D58
         */
        SimpleAutomaton a= new SimpleAutomaton();
        Node n0= a.getInitialNode();
        Node n1= a.buildNode();
        Node n2= a.buildNode();
        Node n3= a.buildNode();
        n3.setFinal(true);
        a.addLink(n0,n1, new SimpleLabel("U23"));
        a.addLink(n1,n2, Epsilon.EPSILON);
        a.addLink(n2,n3, new SimpleLabel("D48"));
        
        // Display :
        displayAutomaton(a);
        
        /*
         * B) create a rule SimpleRuleTransducer for processing the rules :
         * U23 -> P(A,b) / 100
         * U23 -> P(m,r) / 100
         * D58 -> P(b) / 100
         */
        SimpleRuleTransducer t= new SimpleRuleTransducer();
        Node tn0= t.getInitialNode();
        Node tn1= t.buildNode();
        Node tn2= t.buildNode();
       
        
        //t.addLink(tn0, tn1, ,);
        t.setRuleLimit(tn0);
        t.setRuleLimit(tn2);
        t.addLink(tn0,tn1,new SimpleLeftExpression("U23"),Epsilon.EPSILON,new SimpleRuleCost(0));
        t.addLink(tn1,tn2,Epsilon.EPSILON,new SimpleRightExpression("P", new String[]{"A", "b"}), new SimpleRuleCost(100));
        t.addLink(tn1,tn2,Epsilon.EPSILON,new SimpleRightExpression("P", new String[]{"m", "r"}), new SimpleRuleCost(100));
        t.addLink(tn0, tn2, new SimpleLeftExpression("D58"), new SimpleRightExpression("P", new String[] {"b"}), new SimpleRuleCost(100));
        t.addLink(tn2, tn0, Epsilon.EPSILON, Epsilon.EPSILON, new SimpleRuleCost(0));
        /*
         * Apply B to A. 
         */
        RuleNodeApplier composer= new RuleNodeApplier();
        
        Automaton at= composer.apply(t,a);
        /*
         * Print the resulting automaton
         */
       
        displayAutomaton(at);
        
        /*
         * create the rule transducer corresponding to :
         * P($X,$Y) -> L($X), L($Y) / 100
         * P($X) -> L($X) / 100
         * P($X,$Y), P($Y) -> L($X), L($Y) /100
         */
        /*
         * Print the result
         */
        /*
         * Compute the shortest way.
         * Print it. 
         */
    }
    
}
