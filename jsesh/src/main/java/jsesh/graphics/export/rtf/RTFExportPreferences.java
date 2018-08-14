/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.graphics.export.rtf;


/**
 * @author rosmord
 *
 */
public class RTFExportPreferences {


    /**
     * The list of granularities for display.
     */
    public static final RTFExportGranularity[] EXPORT_MODES = {
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
