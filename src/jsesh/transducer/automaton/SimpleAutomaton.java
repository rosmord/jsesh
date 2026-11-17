/*
 * Created on 2 mai 2005 by rosmord
 *
 * TODO document the file SimpleAutomaton.java
 * 
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package jsesh.transducer.automaton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * A simple straightforward implementation of an automaton.
 * In a SimpleAutomaton, each node is associated with one single Node object.
 * Hence, node equality can be tested through ==.
 * @author rosmord
 */
public class SimpleAutomaton implements Automaton {
    private int maxLabel;

    private Node initialNode;

    private List nodes;

    private HashSet finalNodes;

    /**
     * creates a SimpleAutomaton with an initial node.
     */
    public SimpleAutomaton() {
        maxLabel = 0;
        nodes = new ArrayList(); // Perhaps linked list ???
        finalNodes = new HashSet();
        initialNode= buildNode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.transducer.automaton.Automaton#getInitialNode()
     */
    public Node getInitialNode() {
        return initialNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.transducer.automaton.Automaton#buildNode()
     */
    public Node buildNode() {
        SimpleNode n= new SimpleNode();
        nodes.add(n);
        return n;
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.transducer.automaton.Automaton#getNodeIterator()
     */
    public NodeIterator getNodeIterator() {
        return new SimpleNodeIterator(nodes.iterator());
    }

    /*
     * (non-Javadoc)
     * 
     * @see jsesh.transducer.automaton.Automaton#addLink(jsesh.transducer.automaton.Automaton.SimpleNode,
     *      jsesh.transducer.automaton.Automaton.SimpleNode, java.lang.Object)
     */
    
    public void addLink(Node n1, Node n2, Object label) {
        // As n1 and n2 both have the same role, we have decided that the job should be done here.
        SimpleNode n1s= (SimpleNode) n1;
        SimpleNode n2s= (SimpleNode) n2;
        Link l = new Link(n1s, n2s, label);
        n1s.getForward().add(l);
        n2s.getBackward().add(l);
    }

    public class SimpleNode implements Node, Comparable {
        int label;

        List forwardLinks, backwardLinks;

        public SimpleNode() {
            this.label = maxLabel++;
            forwardLinks = null;
            backwardLinks = null;
        }

        /**
         * @return Returns the label.
         */
        public int getLabel() {
            return label;
        }

        /*
         * (non-Javadoc)
         * 
         * @see jsesh.transducer.automaton.Automaton.SimpleNode#getAutomaton()
         */
        public Automaton getAutomaton() {
            // SimpleNode is a inner class. So
            // the following line returns the containing object.
            return SimpleAutomaton.this;
        }

        /*
         * (non-Javadoc)
         * 
         * @see jsesh.transducer.automaton.Automaton.SimpleNode#isFinal()
         */
        public boolean isFinal() {
            return finalNodes.contains(this);
        }

        public void setFinal(boolean isFinal) {
            if (isFinal)
                finalNodes.add(this);
            else
                finalNodes.remove(this);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj) {
            if (obj instanceof SimpleNode) {
                SimpleNode node = (SimpleNode) obj;
                return getAutomaton() == node.getAutomaton()
                        && label == node.getLabel();
            } else {
                return false;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return label;
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Object o) {
            SimpleNode n= (SimpleNode) o;
            return label - n.label;
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see jsesh.transducer.automaton.Automaton.SimpleNode#getLinkIterator(int)
         */
        public LinkIterator getLinkIterator(int direction) {
            switch (direction) {
            case FORWARD:
                return new SimpleLinkIterator(getForward().iterator());
            case BACKWARD:
                return new SimpleLinkIterator(getBackward().iterator());
            default:
                throw new RuntimeException("Bad argument "+ direction);
            }
        }

        /**
         * Get the list of links which start with this node.
         * @return the list of links which start with this node.
         */
        List getForward() {
            if (forwardLinks == null)
                forwardLinks = new ArrayList();
            return forwardLinks;
        }

        /**
         * Get the list of links which point toward this node.
         * @return the list of links which point toward this node.
         */
        List getBackward() {
            if (backwardLinks == null)
                backwardLinks = new ArrayList();
            return backwardLinks;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return "node : "+ getLabel();
        }
    }

    private static class SimpleLinkIterator implements LinkIterator {
        Iterator it;

        public SimpleLinkIterator(Iterator it) {
            this.it = it;
        }

        /*
         * (non-Javadoc)
         * 
         * @see jsesh.transducer.automaton.Automaton.LinkIterator#remove()
         */
        public void remove() {
            it.remove();
        }

        /*
         * (non-Javadoc)
         * 
         * @see jsesh.transducer.automaton.Automaton.LinkIterator#hasNext()
         */
        public boolean hasNext() {
            return it.hasNext();
        }

        /*
         * (non-Javadoc)
         * 
         * @see jsesh.transducer.automaton.Automaton.LinkIterator#next()
         */
        public Automaton.Link next() {
            return (Link) it.next();
        }

    }

    private static class SimpleNodeIterator implements NodeIterator {

        private Iterator it;

        public SimpleNodeIterator(Iterator it) {
            this.it = it;
        }

        public void remove() {
            it.remove();
        }

        /*
         * (non-Javadoc)
         * 
         * @see jsesh.transducer.automaton.NodeIterator#hasNext()
         */
        public boolean hasNext() {
            return it.hasNext();
        }

        /*
         * (non-Javadoc)
         * 
         * @see jsesh.transducer.automaton.NodeIterator#next()
         */
        public Node next() {
            return (Node) it.next();
        }

    }

    public class Link implements Automaton.Link {

        private SimpleNode start;

        private SimpleNode end;

        private Object label;

        /**
         * @param start
         * @param end
         * @param label
         */
        public Link(SimpleNode start, SimpleNode end, Object label) {
            super();
            this.start = start;
            this.end = end;
            this.label = label;
        }

        /*
         * (non-Javadoc)
         * 
         * @see jsesh.transducer.automaton.Automaton.Link#getStart()
         */
        public Node getStart() {
            return start;
        }

        /*
         * (non-Javadoc)
         * 
         * @see jsesh.transducer.automaton.Automaton.Link#getEnd()
         */
        public Node getEnd() {
            return end;
        }

        /*
         * (non-Javadoc)
         * 
         * @see jsesh.transducer.automaton.Automaton.Link#getLabel()
         */
        public Object getLabel() {
            return label;
        }

    }

}
