package jsesh.model;

/*
 * This file is free Software
 * (c) Serge Rosmorduc
 * @author rosmord
 *
 */

/// An absolute tab, `?N`, from Winglyph's version of the Manuel de Codage.
///
/// The next item is placed at an absolute position from the start of the
/// line, in tab units (`tabUnitWidth` in the geometry of the style).
///
/// Not to be confused with [Tab] (`%tab`), whose position is computed from
/// the text of the zone.
public class AbsoluteTab extends TopItem {
	private static final long serialVersionUID = -6631740389850638821L;
	private int stopPos;

	public AbsoluteTab(int stopPos) {
		this.stopPos = stopPos;
	}

	/// @return the position, in tab units.
	public int getStopPos() {
		return stopPos;
	}

	/// Sets the position, in tab units.
	public void setStopPos(int stopPos) {
		this.stopPos = stopPos;
	}

	@Override
	public void accept(ModelElementVisitor v) {
		v.visitAbsoluteTab(this);
	}

	@Override
	public int compareToAux(ModelElement e) {
		int result = stopPos - ((AbsoluteTab) e).stopPos;
		if (result == 0)
			result = getState().compareTo(((AbsoluteTab) e).getState());
		return result;
	}

	@Override
	public AbsoluteTab deepCopy() {
		AbsoluteTab r = new AbsoluteTab(stopPos);
		copyStateTo(r);
		return r;
	}

	@Override
	public String toString() {
		return "(tab " + stopPos + ")";
	}

	@Override
	public HorizontalListElement buildHorizontalListElement() {
		return null;
	}

	@Override
	protected boolean equalsIgnoreIdAux(ModelElement other) {
		return stopPos == ((AbsoluteTab) other).stopPos;
	}
}
