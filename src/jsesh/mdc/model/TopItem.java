package jsesh.mdc.model;

/**
 * 
 * TopItems are elements which can appear at the upper level of a manuel de codage text.
 * Some of them can appear in other positions : these are BasicItems.
 * @author rosmord
 *
 *This code is published under the GNU LGPL.
 */
abstract public class TopItem extends ModelElement{
	
	private TopItemState state;
	
	public TopItem() {
		state= new TopItemState();
	}

	/**
	 * @return
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

} 


