package jsesh.mdc.model;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import jsesh.mdc.output.MdCModelWriter;

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
	 * @see jsesh.mdc.model.ModelElement#buildTopItem()
	 */
	public TopItem buildTopItem() {
		return (TopItem) deepCopy();
	}

	public String toMdC() {
		// As requested by the IFAO, we save the Manuel de codage content in
		// the picture as a comment.
		MdCModelWriter mdCModelWriter = new MdCModelWriter();
		StringWriter comment = new StringWriter();
		TopItemList l= new TopItemList();
		l.addChild(buildTopItem());
		mdCModelWriter.write(comment,l);

		String mdcText = comment.toString();
		return mdcText;
	}

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


