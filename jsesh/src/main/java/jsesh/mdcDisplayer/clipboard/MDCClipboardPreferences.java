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
package jsesh.mdcDisplayer.clipboard;

import java.util.prefs.Preferences;

import org.qenherkhopeshef.utils.PlatformDetection;

/**
 * Information about the clipboard (Immutable class).
 * <p>
 * New convention : this class is now immutable. We will use "with" instead of
 * "set" for stuff which looks like setters.
 * 
 * <p>
 * Note : this change is not really interesting here, as clipboard settings made
 * in the "usual" way are not really a problem.
 * <p>
 * To mutate the object, use
 * 
 * <pre>
 * MDCClipboardPreferences pref = new MDCClipboardPreferences();
 * pref = pref.withImageWanted(true);
 * </pre>
 * 
 * or, shorter :
 * 
 * <pre>
 * MDCClipboardPreferences pref = new MDCClipboardPreferences()
 * 		.withImageWanted(true);
 * </pre>
 * 
 * (etc...)
 * 
 * @author rosmord
 */

public class MDCClipboardPreferences {
	private boolean pdfWanted = false;
	private boolean rtfWanted = true;
	private boolean textWanted = true;
	private boolean imageWanted = false;

	public MDCClipboardPreferences() {
	}

	private MDCClipboardPreferences(MDCClipboardPreferences orig) {
		this.pdfWanted = orig.pdfWanted;
		this.rtfWanted = orig.rtfWanted;
		this.textWanted = orig.textWanted;
		this.imageWanted = orig.imageWanted;
	}

	public boolean isImageWanted() {
		return imageWanted;
	}

	public MDCClipboardPreferences withImageWanted(boolean imageWanted) {
		MDCClipboardPreferences result = new MDCClipboardPreferences(this);
		result.imageWanted = imageWanted;
		return result;
	}

	public boolean isPdfWanted() {
		return pdfWanted;
	}

	public MDCClipboardPreferences withPdfWanted(boolean pdfWanted) {
		MDCClipboardPreferences result = new MDCClipboardPreferences(this);
		result.pdfWanted = pdfWanted;
		return result;
	}

	public boolean isRtfWanted() {
		return rtfWanted;
	}

	public MDCClipboardPreferences withRtfWanted(boolean rtfWanted) {
		MDCClipboardPreferences result = new MDCClipboardPreferences(this);
		result.rtfWanted = rtfWanted;
		return result;
	}

	public boolean isTextWanted() {
		return textWanted;
	}

	public MDCClipboardPreferences withTextWanted(boolean textWanted) {
		MDCClipboardPreferences result = new MDCClipboardPreferences(this);
		result.textWanted = textWanted;
		return result;
	}

	public void saveToPrefs(Preferences prefs) {
		prefs.putBoolean("rtfWanted", rtfWanted);
		prefs.putBoolean("pdfWanted", pdfWanted);
		prefs.putBoolean("imageWanted", imageWanted);
		prefs.putBoolean("textWanted", this.textWanted);
	}

	public static MDCClipboardPreferences getFromPreferences(Preferences prefs) {
		return new MDCClipboardPreferences()
				.withRtfWanted(prefs.getBoolean("rtfWanted", true))
				.withPdfWanted(
						prefs.getBoolean(
								"pdfWanted",
								PlatformDetection.getPlatform() == PlatformDetection.MACOSX))
				.withImageWanted(prefs.getBoolean("imageWanted", false))
				.withTextWanted(prefs.getBoolean("textWanted", false));
	}
}
