package jsesh.mdc.model;

import jsesh.mdc.interfaces.ModifierInterface;

/**
 * <p>
 * This class represent a modifier for a sign. a modifier is typically made of a
 * name and an integer value. There are a number of specific cases.
 * <ul>
 * <li>A No name
 * <li>A.1 no value => reverse the sign orientation
 * <li>A.2 integer value => scaling
 * <li>B name is "?" : doubtful reading
 * </ul>
 * 
 * The problem of signs reversing and rotating is quite badly dealt with in the
 * <em>manuel.</em> In fact, \t and \r override "\".
 * 
 * @author Rosmorduc
 *         </p>
 */
public class Modifier extends EmbeddedModelElement implements ModifierInterface {
	/**
	 * Represents the modifier's name.
	 */
	private String name;

	/**
	 * Represents the modifier's integer value, if any. If there is no integer
	 * value, this should be null
	 */
	private Integer value;

	public Modifier(String name, Integer value) {
		this.name= name;
		this.value= value;
	}        

	public void accept(ModelElementVisitor v) {
		v.visitModifier(this);
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#buildTopItem()
	 */
	public TopItem buildTopItem() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.ModelElement#compareToAux(jsesh.mdc.model.ModelElement)
	 */
	public int compareToAux(ModelElement e) {
		int result;
		Modifier m=(Modifier)e;
		if (name == null) {
			if (m.name == null)
				result= 0;
			else
				result= -1;
		} else if (m.name== null) {
				result= 1;
		} else {
				result= name.compareTo(m.name);
		}
		
		if (result == 0) {
			if (value == null) {
				if (m.value== null) {
					result= 0;
				} else {
					result= -1;
				}
			} else if (m.value == null)
				result= 1;
			else {
				result= value.compareTo(m.value);
			}
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#deepCopy()
	 */
	public ModelElement deepCopy() {
		// Both name and value are immutable objects !
		Modifier m= new Modifier(name,value);
		return m;
	}

	public boolean equals(Object obj) {
		boolean result= false;
		if (obj instanceof Modifier) {
			Modifier mod= (Modifier) obj;
			if (mod.name.equals(name)) {
				if (mod.value == null && value == null)
					result= true;
				else if (
					mod.value != null
						&& value != null
						&& value.equals(mod.value))
					result= true;
			}

		}
		return result;
	}

	public String getName() {
		return name;
	}
	public Integer getValue() {
		return value;
	}

	public int hashCode() {
		int result;
		result= name.hashCode();
		if (value != null)
			result += value.hashCode();
		return result;
	}
	public void setName(String _name) {
		name= _name;
		notifyModification();
	}
	
	public void setValue(Integer _value) {
		value= _value;
		notifyModification();
	}

	public String toString() {
		String result= "\\" + getName();
		if (value != null)
			result += value;
		return result;
	}

}
