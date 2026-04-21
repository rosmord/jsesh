package jsesh.jhotdraw;

import jsesh.JSeshUserSignLibraryConfiguration;

/**
 * Data which must be created before starting the application for real.
 * Currently, the preloading step doesn't return anything.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 *
 */
public record JSeshApplicationStartingData(
    /**
     * Preferred height for icons.
     */
    int iconHeight,
    /**
     * JSesh configuration, mostly about fonts.
     */
    JSeshUserSignLibraryConfiguration applicationDefaults    
) {

}
