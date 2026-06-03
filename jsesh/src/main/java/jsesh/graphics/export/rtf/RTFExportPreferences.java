/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.graphics.export.rtf;


/**
 * @author rosmord
 *
 */
public record RTFExportPreferences(
        int cadratHeight,
        RTFExportGranularity exportGranularity,
        boolean respectOriginalTextLayout,
        RTFExportGraphicFormat exportGraphicFormat) {


    /**
     * The list of granularities for display.
     */
    public static final RTFExportGranularity[] EXPORT_MODES = {
        RTFExportGranularity.ONE_LARGE_PICTURE,
        RTFExportGranularity.GROUPED_CADRATS,
        RTFExportGranularity.ONE_PICTURE_PER_CADRAT
    };

    /**
     * Creates a default RTF export preferences object.
     * 
     * The default values are :
     * <ul>
     * <li>cadrat height : 20
     * <li>granularity : one picture per cadrat
     * <li>respect original text layout : true
     * <li>export graphic format : default format for the current platform.
     * </ul>
     */
    public RTFExportPreferences() {
        this(20, RTFExportGranularity.ONE_PICTURE_PER_CADRAT, true, RTFExportGraphicFormat.DEFAULT);
    }
     
    public Builder copy() {
        return new Builder(this);
    }

    // Builder
    public static final class Builder {
        private int cadratHeight = 20;
        private RTFExportGranularity exportGranularity = RTFExportGranularity.ONE_PICTURE_PER_CADRAT;
        private boolean respectOriginalTextLayout = true;
        private RTFExportGraphicFormat exportGraphicFormat = RTFExportGraphicFormat.DEFAULT;

        private Builder(RTFExportPreferences original) {
            this.cadratHeight = original.cadratHeight;
            this.exportGranularity = original.exportGranularity;
            this.respectOriginalTextLayout = original.respectOriginalTextLayout;
            this.exportGraphicFormat = original.exportGraphicFormat;
        }

        
        public Builder cadratHeight(int height) {
            this.cadratHeight = height;
            return this;
        }

        public Builder exportGranularity(RTFExportGranularity granularity) {
            this.exportGranularity = granularity;
            return this;
        }

        public Builder respectOriginalTextLayout(boolean respectOriginalTextLayout) {
            this.respectOriginalTextLayout = respectOriginalTextLayout;
            return this;
        }

        public Builder exportGraphicFormat(RTFExportGraphicFormat exportGraphicFormat) {
            this.exportGraphicFormat = exportGraphicFormat;
            return this;
        }

        public RTFExportPreferences build() {
            return new RTFExportPreferences(cadratHeight, exportGranularity, respectOriginalTextLayout, exportGraphicFormat);
        }
    }
   
    
}
