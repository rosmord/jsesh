package jsesh.mdcDisplayer.drawingElements;

import jsesh.mdc.constants.SymbolCodes;

public class PhilologyHelper {
    
    /**
     * Returns the width of a given philological mark.
     * @param philologyType
     * @return
     */
    public static float philologyWidth(int philologyType) {
        // IMPORTANT : this is a hack. The closing codes are twice as large as the opening ones.        
        if (philologyType >= 100) {
            philologyType = philologyType / 2;
        }
        float width = 0;
        switch (philologyType) {
            case SymbolCodes.EDITORADDITION:
            case SymbolCodes.EDITORSUPERFLUOUS:
            case SymbolCodes.ERASEDSIGNS:
            case SymbolCodes.SCRIBEADDITION:
            case SymbolCodes.PREVIOUSLYREADABLE:
            case SymbolCodes.MINORADDITION:
            case SymbolCodes.DUBIOUS:
                width = 4;
                break;
        }
        return width;
    }
}
