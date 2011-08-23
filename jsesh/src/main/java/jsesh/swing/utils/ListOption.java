package jsesh.swing.utils;

/**
 * A couple key/displayed value for use in comboboxes.
 * This can be used for any graphical object which must display a string, and be selected from
 * a given list. It mirrors, more or less, the use of select and option in HTML.
 * @author rosmord
 *
 */
public class ListOption {
	private Object key="";
	private String displayedValue="";
	
	
	public ListOption(Object key, String displayedValue) {
		super();
		this.key = key;
		this.displayedValue = displayedValue;
	}


	
	public Object getKey() {
		return key;
	}



	public String getDisplayedValue() {
		return displayedValue;
	}



	public String toString() {
		return displayedValue;
	}
}
