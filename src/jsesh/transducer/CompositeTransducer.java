/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 10 mars 2005
 *
 */
package jsesh.transducer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import jsesh.transducer.Transducer.Node;

/**
 * @author Serge Rosmorduc
 */

public class CompositeTransducer extends Transducer {

//  Nodes in Composite transducers
//  A node in a composite transducer is either
//  associated with an couple of nodes (ChoiceNode)
//  or with an entry node and a rule state (MiddleNode)

    public abstract class CompositeNode extends Node {
        
        private Node entry;
        private int label;
        protected List links;
        protected List previousLinks;
        
        protected CompositeNode(Node e) {
            entry= e;
            addToAgenda(this);            
            label= nextLabel();
            // Register the node.
            nodes.put(new Integer(label),this);
            links= new ArrayList();
            previousLinks= new ArrayList();
        }

        // Inherited functions
        public Transducer getTransducer() {
            return CompositeTransducer.this;
        }
        
        public int getLabel() {return label;}
        
        public LinkList  makeLinks() {
            return new LinkListFromComposite(this, LinkDir.FORWARD);
        }
        public LinkList  makePreviousLinks() {
            return new LinkListFromComposite(this,LinkDir.BACKWARD);
        }
            
        // Specific functions
        Node getUpNode() {
            return entry;
        }
         
        CompositeTransducer getCompAutomaton() {
            return CompositeTransducer.this;
        }
        
        abstract void extendsNode();
        
        abstract boolean isDeadNode();
        
        private CompositeLink addLink(CompositeNode end,
                BoundExpr upper,	/* upper label */
                RuleItem ruleItem, /* Rule item. determine lower label */
                int cost	/* cost of the link */
            	) {
            return new CompositeLink();
        }
            
            
            // For use by CompositeLink only :
            private void registerLink(CompositeLink link, enum LinkDir);
            private void unRegisterLink(CompositeLink link, enum LinkDir);
            
    }

    public class ChoiceNode extends CompositeNode {
        private  void extendsNode();
        private boolean isDeadNode() {}
        private RuleNode ruleNode;
        private boolean finalState;
        
        ChoiceNode(Node e, RuleNode r) {}
        boolean isFinal() {}        
        RuleNode getRuleNode();
    }

//  Intermediary nodes. The only choice stands between different entries
//  A middle node can only have one entry
    class MiddleNode extends CompositeNode {
        private    RuleItem  ruleItem;
        private RuleLink  ruleLink;

        public   MiddleNode(
 	     Node  e,		// the entry node
 	     RuleItem r,	// the rule state
 	     RuleLink rl);	// the associated rule link
   
   boolean isFinal();
  private   void extendsNode();
   private boolean isDeadNode();
   RuleItem  getRuleItem();
   RuleLink  getRuleLink();
 };

 private class CompositeLink extends Link {
     private CompositeNode start, end;
     private  BoundExpr up;
     private RuleItem down;
     private int cost;
     
    public CompositeLink(CompositeNode start, CompositeNode end, BoundExpr up,
            RuleItem down, int cost) {
        super();
        this.start = start;
        this.end = end;
        this.up = up;
        this.down = down;
        this.cost = cost;
    }

    /* (non-Javadoc)
     * @see jsesh.transducer.Transducer.Link#getStart()
     */
    public Node getStart() {
       return start;
    }

    /* (non-Javadoc)
     * @see jsesh.transducer.Transducer.Link#getEnd()
     */
    public Node getEnd() {
        return end;
    }

    /* (non-Javadoc)
     * @see jsesh.transducer.Transducer.Link#getUp()
     */
    public BoundExpr getUp() {
        return up;
    }

    /* (non-Javadoc)
     * @see jsesh.transducer.Transducer.Link#getDown()
     */
    public BoundExpr getDown() {
        if (down != null)
            return down.getLower();
          else
            return null;
    }

    /* (non-Javadoc)
     * @see jsesh.transducer.Transducer.Link#downIsEpsilon()
     */
    public boolean downIsEpsilon() {
        return
        (down == null) || 
        (down.getLower() == null) ||
        (down.getLower().getExpr() == null) ||    
        (down.getLower().getExpr().isEpsilon());

    }

    /* (non-Javadoc)
     * @see jsesh.transducer.Transducer.Link#getCost()
     */
    public int getCost() {
      return cost;
    }
    
    
 }
 
 private class LinkListFromComposite extends LinkList {
     Iterator iterator;
     CompositeLink currentLink;
     
     LinkListFromComposite(CompositeNode n, LinkDir d) {
         if (d.equals(LinkDir.FORWARD)) {
             iterator= n.links.iterator();            
         } else {
             iterator= n.previousLinks.iterator();
         }
         next();
     }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.transducer.Transducer.LinkList#hasElements()
     */
    public boolean hasElements() {
       return currentLink != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.transducer.Transducer.LinkList#getElement()
     */
    public Link getElement() {
        return currentLink;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.transducer.Transducer.LinkList#next()
     */
    public void next() {
        if (iterator.hasNext())
        {
            currentLink= (CompositeLink) iterator.next();            
        } else
            currentLink= null;
    }
         
 }
    private int maxLabel; // Label to use for new nodes
    
    // The table below is used to associate a Node with a couple of Nodes
    // from the two parent transducers
    // typedef map<pair<Node*,RuleNode*>,ChoiceNode*> Labels;
    private HashMap labelValues;
    
    // Nodes still to be expanded.
    // Shouldn't be there, methink.
    //stack<CompositeNode*> toDo;
    Stack toDo;

    // First Node
    CompositeNode start;
    CompositeNode end;

    // Nodes
    //map<int,CompositeNode*> nodes;
    HashMap nodes;
    
    //map<int,CompositeNode*>::iterator currentNode;
    CompositeNode currentNode;
    Iterator iterator;

    public CompositeTransducer(Transducer transducer, RuleTransducer ruleTransducer) {
        
    }

    
    // Overloaded Methods
    
    // "Real" access functions :
    CompositeNode getTrueStart() {return start;}
    CompositeNode  getTrueCurrentNode() {return currentNode;}  

    // Specific Methods
    ChoiceNode  getChoiceNode(Node n, RuleNode r) {
        // TODO
    }
    
    int nextLabel() {}
    int getMaxLabel() {}
    
    void addToAgenda(CompositeNode node) {
        
    }

    void setEnd(CompositeNode node) {
        // TODO
    }

}
