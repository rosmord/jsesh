package org.qenherkhopeshef.graphics.eps;

public class EPSCMYKColor {
	int cyan;
	int magenta;
	int yellow;
	int black;
	
	/**
	 * Create a color.
	 * Scale is 100 : full color, 0 no color.
	 * @param cyan
	 * @param magenta
	 * @param yellow
	 * @param black
	 */
	public EPSCMYKColor(int cyan, int magenta, int yellow, int black) {
		super();
		this.cyan = cyan;
		this.magenta = magenta;
		this.yellow = yellow;
		this.black = black;
	}
	
	public int getCyan() {
		return cyan;
	}
	public int getMagenta() {
		return magenta;
	}
	public int getYellow() {
		return yellow;
	}
	public int getBlack() {
		return black;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + black;
		result = prime * result + cyan;
		result = prime * result + magenta;
		result = prime * result + yellow;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EPSCMYKColor other = (EPSCMYKColor) obj;
		if (black != other.black)
			return false;
		if (cyan != other.cyan)
			return false;
		if (magenta != other.magenta)
			return false;
		if (yellow != other.yellow)
			return false;
		return true;
	}
	
}
