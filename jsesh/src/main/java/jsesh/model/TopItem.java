package jsesh.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 
 * TopItems are elements which can appear at the upper level of a manuel de codage text.
 * Some of them can appear in other positions : these are BasicItems.
 * @author rosmord
 *
 *This code is published under the GNU LGPL.
 */
abstract public class TopItem extends EmbeddedModelElement{
	private static final long serialVersionUID = 658832630365366776L;
	private TopItemState state;
	
	public TopItem() {
		state= new TopItemState();
	}

	/**
	 * @return the state.
	 */
	public TopItemState getState()
	{
		return state;
	}

	/**
	 * @param state
	 */
	public void setState(TopItemState state)
	{
		this.state= state;
	}
	
	protected void copyStateTo(TopItem it) {
		try {
			it.state= (TopItemState) state.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	
	/* (non-Javadoc)
	 * @see jsesh.model.ModelElement#buildTopItem()
	 */
	public TopItem buildTopItem() {
		return (TopItem) deepCopy();
	}

	/*public String toMdC() {
		// As requested by the IFAO, we save the Manuel de codage content in
		// the picture as a comment.
		MdCModelWriter mdCModelWriter = new MdCModelWriter();
		StringWriter comment = new StringWriter();
		HieroglyphicText l= new HieroglyphicText();
		l.addChild(buildTopItem());
		mdCModelWriter.write(comment,l);

		String mdcText = comment.toString();
		return mdcText;
	}
*/

	public boolean isRed() {
		return state.isRed();
	}

	public boolean isShaded() {
		return state.isShaded();
	}

	public void setRed(boolean b) {
		state.setRed(b);
	}

	public void setShaded(boolean b) {
		state.setShaded(b);
	}
	
	@Override
	public abstract TopItem deepCopy();

	/// The element which contains a nested gap of this item.
	///
	/// The gap is given by its position in the text: the first index of its
	/// path is taken to be the index of this item, whatever it is. This way,
	/// a copy of an item can be queried before it is put in the text.
	///
	/// @param gap a nested position (not a top-level one).
	/// @return the element which contains the gap, or empty if the path
	///         doesn't match the structure of this item.
	public Optional<ModelElement> getGapContainer(MDCPosition gap) {
		int[] path = gap.getPath();
		return getDescendant(Arrays.copyOfRange(path, 1, path.length - 1));
	}

	/// Brings a position inside this item to the nearest place where the cursor
	/// may stand.
	///
	/// The edge gaps (first and last) of a container which is neither a list of
	/// quadrats ([ModelElement#edgeGapsAreStops()]) nor a row or a quadrat
	/// ([ModelElement#edgeGapsAreInnerStops()]) are not stops: they coincide
	/// with the gap which surrounds the container. Such a position is replaced
	/// by the surrounding gap, and so on recursively, up to a top-level
	/// position if need be. Other positions are returned unchanged.
	///
	/// As for [#getGapContainer(MDCPosition)], the first index of the path is
	/// taken to be the index of this item.
	///
	/// @param position a position, possibly nested. A path which no longer
	///        matches the structure of this item is handled as far as possible.
	/// @return the equivalent stop.
	public MDCPosition canonicalize(MDCPosition position) {
		MDCPosition p = position;
		while (!p.isTop()) {
			int gap = p.getLastIndex();
			if (getGapContainer(p).map(c -> c.isCursorStop(gap, CaretGranularity.INNER)).orElse(false)) {
				return p;
			}
			p = p.surroundingGap();
		}
		return p;
	}
	
	/**
	 * defensive copy.
	 * @param items
	 * @return
	 */
	public static List<TopItem> listCopy(List<TopItem> items) {
		ArrayList<TopItem> result= new ArrayList<TopItem>();
		for (TopItem item: items) {
			result.add(item.deepCopy());
		}
		return result;
	}
} 


