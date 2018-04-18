/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
package jsesh.jhotdraw.applicationPreferences.ui;

import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.Messages;
import jsesh.jhotdraw.utils.PanelBuilder;
import jsesh.mdcDisplayer.clipboard.MDCClipboardPreferences;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class JClipboardFormatSelector {
	private JPanel panel = new JPanel();
	private JEditorPane messageArea;
	private JCheckBox rtfCheckBox, pdfCheckBox, emfCheckBox, bitmapCheckBox, plainTextBox;

	public JClipboardFormatSelector() {
		init();
		layout();
	}

	private void init() {
		rtfCheckBox = new JCheckBox(
				Messages.getString("clipboardFormat.rtf.text"));
		pdfCheckBox = new JCheckBox(
				Messages.getString("clipboardFormat.pdf.text"));
		emfCheckBox = new JCheckBox(
				Messages.getString("clipboardFormat.emf.text"));
		bitmapCheckBox = new JCheckBox(
				Messages.getString("clipboardFormat.bitmap.text"));
		plainTextBox = new JCheckBox(
				Messages.getString("clipboardFormat.plaintext.text"));
		messageArea = new JEditorPane("text/html",
				Messages.getString("clipboardFormat.explanation.text"));

		messageArea.setEditable(false);
		messageArea.setBackground(panel.getBackground());
	}

	public JCheckBox getBitmapCheckBox() {
		return bitmapCheckBox;
	}

	public JCheckBox getPdfCheckBox() {
		return pdfCheckBox;
	}

	public JCheckBox getPlainTextBox() {
		return plainTextBox;
	}

	public JCheckBox getRtfCheckBox() {
		return rtfCheckBox;
	}

	private void layout() {
		panel.setLayout(new MigLayout());
		PanelBuilder helper = new PanelBuilder(panel);
		helper.add(messageArea, "wrap");
		helper.add(rtfCheckBox, "wrap");
		helper.add(emfCheckBox, "wrap");
		helper.add(pdfCheckBox, "wrap");
		helper.add(bitmapCheckBox, "wrap");
		helper.add(plainTextBox, "wrap");
	}

	public JPanel getPanel() {
		return panel;
	}

	public static void main(String[] args) throws InterruptedException,
			InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.add(new JClipboardFormatSelector().getPanel());
				frame.pack();
				frame.setVisible(true);
			}
		});
	}

	public void loadPreferences(JSeshApplicationModel app) {
		MDCClipboardPreferences clipboardPreferences = app
				.getClipboardPreferences();
		this.rtfCheckBox.setSelected(clipboardPreferences.isRtfWanted());
		this.emfCheckBox.setSelected(clipboardPreferences.isEmfWanted());
		this.pdfCheckBox.setSelected(clipboardPreferences.isPdfWanted());
		this.bitmapCheckBox.setSelected(clipboardPreferences.isImageWanted());
		this.plainTextBox.setSelected(clipboardPreferences.isTextWanted());
	}

	public void updatePreferences(JSeshApplicationModel app) {
		MDCClipboardPreferences prefs = new MDCClipboardPreferences()
				.withRtfWanted(rtfCheckBox.isSelected())
				.withEmfWanted(emfCheckBox.isSelected())
				.withPdfWanted(pdfCheckBox.isSelected())
				.withImageWanted(bitmapCheckBox.isSelected())
				.withTextWanted(plainTextBox.isSelected());
		app.setClipboardPreferences(prefs);
	}
}
