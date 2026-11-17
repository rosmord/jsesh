/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 9 mai 2005
 *
 */
package jsesh.transducer.tests;

import jsesh.transducer.transducer.RuleCost;

/**
 * @author Serge Rosmorduc
 */
public class SimpleRuleCost implements RuleCost {

    double cost;
    
    /**
     * @param cost
     */
    public SimpleRuleCost(double cost) {
        this.cost = cost;
    }
    
    
}
