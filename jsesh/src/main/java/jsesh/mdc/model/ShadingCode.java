package jsesh.mdc.model;

/**
 * Constants which define which part of a quadrant is shaded.
 * 
 * <p>
 * Basically, a simple "or" on the following values :
 * <table>
 * <tr>
 * <td>1</td>
 * <td>2</td>
 * </tr>
 * <tr>
 * <td>4</td>
 * <td>8</td>
 * </tr>
 * </table>
 * 
 * <p> Note that there are two ways to specify shading : in the MdC, the "#1234" system is used.
 * But internally, it's simpler to compute an or-value between the constants declared here.
 * <p> In this case, a loop on all codes can be done by looping from 0 to 15.
 * @author rosmord
 * 
 */
public class ShadingCode {

	public final static int TOP_START = 1;
	public final static int TOP_END = 2;
	public final static int BOTTOM_START = 4;
	public final static int BOTTOM_END = 8;
	public final static int FULL = 15;
	public final static int NONE = 0;

	/**
	 * Helper method to compute the String representation of a shading from its
	 * code.
	 * 
	 * @param shadingMarker
	 *            the symbol used to indicate shading (usually '#' or '##').
	 *            Ommited when there is no shading.
	 * @param code
	 *            a or-combination of {@link #TOP_START}, {@value #TOP_END},
	 *            {@link #BOTTOM_START} and {@link #BOTTOM_END}.
	 * @return
	 */
	public static final String toString(String shadingMarker, int sh) {
		StringBuffer buff = new StringBuffer();
		if (sh != ShadingCode.NONE) {
			buff.append(shadingMarker);
			if ((sh & ShadingCode.TOP_START) != 0)
				buff.append("1");
			if ((sh & ShadingCode.TOP_END) != 0)
				buff.append("2");
			if ((sh & ShadingCode.BOTTOM_START) != 0)
				buff.append("3");
			if ((sh & ShadingCode.BOTTOM_END) != 0)
				buff.append("4");
		}
		return buff.toString();
	}
}
