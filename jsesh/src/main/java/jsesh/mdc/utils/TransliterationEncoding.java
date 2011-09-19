package jsesh.mdc.utils;

/**
 * The way transliteration can be encoded for output.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
public class TransliterationEncoding {
	private YODChoice yodChoice;
	private boolean unicode;
	private boolean gardinerQofUsed;
	
	public TransliterationEncoding(boolean unicode, YODChoice yodChoice,
			boolean gardinerQofUsed) {
		super();
		this.unicode = unicode;
		this.yodChoice = yodChoice;
		this.gardinerQofUsed = gardinerQofUsed;
	}

	public boolean isTranslitUnicode() {
		return this.unicode;
		
	}

	public YODChoice getYodChoice() {
		return yodChoice;
	}

	public boolean isGardinerQofUsed() {
		return gardinerQofUsed;
	}
}
