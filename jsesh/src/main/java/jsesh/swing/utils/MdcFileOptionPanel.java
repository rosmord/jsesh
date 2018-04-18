package jsesh.swing.utils;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * A panel for entering options related to Manuel de Codage file loading and saving.
 * @author rosmord
 *
 */

public class MdcFileOptionPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1018680394289021735L;

	private static final String ENCODING_TIP = "(a bit technical). Sets the text encoding for the manuel de codage file.";
		
		/*"Manuel de codage files are text files,\n" +
			" and text files come in various flavours, called 'encoding', which is the reason\n" +
			" why you sometime see funny characters in your emails where you should have accents and the like.\n" +
			" Normally, you should not bother with encodings while you are reading texts you wrote,\n" +
			" but if you receive a manuel de codage text where accents are not readable, just try to choose the\n" +
			"right encoding. Well, it's some kind of expert option. With some luck, MdC 2007 will provide us with a general solution.\n";
			*/
	
	private JComboBox encodingField;
	
	
	public MdcFileOptionPanel() {
		// create the panel. 
		encodingField= new JComboBox(buildEncodingsList());
		encodingField.setToolTipText(ENCODING_TIP);
		prepareLayout();
	}


	@SuppressWarnings("unchecked")
	private Vector<String> buildEncodingsList() {
		// Build a list of all available encoding, with prefered values first.
		Vector<String> listOfEncodings= new Vector<String>();
		listOfEncodings.addAll(Arrays.asList(new String[] {"Default", "UTF-8", "ISO-8859-15", "x-MacRoman"}));
		Vector otherList= new Vector(Charset.availableCharsets().keySet());
		otherList.removeAll(listOfEncodings);
		listOfEncodings.addAll(otherList);
		return listOfEncodings;
	}
	
	public String getEncoding() {
		return (String) encodingField.getSelectedItem();
	}


	public void setEncoding(String encoding) {
		if (encoding.equals(System.getProperty("file.encoding")))
			encodingField.setSelectedIndex(0);
		else
			encodingField.setSelectedItem(encoding);
	}
	
	private void prepareLayout() {
		add("File encoding",encodingField);
	}
}
