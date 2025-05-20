package jsesh.mdc.lex;

public class MDCHRule implements MDCSymbols {
	private char lineType;
	private int startPos;
	private int endPos;

	public MDCHRule(char lineType, int startPos, int endPos) {
		this.lineType = lineType;
		this.startPos = startPos;
		this.endPos = endPos;
	}

	/**
	 * Gets line type.
	 * 
	 * @return 'l' for simple line, and 'L' for double line
	 */
	public char getLineType() {
		return this.lineType;
	}

	/**
	 * Sets the value of lineType
	 * 
	 * @param argLineType
	 *            Value to assign to this.lineType
	 */
	public void setLineType(char argLineType) {
		this.lineType = argLineType;
	}

	/**
	 * Gets the start position for this horizontal rule
	 * 
	 * @return an absolute integer position from left edge of the page (?), in
	 *         tab units.
	 * @see DrawingSpecification#getTabUnitWidth()
	 */
	public int getStartPos() {
		return this.startPos;
	}

	/**
	 * Sets the value of startPos
	 * 
	 * @param argStartPos
	 *            Value to assign to this.startPos
	 */
	public void setStartPos(int argStartPos) {
		this.startPos = argStartPos;
	}

	/**
	 * Gets the ending position for this horizontal rule
	 * 
	 * @return an absolute integer position from left edge of the page (?).
	 * @see DrawingSpecification#getTabUnitWidth()
	 */
	public int getEndPos() {
		return this.endPos;
	}

	/**
	 * Sets the value of endPos
	 * 
	 * @param argEndPos
	 *            Value to assign to this.endPos
	 */
	public void setEndPos(int argEndPos) {
		this.endPos = argEndPos;
	}
}
