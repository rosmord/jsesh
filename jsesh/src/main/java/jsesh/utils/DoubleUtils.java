package jsesh.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Utility class for double and floats alike
 * @author rosmord
 * @deprecated
 */
public class DoubleUtils {
	/**
	 * Force writing for this double in simple computer-readable decimal format. 
	 * 
	 */
	public static String writeInDecimal(double d) {
		DecimalFormatSymbols symbols= new DecimalFormatSymbols();
		// Ensures we use english conventions
		symbols.setMinusSign('-');
		symbols.setZeroDigit('0');
		symbols.setDecimalSeparator('.');
		
		DecimalFormat format= new DecimalFormat("#.######",symbols);
		return format.format(d);
	}
}
