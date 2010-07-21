package jsesh.mdc.model;

import jsesh.mdc.constants.TabbingJustification;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

/*
 * This file is free Software
 * (c) Serge Rosmorduc
 * @author rosmord
 *
 */
/**
 * A complex tabulation.
 */

public class Tabbing extends TopItem {
	/**
	 * Identifies tab ids.
	 */
	private int id;
	
	/**
	 * The way the text after the tabbing will behave.
	 * (it might be necessary to put a tabbing in the beginning of a line if needed).
	 */
	private TabbingJustification tabbingJustification;
	
	/**
	 * The orientation for which the tabbing has a meaning.
	 * NOT USED CURRENTLY... perhaps not relevant.
	 */
	private TextOrientation orientation;
	
	public Tabbing(int id, TabbingJustification tabbingJustification,
			TextOrientation orientation) {
		super();
		this.id = id;
		this.tabbingJustification = tabbingJustification;
		this.orientation = orientation;
	}

	/*
	 * @see jsesh.mdc.model.ModelElement#accept(jsesh.mdc.model.ModelElementVisitor)
	 */
	public void accept(ModelElementVisitor v) {
		v.visitTabbing(this);
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#compareToAux(jsesh.mdc.model.ModelElement)
	 */
	public int compareToAux(ModelElement e) {
		Tabbing other= (Tabbing) e;
		int result= this.id - other.id;
		if (result !=  0)
			return result;
		result= this.tabbingJustification.compareTo(other.tabbingJustification);
		if (result !=  0)
			return result;
		result= this.orientation.getId() - other.orientation.getId();
		if (result !=  0)
			return result;
		if (result==0)
			result= getState().compareTo(((Tabbing)e).getState());
		return result;
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#deepCopy()
	 */
	public ModelElement deepCopy() {
		Tabbing r= new Tabbing(id, tabbingJustification, orientation);
		copyStateTo(r);
		return r;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(tabbing "+ id + " " + tabbingJustification + " "+ orientation+ ")";
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the tabbingJustification
	 */
	public TabbingJustification getTabbingJustification() {
		return tabbingJustification;
	}

	/**
	 * @return the orientation
	 */
	public TextOrientation getOrientation() {
		return orientation;
	}
	
	
}
