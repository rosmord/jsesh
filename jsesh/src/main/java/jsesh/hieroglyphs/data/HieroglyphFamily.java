package jsesh.hieroglyphs.data;

/**
 * Abstract representation of a family of hieroglyphs.
 * 
 * @author rosmord
 * 
 */
public class HieroglyphFamily {
	private String code = "";

	private String description = "";

	public HieroglyphFamily(String code) {
		this.code = code;
	}

	public HieroglyphFamily(String code, String description) {
		super();
		this.code = code;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public String toString() {
		return code + ". " + description;
	}
}
