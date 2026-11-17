/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 9 mars 2005
 *
 */
package jsesh.transducer;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

/**
 * A base class for transducers.
 * <p>
 * Used in fact more for transduction result than as filters. Lots of things are
 * out of place, in particular the computation algorithms.
 * 
 * @author Serge Rosmorduc
 */
public abstract class Transducer {
    //      Data used for lowest cost path calculation
    public static final int WHITE = 0;

    public static final int GRAY = 1;

    public static final int BLACK = 2;

    List sortedNodes; // List of nodes

    abstract public class Node {

        /**
         * Data used for lowest cost path calculation. One of WHITE, GRAY, BLACK
         */
        private int state;

        private Link bestLink;

        private int bestCost;

        public abstract boolean isFinal();

        public abstract Transducer getTransducer();

        public abstract int getLabel();

        public abstract LinkList makeLinks();

        public abstract LinkList makePreviousLinks();

        // Auxiliary function for lowest cost path calculation
        private void depthFirst(List nodes) {
            // TODO
        }
    }

    public abstract class Link {
        public abstract Node getStart();

        public abstract Node getEnd();

        public abstract BoundExpr getUp();

        public abstract BoundExpr getDown();

        public abstract boolean downIsEpsilon();

        public abstract int getCost();
        
    }

    public abstract class LinkList {
        public abstract boolean hasElements();

        public abstract Link getElement();

        public abstract void next();

    }

    public abstract Node getStart();

    public abstract Node getEnd();

    public abstract Node getCurrentNode();

    public abstract void nextNode();

    public abstract void reset();

    public abstract boolean lastNode();

    public void computeShortestPath() {
        // A) Topological Sort O(S+A)
        getStart().depthFirst(sortedNodes);
        // B) Shortest path
        Iterator i = sortedNodes.iterator();
        Node first = (Node) i.next();
        first.bestCost = 0;
        // TODO : think about i.next()
        // cout << "computing shortest path :" << endl;
        while (i.hasNext()) {
            Node theNode = (Node) i.next();
            // LOOP INVARIANT :
            //   we know the shortest path
            //   for all of X's prececessors
            // cout << "node is " << (**i) << "(" << (*i) << ")" << endl;
            LinkList prevlist = theNode.makePreviousLinks();
            //   Find N, the predecessor of X such that :
            //    N.bestCost() + cost of link l1 between N and X is minimal
            //   X.bestLink= l1;
            // C++ correct line :
            // (*i).bestCost= numeric_limits<int>.max();
            // Currently usable line :
            theNode.bestCost = Integer.MAX_VALUE;
            while (prevlist.hasElements()) {
                Link currLink = prevlist.getElement();
                // cost for coming from currLink :
                int cost = currLink.getCost() + currLink.getStart().bestCost;
                // 
                if (theNode.bestCost > cost) {
                    theNode.bestCost = cost;
                    theNode.bestLink = currLink;
                }
                prevlist.next();
            }
            prevlist = null;
        }
        // C) Reverse the process and build the path
        // Look for *the* final node, let it be curr
        Node curr = getEnd();
        if (curr == null)
            throw new RuntimeException("No final node in this transducer");
        Link bestLink = curr.bestLink;
        curr.bestLink = null;
        while (curr != null) {
            // cout << "Looking at node " << curr << "(";
            // cout << *curr << ")" << endl;
            if (bestLink != null) {
                // We get the information
                // cout << "best link is " << *bestLink << endl;
                Node prev = bestLink.getStart();
                Link nextBest = prev.bestLink;
                // we put the link information in the starting node
                prev.bestLink = bestLink;
                // we retrieve the next link to consider
                bestLink = nextBest;
                // and we are ready for next node
                curr = prev;
            } else {
                // We'd better be at the beginning :
                if (curr != getStart())
                    throw new RuntimeException(
                            "No Path between start and end nodes");
                else {
                    // We stop
                    curr = null;
                }
            }
        }
        // Now, if there is a path, the lowest-cost path is indicated.

    }

    public void showPath(PrintWriter out, int detailLevel) {
        Node curr = getStart();
        int c = 0;
        out.println("Best path");
        while (curr != null) {
            if (curr.bestLink != null) {
                if (detailLevel > 0) {
                    if (curr.bestLink.getDown() != null)
                        out.write(curr.bestLink.getDown() + " ");

                } else
                    out.println(curr.bestLink);
                c += curr.bestLink.getCost();
                curr = curr.bestLink.getEnd();
            } else
                curr = null;
        }
        out.println();
        out.write("Total cost");
        out.write(c);
    }
}