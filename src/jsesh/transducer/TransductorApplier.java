/*
 * Created on 25 mars 2005 by rosmord
 *
 * This file is distributed according to the GNU LESSER PUBLIC LICENCE
 */
package jsesh.transducer;

import java.util.Stack;

/**
 * @author rosmord
 * 
 * TODO DOCUMENT THIS CLASS
 */
public class TransductorApplier {
    Stack todo;

    public Transducer compose(Transducer Entry, VariableTransducer filter) {
        todo = new Stack();
        // The last part of the label is either :
        //   * a rule node label
        //   * a dynamically created label
        // Hence, maxLabel must be greater than the maximum label number in
        //   the RuleTransducer a2.
        // maxLabel= a2->getMaxLabel() + 1;
        // idea :

        // create first node.
        // Register it in node map.
        // push couple on todo.
        maxLabel = 0;
        start = new ChoiceNode(this, a1.getStart(), a2.getStart());
        end = 0;

        while (!toDo.empty()) {
            // Get toDo top.
            CompositeNode n;
            n = toDo.top();
            toDo.pop();

            // Extends the current node.

            extendsNode(n);
            // Test if node is "dead" (i.e. has no spawn).
            if (isDeadNode(n)) {
                delete n;
            }

        }
    }

    private void extendsNode() {
        // Algo : 
        // 
    }
    
  
    
}