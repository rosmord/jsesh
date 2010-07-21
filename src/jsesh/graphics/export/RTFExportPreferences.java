/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.graphics.export;

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

        public static final RTFExportGranularity ONE_LARGE_PICTURE = new RTFExportGranularity(0, "as one large picture");
        public static final RTFExportGranularity GROUPED_CADRATS = new RTFExportGranularity(1, "grouped cadrats");
        public static final RTFExportGranularity ONE_PICTURE_PER_CADRAT = new RTFExportGranularity(2, "one picture per cadrat");
        public static final RTFExportGranularity[] GRANULARITIES = new RTFExportGranularity[]{
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

    public static class RTFExportGraphicFormat extends EnumBase {

        public static final RTFExportGraphicFormat DEFAULT = new RTFExportGraphicFormat(0, "DEFAULT");
        public static final RTFExportGraphicFormat WMF = new RTFExportGraphicFormat(1, "WMF");
        public static final RTFExportGraphicFormat EMF = new RTFExportGraphicFormat(2, "EMF");
        public static final RTFExportGraphicFormat MACPICT = new RTFExportGraphicFormat(3, "MACPICT");

        /**
         * returns the format with id "id".
         * @param aInt
         * @return
         */
        public static RTFExportGraphicFormat getMode(int id) {
            RTFExportGraphicFormat formats[]= {
              DEFAULT, WMF, EMF, MACPICT
            };
            return formats[id];
        }

        private RTFExportGraphicFormat(int id, String designation) {
            super(id, designation);
        }
    }

    /**
     * The list of formats for display.
     */
    public static final RTFExportGraphicFormat graphicFormatList[] = {
        RTFExportGraphicFormat.DEFAULT, RTFExportGraphicFormat.MACPICT, RTFExportGraphicFormat.WMF, RTFExportGraphicFormat.EMF
    };

    /**
     * The list of granularities for display.
     */
    public static final RTFExportGranularity exportModes[] = {
        RTFExportGranularity.ONE_LARGE_PICTURE,
        RTFExportGranularity.GROUPED_CADRATS,
        RTFExportGranularity.ONE_PICTURE_PER_CADRAT
    };

    private RTFExportGranularity exportGranularity;
    private int cadratHeight;
    private boolean respectOriginalTextLayout = true;
    private RTFExportGraphicFormat exportGraphicFormat= RTFExportGraphicFormat.DEFAULT;
    
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
        exportGranularity = RTFExportGranularity.ONE_PICTURE_PER_CADRAT;
        cadratHeight = 20; // 20 points
    }

    /**
     * @return Returns the cadratHeight (in points).
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

    public void setRespectOriginalTextLayout(boolean respectOriginalTextLayout) {
        this.respectOriginalTextLayout = respectOriginalTextLayout;
    }

    public boolean respectOriginalTextLayout() {
        return respectOriginalTextLayout;
    }

    public RTFExportGraphicFormat getExportGraphicFormat() {
        return exportGraphicFormat;
    }

    public void setExportGraphicFormat(RTFExportGraphicFormat exportGraphicFormat) {
        this.exportGraphicFormat =  exportGraphicFormat;
    }

    
}
