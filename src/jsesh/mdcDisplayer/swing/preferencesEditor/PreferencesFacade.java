/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.preferencesEditor;

import jsesh.graphicExport.RTFExportPreferences;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;
import jsesh.mdcDisplayer.swing.units.LengthUnit;

/**
 * Facade class for abstract preferences manipulation.
 *
 */
public class PreferencesFacade {

	private RTFExportPreferences rtfExportPreferences[];

	private LengthUnit preferedUnit;

	private DrawingSpecifications drawingSpecifications;
	
	
	/**
	 * @param unit
	 * @param preferences
	 */
	public PreferencesFacade(DrawingSpecifications drawingSpecifications, LengthUnit unit, RTFExportPreferences[] preferences) {
		this.drawingSpecifications= drawingSpecifications;
		preferedUnit = unit;
		rtfExportPreferences = preferences;
		for (int i=0; i< rtfExportPreferences.length; i++)
			System.out.println(rtfExportPreferences[i].getCadratHeight());
	}

	/**
	 * @param preferenceIndex
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
	public DrawingSpecifications getDrawingSpecifications() {
		return drawingSpecifications;
	}
	
}
