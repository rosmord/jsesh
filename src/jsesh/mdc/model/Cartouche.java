package jsesh.mdc.model;

import jsesh.mdc.interfaces.CartoucheInterface;

/**
 * <p>
 * 
 * @author Rosmorduc
 *         </p>
 */
public class Cartouche extends InnerGroup implements CartoucheInterface {

	/**
	 * <p>
	 * see endPart
	 * </p>
	 *  
	 */
	private int startPart;

	/**
	 * <p>
	 * indicates how the last extremity of the cartouche should be drawn.
	 * </p>
	 * <p>
	 * 0 : nothing
	 * </p>
	 * <p>
	 * 1 : normal start
	 * </p>
	 * <p>
	 * 2 : ending part (e.g. for actual cartouches; the node)
	 * </p>
	 * <p>
	 * For Hwt signs, this is somehow different :
	 * </p>
	 * <p>
	 * 1 : no square
	 * </p>
	 * <p>
	 * 2 : square in the lower part (for text in lines)
	 * </p>
	 * <p>
	 * 3 : square in the upper part.
	 * </p>
	 *  
	 */
	private int endPart;

	/**
	 * <p>
	 * one of 'c' (cartouche) 's' (serekh) 'h' hwt-sign and 'f' castle.
	 * </p>
	 *  
	 */
	private int type;

	/**
	 * <p>
	 * Creates a cartouche.
	 * </p>
	 * <p>
	 * 
	 * @param type
	 *            which actual kind of cartouche should be drawn
	 *            </p>
	 *            <p>
	 * @param startPart
	 *            describe which part of the cartouche to draw first.
	 *            </p>
	 *            <p>
	 * @param endPart
	 *            describe the last part of the cartouche to draw.
	 *            </p>
	 *            <p>
	 * @param content
	 *            the content of the cartouche
	 *            </p>
	 */
	public Cartouche(int type, int startPart, int endPart, BasicItemList content) {
		this.type = type;
		this.startPart = startPart;
		this.endPart = endPart;
		//setChildAt(0, content);
		addChildAt(0, content);
	} // end Cartouche

	/**
	 * <p>
	 * indicates how the first extremity of the cartouche should be drawn.
	 * </p>
	 * <p>
	 * 0 : nothing
	 * </p>
	 * <p>
	 * 1 : normal start
	 * </p>
	 * <p>
	 * 2 : ending part (e.g. for actual cartouches; the node)
	 * </p>
	 * <p>
	 * For Hwt signs, this is somehow different :
	 * </p>
	 * <p>
	 * 1 : no square
	 * </p>
	 * <p>
	 * 2 : square in the lower part (for text in lines)
	 * </p>
	 * <p>
	 * 3 : square in the upper part.
	 * </p>
	 * @return the end part code.
	 *  
	 */
	public int getStartPart() {
		return startPart;
	}

	public void setStartPart(int _startPart) {
		startPart = _startPart;
	}

	/**
	 * <p>
	 * indicates how the last extremity of the cartouche should be drawn.
	 * </p>
	 * <p>
	 * 0 : nothing
	 * </p>
	 * <p>
	 * 1 : normal start
	 * </p>
	 * <p>
	 * 2 : ending part (e.g. for actual cartouches; the node)
	 * </p>
	 * <p>
	 * For Hwt signs, this is somehow different :
	 * </p>
	 * <p>
	 * 1 : no square
	 * </p>
	 * <p>
	 * 2 : square in the lower part (for text in lines)
	 * </p>
	 * <p>
	 * 3 : square in the upper part.
	 * </p>
	 * @return the end part code.
	 *  
	 */

	public int getEndPart() {
		return endPart;
	}

	public void setEndPart(int _endPart) {
		endPart = _endPart;
	}

	/**
	 * Returns the type of the cartouche : one of 'c', 's', 'h', 'f'.
	 * 
	 * @return the type of the cartouche.
	 */
	public int getType() {
		return type;
	}

	public void setType(int _type) {
		type = _type;
	}

	public BasicItemIterator iterator() {
		return new BasicItemIterator(getListIterator());
	}

	/*
	 * @see jsesh.mdc.model.ModelElement#Accept(jsesh.mdc.model.ModelElementVisitor)
	 */

	public void accept(ModelElementVisitor v) {
		v.visitCartouche(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(cartouche " + getChildrenAsString() + ")";
	}

	/**
	 * returns the list of items contained in this cartouche.
	 * @return the list of items contained in this cartouche.
	 */
	public BasicItemList getBasicItemList() {
		return (BasicItemList) getChildAt(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.ModelElement#compareToAux(jsesh.mdc.model.ModelElement)
	 */
	public int compareToAux(ModelElement e) {
		int result = compareContents(e);
		if (result == 0) {
			Cartouche c = (Cartouche) e;
			;
			result = type - c.type;
			if (result == 0) {
				result = startPart - c.startPart;
				if (result == 0) {
					result = endPart - c.endPart;
// As cartouches are no more top-level items, they don't have state anymore
//					if (result == 0) {
//						result = getState().compareTo(c.getState());
//					}
				}
			}
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#deepCopy()
	 */
	public ModelElement deepCopy() {
		BasicItemList l= (BasicItemList) getBasicItemList().deepCopy();
		Cartouche c= new Cartouche(type,startPart, endPart,l);
		// As cartouches are no more top-level items, they don't have state anymore
		//copyStateTo(c);
		return c;
	}
	
	/**
	 * A cartouche is never considered as containing only one sign, as it's a sign in itself.
	 * 
	 */
	public boolean containsOnlyOneSign() {
		return false;
	}

} // end Cartouche
