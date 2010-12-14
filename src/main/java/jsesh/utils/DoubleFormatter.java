package jsesh.utils;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class DoubleFormatter {
	DecimalFormat format;

	public DoubleFormatter() {
		this(4);
	}

	public DoubleFormatter(int precision) {
		buildFormat(precision);
	}

	/**
	 * Writes a space separated list of floats.
	 * The elements are a part of an array.
	 * @param out
	 * @param t an array of float
	 * @param length the number of elements to write.
	 * @throws IOException
	 */
	public void outputNumbers(Writer out, float[] t, int length)
			throws IOException {
		for (int i = 0; i < length; i++) {
			// out.write(DoubleUtils.writeInDecimal(coords[i]));
			writeTo(out, t[i]);
			out.write(' ');
		}
	}

	private void buildFormat(int precision) {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		// Ensures we use english conventions
		symbols.setMinusSign('-');
		symbols.setZeroDigit('0');
		symbols.setDecimalSeparator('.');

		String formatString = "#";
		if (precision > 0) {
			formatString = formatString + ".";
			for (int i = 0; i < precision; i++) {
				formatString += "#";
			}
		}

		format = new DecimalFormat(formatString, symbols);

	}

	public void writeTo(Writer writer, double d) throws IOException {
		writer.write(toDecimal(d));
	}

	/**
	 * Force writing for this double in simple computer-readable decimal format.
	 * 
	 */
	public String toDecimal(double d) {
		return format.format(d);
	}
}
