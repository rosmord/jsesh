/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 10 mars 2005
 *
 */
package jsesh.transducer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author Serge Rosmorduc
 */
public class EntryTransducer extends Transducer {
    private EntryNode start, end;

    private HashMap nodes;

    private Iterator iterator;

    private EntryNode currentNode;

    private class EntryNode extends Node {
        private int label;

        private boolean finalState;

        List links, previousLinks;

        public EntryNode(int label) {
            this.label = label;
            finalState = false;
        }

        // Inherited functions
        public boolean isFinal() {
            return finalState;
        }

        public Transducer getTransducer() {
            return EntryTransducer.this;
        }

        public int getLabel() {
            return label;
        }

        public LinkList makeLinks() {
            return new LinkListFromEntry(this, LinkDir.FORWARD);
        }

        public LinkList makePreviousLinks() {
            return new LinkListFromEntry(this, LinkDir.BACKWARD);
        }

        // Specific functions
        public void setFinal(boolean finalState) {
            this.finalState = finalState;
        }

        public void setLabel(int label) {
            this.label = label;
        }

        public EntryLink addLink(EntryNode end, String up, String down, int cost) {
            EntryLink res;
            BoundExpr bup, bdown;
            SimpleExprBuilder parse= new SimpleExprBuilder();
            // Create Bounded expressions.
            bup = new BoundExpr(parse.makePlainExpr(up), null);
            bdown = new BoundExpr(parse.makePlainExpr(down), null);
            res = new EntryLink(this, end, bup, bdown, cost);
            links.add(res);
            end.previousLinks.add(res);

            return res;

        }
    }

    private class EntryLink extends Link {
        private EntryNode start, end;

        BoundExpr up;

        BoundExpr down;

        int cost;

        /**
         * @param start
         * @param end
         * @param up
         * @param down
         * @param cost
         */
        public EntryLink(EntryNode start, EntryNode end, BoundExpr up,
                BoundExpr down, int cost) {
            super();
            this.start = start;
            this.end = end;
            this.up = up;
            this.down = down;
            this.cost = cost;
        }

        // Inherited Methods
        public Node getStart() {
            return start;
        }

        public Node getEnd() {
            return end;
        }

        public BoundExpr getUp() {
            return up;
        }

        public BoundExpr getDown() {
            return down;
        }

        public boolean downIsEpsilon() {
            return (down == null) || down.getExpr().isEpsilon();
        }

        public int getCost() {
            return cost;
        }
    }

    private class LinkListFromEntry extends LinkList {

        private Iterator iterator;

        private Link currentLink;

        private LinkListFromEntry(EntryNode n, LinkDir d) {
            if (d.equals(LinkDir.FORWARD))
                iterator = n.links.iterator();
            else
                iterator = n.previousLinks.iterator();
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
            if (iterator.hasNext()) {
                currentLink = (Link) iterator.next();
            } else {
                currentLink = null;
            }
        }
    }

    public EntryTransducer() {
        start = makeNodeByLabel(0);
        end = null;
        nodes = new HashMap();
        currentNode = null;
    }

    // Overloaded Methods
    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public void nextNode() {
        if (iterator.hasNext())
            currentNode = (EntryNode) iterator.next();
        else
            currentNode = null;
    }

    public void reset() {
        iterator = (Iterator) nodes.values();
    }

    public boolean lastNode() {
        return currentNode == null;
    }

    // "Real" access functions :
    private EntryNode getTrueStart() {
        return start;
    }

    private void setEnd(EntryNode node) {
        if (end == null) {
            end = node;
        } else {
            throw new RuntimeException("Only one end allowed.");
        }
    }

    private EntryNode getTrueCurrentNode() {
        return currentNode;
    }

    // Specific Methods
    private EntryNode makeNodeByLabel(int l) {
        EntryNode result;
        result = new EntryNode(l);
        nodes.put(new Integer(l), result);
        return result;
    }

}