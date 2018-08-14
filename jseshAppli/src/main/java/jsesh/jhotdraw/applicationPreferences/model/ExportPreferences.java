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
package jsesh.jhotdraw.applicationPreferences.model;

import java.util.prefs.Preferences;

import jsesh.graphics.export.rtf.RTFExportGranularity;
import jsesh.graphics.export.rtf.RTFExportGraphicFormat;

public final class ExportPreferences implements Cloneable {

	private RTFExportGranularity rtfExportGranularity = RTFExportGranularity.GROUPED_CADRATS;
	private RTFExportGraphicFormat rtfExportGraphicFormat = RTFExportGraphicFormat.DEFAULT;
	private boolean textLayoutRespected = false;
	private double quadrantHeightSmall = 12;
	private double quadrantHeightFile = 12;
	private double quadrantHeightLarge = 18;
	private double quadrantHeightWysiwyg = 12;

	@Override
	public final ExportPreferences clone() {
		try {
			return (ExportPreferences) super.clone();
		} catch (CloneNotSupportedException e) {
			// Should not happen.
			throw new RuntimeException(e);
		}
	}

	public RTFExportGranularity getGranularity() {
		return rtfExportGranularity;
	}

	public RTFExportGraphicFormat getGraphicFormat() {
		return rtfExportGraphicFormat;
	}

	public boolean isTextLayoutRespected() {
		return textLayoutRespected;
	}

	public double getquadrantHeightSmall() {
		return quadrantHeightSmall;
	}

	public double getquadrantHeightLarge() {
		return quadrantHeightLarge;
	}

	public double getquadrantHeightFile() {
		return quadrantHeightFile;
	}

	public double getquadrantHeightWysiwyg() {
		return quadrantHeightWysiwyg;
	}

	/**
	 * @param rtfExportGranularity
	 *            the rtfExportGranularity to set
	 */
	public void setRTFExportGranularity(
			RTFExportGranularity rtfExportGranularity) {
		this.rtfExportGranularity = rtfExportGranularity;
	}

	/**
	 * @param rtfExportGraphicFormat
	 *            the rtfExportGraphicFormat to set
	 */
	public void setRTFExportGraphicFormat(
			RTFExportGraphicFormat rtfExportGraphicFormat) {
		this.rtfExportGraphicFormat = rtfExportGraphicFormat;
	}

	/**
	 * @param textLayoutRespected
	 *            the textLayoutRespected to set
	 */
	public void setTextLayoutRespected(boolean textLayoutRespected) {
		this.textLayoutRespected = textLayoutRespected;
	}

	/**
	 * @param quadrantHeightSmall
	 *            the quadrantHeightSmall to set
	 */
	public void setQuadrantHeightSmall(double quadrantHeightSmall) {
		this.quadrantHeightSmall = quadrantHeightSmall;
	}

	/**
	 * @param quadrantHeightFile
	 *            the quadrantHeightFile to set
	 */
	public void setQuadrantHeightFile(double quadrantHeightFile) {
		this.quadrantHeightFile = quadrantHeightFile;
	}

	/**
	 * @param quadrantHeightLarge
	 *            the quadrantHeightLarge to set
	 */
	public void setQuadrantHeightLarge(double quadrantHeightLarge) {
		this.quadrantHeightLarge = quadrantHeightLarge;
	}

	/**
	 * @param quadrantHeightWysiwyg
	 *            the quadrantHeightWysiwyg to set
	 */
	public void setQuadrantHeightWysiwyg(double quadrantHeightWysiwyg) {
		this.quadrantHeightWysiwyg = quadrantHeightWysiwyg;
	}

	public void saveToPrefs(Preferences preferences) {
		preferences.putDouble("quadrantHeightFile", quadrantHeightFile);
		preferences.putDouble("quadrantHeightLarge", quadrantHeightLarge);
		preferences.putDouble("quadrantHeightSmall", quadrantHeightSmall);
		preferences.putDouble("quadrantHeightWysiwyg", quadrantHeightWysiwyg);
		preferences
				.putInt("rtfExportGranularity", rtfExportGranularity.getId());
		preferences.putInt("rtfExportGraphicFormat",
				rtfExportGraphicFormat.getId());
		preferences.putBoolean("textLayoutRespected", textLayoutRespected);
	}

	public static ExportPreferences getFromPreferences(Preferences prefs) {
		ExportPreferences r = new ExportPreferences();

		r.quadrantHeightFile = prefs.getDouble("quadrantHeightFile", 12);
		r.quadrantHeightLarge = prefs.getDouble("quadrantHeightLarge", 20);
		r.quadrantHeightSmall = prefs.getDouble("quadrantHeightSmall", 12);
		r.quadrantHeightWysiwyg = prefs.getDouble("quadrantHeightWysiwyg", 12);

		r.rtfExportGranularity = RTFExportGranularity.getGranularity(prefs
				.getInt("rtfExportGranularity", 0));
		r.rtfExportGraphicFormat = RTFExportGraphicFormat.getMode(prefs.getInt(
				"rtfExportGraphicFormat", 0));
		r.textLayoutRespected = prefs.getBoolean("textLayoutRespected", true);
		return r;
	}
}
