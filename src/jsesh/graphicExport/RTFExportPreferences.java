/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.graphicExport;

import jsesh.utils.EnumBase;

/**
 * @author rosmord
 *
 */
public class RTFExportPreferences {
	/**
	 * Possible modes for RTF export.
	 */

	
	public static class RTFExportGranularity extends EnumBase {

		public static final RTFExportGranularity ONE_LARGE_PICTURE= new RTFExportGranularity(0,"as one large picture");
		public static final RTFExportGranularity GROUPED_CADRATS= new RTFExportGranularity(1,"grouped cadrats" ) ;
		public static final RTFExportGranularity ONE_PICTURE_PER_CADRAT= new RTFExportGranularity(2,"one picture per cadrat" );
		
		public static final RTFExportGranularity[] GRANULARITIES=  new RTFExportGranularity[] {
			ONE_LARGE_PICTURE, GROUPED_CADRATS, ONE_PICTURE_PER_CADRAT
		};
		
		private RTFExportGranularity(int id, String designation) {
			super(id, designation);
		}
		
		
		/**
		 * Returns the granularity associated with a given ID.
		 * @param int1
		 * @return
		 */
		public static RTFExportGranularity getGranularity(int id) {
			return GRANULARITIES[id];
		}		
	}
	
	public static final RTFExportGranularity exportModes[]= {
		RTFExportGranularity.ONE_LARGE_PICTURE,
		RTFExportGranularity.GROUPED_CADRATS,
		RTFExportGranularity.ONE_PICTURE_PER_CADRAT
	};
	
	private RTFExportGranularity exportGranularity;
	private int cadratHeight;
	
	/**
	 * @param height
	 * @param granularity
	 */
	public RTFExportPreferences(int height, RTFExportGranularity granularity) {
		super();
		// TODO Auto-generated constructor stub
		cadratHeight = height;
		exportGranularity = granularity;
	}

	public RTFExportPreferences() {
		exportGranularity= RTFExportGranularity.ONE_PICTURE_PER_CADRAT;
		cadratHeight= 20; // 20 points
	}
	
	/**
	 * @return Returns the cadratHeight.
	 */
	public int getCadratHeight() {
		return cadratHeight;
	}
	/**
	 * @param cadratHeight The cadratHeight to set (in points).
	 */
	public void setCadratHeight(int cadratHeight) {
		this.cadratHeight = cadratHeight;
	}
	/**
	 * @return Returns the exportGranularity.
	 */
	public RTFExportGranularity getExportGranularity() {
		return exportGranularity;
	}
	/**
	 * @param exportGranularity The exportGranularity to set.
	 */
	public void setExportGranularity(RTFExportGranularity exportGranularity) {
		this.exportGranularity = exportGranularity;
	}
	
	
}
