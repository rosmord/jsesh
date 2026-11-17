package jsesh.transducer.automaton;

import java.util.List;

import jsesh.transducer.tests.Couple;

/**
 * Wraps a transducer into an automaton.
 * 
 * @author rosmord
 * 
 */
public class TransducerToAutomatonWrapper implements Automaton {

	static public final int UP = 0;

	static public final int DOWN = 1;

	static public final int COUPLE = 2;

	private Transducer transducer;

	private int labelSource;

	/**
	 * Create an automaton representing a projection of a particular transducer.
	 * The resulting automaton will be labeled, either with only the top or
	 * bottom labels of the transducer's transitions (links), or with couples
	 * (topLabel,bottomLabel).
	 * 
	 * @param transducer
	 *            the transducer
	 * @param source
	 *            one of UP, DOWN or COUPLE, depending on which labels should be
	 *            used.
	 */

	public TransducerToAutomatonWrapper(Transducer transducer, int source) {
		this.transducer = transducer;
		labelSource = source;
	}

	public Node getInitialNode() {
		return new MyNode(transducer.getInitialNode());
	}

	public NodeIterator getNodeIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public List getEpsilonLinksFrom(int automatonNode) {
		// TODO Auto-generated method stub
		return null;
	}

	private class MyNode implements Node {
		Transducer.Node transducerNode;

		/**
		 * @param node
		 */
		public MyNode(Transducer.Node node) {
			transducerNode = node;
		}

		public int getLabel() {
			return transducerNode.getLabel();
		}

		public boolean isFinal() {
			return transducerNode.isFinal();
		}

		public Automaton getAutomaton() {
			return TransducerToAutomatonWrapper.this;
		}

		public LinkIterator getLinkIterator(int direction) {
			return new MyLinkIterator(transducerNode.getLinkIterator(direction));
		}
	}

	private class MyLink implements Automaton.Link {

		
		private Transducer.Link transducerLink;
		
		
		/**
		 * @param link
		 */
		public MyLink(jsesh.transducer.automaton.Transducer.Link link) {
			transducerLink = link;
		}

		public Node getStart() {
			return new MyNode(transducerLink.getStart());
		}

		public Node getEnd() {
			return new MyNode(transducerLink.getEnd());
		}

		public Object getLabel() {
			switch (labelSource) {
			case UP:
				return transducerLink.getUpperLabel();
			case DOWN:
				return transducerLink.getLowerLabel();
			case COUPLE:
				return new Couple(transducerLink.getUpperLabel(),
						transducerLink.getLowerLabel());
			default:
				throw new RuntimeException("bad labelSource " + labelSource);
			}
		}
	}
	
	private class MyLinkIterator implements LinkIterator {

		Transducer.LinkIterator transducerIterator;
		
		/**
		 * @param iterator
		 */
		public MyLinkIterator(jsesh.transducer.automaton.Transducer.LinkIterator iterator) {
			transducerIterator = iterator;
		}

		public boolean hasNext() {
			return transducerIterator.hasNext();
		}

		public Link next() {
			return new MyLink(transducerIterator.next());
		}
	}
}
