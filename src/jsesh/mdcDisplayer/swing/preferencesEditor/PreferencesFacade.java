/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.preferencesEditor;

import jsesh.graphics.export.RTFExportPreferences;
import jsesh.mdcDisplayer.clipboard.MDCClipboardPreferences;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.swing.units.LengthUnit;

/**
 * Facade class for abstract preferences manipulation.
 *
 */
public class PreferencesFacade {

	private RTFExportPreferences rtfExportPreferences[];

	private LengthUnit preferedUnit;

	private DrawingSpecification drawingSpecifications;

    private MDCClipboardPreferences clipboardPreferences;
	
	
	/**
     * @param drawingSpecifications
     * @param unit
     * @param preferences
     * @param clipboardPreferences 
	 */
	public PreferencesFacade(DrawingSpecification drawingSpecifications, LengthUnit unit, RTFExportPreferences[] preferences, MDCClipboardPreferences clipboardPreferences) {
		this.drawingSpecifications= drawingSpecifications;
		preferedUnit = unit;
		rtfExportPreferences = preferences;
        this.clipboardPreferences= clipboardPreferences;
		for (int i=0; i< rtfExportPreferences.length; i++)
			System.out.println(rtfExportPreferences[i].getCadratHeight());
	}

	/**
     * @param preferenceIndex
     * @return 
	 */
	public RTFExportPreferences getRTFPreference(int preferenceIndex) {
		return rtfExportPreferences[preferenceIndex];
	}

	/**
	 * @return Returns the preferedUnit.
	 */
	public LengthUnit getPreferedUnit() {
		return preferedUnit;
	}

	/**
	 * @param preferedUnit The preferedUnit to set.
	 */
	public void setPreferedUnit(LengthUnit preferedUnit) {
		this.preferedUnit = preferedUnit;
	}

	/**
	 * @return
	 */
	public DrawingSpecification getDrawingSpecifications() {
		return drawingSpecifications;
	}

    public MDCClipboardPreferences getClipboardPreferences() {
        return clipboardPreferences;
    }

    
}
