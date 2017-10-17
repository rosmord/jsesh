/** Java class "Cadrat.java" generated from Poseidon for UML. 
 *  Poseidon for UML is developed by <A HREF="http://www.gentleware.com">Gentleware</A>.
 *  Generated with <A HREF="http://jakarta.apache.org/velocity/">velocity</A> template engine.
 */
package jsesh.mdc.model;

import jsesh.mdc.interfaces.CadratInterface;
import jsesh.mdc.interfaces.VBoxInterface;

/**
 * <p> A quadrant.
 * <p>The current name of the class is not English at all, and refers to the French name for the "ideal squares" found
 * in hieroglyphic texts. 
 * @author Rosmorduc
 * </p>
 */
public class Cadrat
	extends BasicItem
	implements CadratInterface, VBoxInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3562569351364822516L;
	/**
	 * The code for this cadrat's shading.
	 * @see ShadingCode
	 */
	private int shading;


	public Cadrat() {
		shading = ShadingCode.NONE;
	}

	/**
	 * Creates a quadrant containing one item.
	 * @param item
	 * @throws IllegalArgumentException if the construction is not possible.
	 */
	public Cadrat(BasicItem item) {
		shading = ShadingCode.NONE;
		addHBox(new HBox(item));
	}
	
	/**
	 * Creates a quadrant containing one item.
	 * @param item
	 * @throws IllegalArgumentException if the construction is not possible.
	 */
	public Cadrat(HorizontalListElement item) {
		shading = ShadingCode.NONE;
		addHBox(new HBox(item));
	}
	

	public void addHBox(HBox hBox) {
		addChild(hBox);
	}

	public void removeHBox(HBox hBox) {
		removeChild(hBox);
	}
	
	public HBox getHBox(int idx) {
		return (HBox) getChildAt(idx);
	}

	public int getNumberOfHBoxes() {
		return getNumberOfChildren();
	}
	/**
	 * Returns the shading.
	 * if shading is 0, the cadrat is not shaded;
	 * else, the shading is given by the sum of 
	 * the constants TOP_LEFT... which state which quarter of the cadrat is shaded.
	 * Note that we provide an easier interface.
	 * @return int
	 * @see ShadingCode
	 */
	public int getShading() {
		return shading;
	}

	/**
	 * Sets the shading value.
	 * @see Cadrat#getShading()
	 * @param shading The shading to set (value in {@link ShadingCode}
	 * @see ShadingCode
	 */
	public void setShading(int shading) {
		this.shading = shading;
		notifyModification();
	}

	/**
	 * shade or unshade one quarter of a cadrat.
	 * @param cadratPart one of TOP_START, ... (defined in ShadingCode).
	 * @param isShaded
	 * @see ShadingCode
	 */
	public void setShade(int cadratPart, boolean isShaded) {
		if (isShaded) {
			shading |= cadratPart;
		} else {
			shading = shading ^ cadratPart;
		}
		notifyModification();
	}

	public boolean isShaded(int cadratPart) {
		return (cadratPart & shading) != 0;
	}

	public void accept(ModelElementVisitor v) {
		v.visitCadrat(this);
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(cadrat " + getChildrenAsString() + ")";
	}

	/**
	 * A cadrat is wide if it contain a box <em>filled</em> with a wide sign.
	 * <p> A wide sign is a sign that can be streched horizontally
	 * (in practice, \l marks such signs).
	 * @return true if the cadrat is wide.
	 */
	public boolean isWide() {
		boolean result= false;
		int i=0;
		while (! result && i < getNumberOfHBoxes())
			{
				result |= getHBox(i).isWide();
				i++;
			}
		return result;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#compareToAux(jsesh.mdc.model.ModelElement)
	 */
	public int compareToAux(ModelElement e) {
		int result= compareContents(e);
		if (result==0)
			result= getShading() - ((Cadrat) e).getShading();
		if (result==0)
			result= getState().compareTo(((Cadrat)e).getState());
		return result;
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#deepCopy()
	 */
	public Cadrat deepCopy() {
		Cadrat result;
		result= new Cadrat();
		result.shading= shading;
		copyContentTo(result);
		copyStateTo(result);
		return result;
	}

	@Override
	public HorizontalListElement buildHorizontalListElement() {
		// We might improve this one ?
		return new SubCadrat(deepCopy());		
	}
	
	/**
	 * Is this quadrant embedded in a larger quadrant as a sub-quadrant.
	 * For instance, in 
	 * <pre>
	 * 	n:(x:t)*U30
	 * </pre>
	 * (x:t) is an embedded quadrant.
	 * @return
	 */
	public boolean isEmbedded() {
		boolean embedded = false;

		ModelElement e = this;
		while (!embedded && e.getParent() != null) {
			e = e.getParent();
			if (e instanceof SubCadrat) {
				embedded = true;
			}
		}
		return embedded;
	}

    @Override
    protected boolean equalsIgnoreIdAux(ModelElement other) {
        Cadrat otherCadrat= (Cadrat) other;
        return this.shading == otherCadrat.shading;
    }
        
        
} // end Cadrat
