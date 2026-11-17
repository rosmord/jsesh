/*
 * Created on 22 févr. 2005 by rosmord
 *
 * This file is distributed according to the GNU LESSER PUBLIC LICENCE
 */
package jsesh.transducer.old;

import java.util.List;

/**
 * @author rosmord
 *
 * TODO DOCUMENT THIS CLASS
 */
public class RuleTranducer {
    static public class RuleLink {
        private RuleNode start, end;
        private Rule rule;
        private RuleItem ruleItem;
        /**
         * Create a new link.
         * Should this method be private ??????
         * @param node
         * @param dest
         * @param r
         */
        public RuleLink(RuleNode start, RuleNode end, Rule rule) {
            this.start= start;
            this.end= end;
            this.rule= rule;
            this.ruleItem= new RuleItem(rule);
        }
        
        /**
         * @return Returns the rule.
         */
        public Rule getRule() {
            return rule;
        }
        
        public int getCost() {
            return rule.getCost();            
        }
        
        /**
         * @return Returns the ruleItem.
         */
        public RuleItem getRuleItem() {
            return ruleItem;
        }
        
        /**
         * @return Returns the start.
         */
        public RuleNode getStart() {
            return start;
        }
        
        /**
         * @return Returns the end.
         */
        public RuleNode getEnd() {
            return end;
        }
        
    }

    public class RuleNode {
        private int label;
        boolean finalState;
        List links; // List of RuleLinks
        public RuleNode(int label) {
            this.label= label;
            finalState= false;
        }

        // TODO : move the string out of here. It doesn't belong here !
        public RuleLink addLink(RuleNode dest, String rule) {
            RuleLink res;
            SimpleExprBuilder parser= new SimpleExprBuilder();
            Rule r= parser.makeRule(rule);
            res= new RuleLink(this,dest,r);
            links.add(res);
            return res;
        }
        
        /**
         * @return Returns the finalState.
         */
        public boolean isFinalState() {
            return finalState;
        }
        
        /**
         * @param finalState The finalState to set.
         */
        public void setFinalState(boolean finalState) {
            this.finalState = finalState;
        }
    
        /**
         * @return Returns the label.
         */
        public int getLabel() {
            return label;
        }
        
        
    }
    
    private RuleNode start, end;
    
    public RuleTranducer() {
        start= makeNodeByLabel(0);
        end= null;
    }
    
    public RuleNode getStart(){
        return start;
    }
    
    public RuleNode getEnd() {
        return end;
    }
    
    public void setEnd(RuleNode node) {
        // TODO : allow multiple end nodes ?
        // the reason there is only one end node is the computation of the shortest past.
        if (end == null)
            end= node;
        else
            throw new RuntimeException("Only one end node is currently possible for a Rule Transducer");
    }
    
    public RuleNode makeNodeByLabel(int l) {
        RuleNode res;
        res= new RuleNode(l);
        return res;
    }
    
    
}
