package jsesh.transducer.automaton;

import java.util.List;

/**
 * Wraps an automaton into a transducer.
 * The links labels of this transducer are always (lab,lab) if the original link has label "lab".
 * @author rosmord
 *
 */
public class AutomatonToTransducerWrapper implements Transducer {
	private Automaton automaton;

	/**
	 * @param automaton
	 */
	public AutomatonToTransducerWrapper(Automaton automaton) {
		this.automaton = automaton;
	}

	public Node getInitialNode() {
		return new MyNode(automaton.getInitialNode());
	}

	public NodeIterator getNodeIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public List getEpsilonLinksFrom(int transducerNode) {
		// TODO Auto-generated method stub
		return null;
	}

	private class MyNode implements Node {
		private Automaton.Node node;
		
		public MyNode(Automaton.Node node) {
			this.node = node;
		}

		public int getLabel() {
			return node.getLabel();
		}

		public boolean isFinal() {
			return node.isFinal();
		}

		public Transducer getTransducer() {
			return AutomatonToTransducerWrapper.this;
		}

		public LinkIterator getLinkIterator(int direction) {
			return new MyLinkIterator(node.getLinkIterator(direction));
		}
	}
	
	private class MyLinkIterator implements LinkIterator {
		private Automaton.LinkIterator iterator;
		
		public MyLinkIterator(jsesh.transducer.automaton.Automaton.LinkIterator iterator) {
			this.iterator = iterator;
		}

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public Link next() {
			return new MyLink(iterator.next());
		}
	}
	
	private class MyLink implements Link {
		Automaton.Link link;
		
		public MyLink(jsesh.transducer.automaton.Automaton.Link link) {
			this.link = link;
		}

		public jsesh.transducer.automaton.Transducer.Node getStart() {
			return new MyNode(link.getStart());
		}

		public jsesh.transducer.automaton.Transducer.Node getEnd() {
			return new MyNode(link.getEnd());
		}

		public Object getUpperLabel() {
			return link.getLabel();
		}

		public Object getLowerLabel() {
			return link.getLabel();
		}
	}
}
