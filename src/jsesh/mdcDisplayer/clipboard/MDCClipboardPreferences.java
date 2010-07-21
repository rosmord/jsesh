/*
 * This file is distributed under the LGPL
 * Author: Serge Rosmorduc
 */

package jsesh.mdcDisplayer.clipboard;

/**
 *
 * @author rosmord
 */
public class MDCClipboardPreferences {
    private boolean pdfWanted= false;
    private boolean rtfWanted= true;
    private boolean textWanted= true;
    private boolean imageWanted= false;

    public boolean isImageWanted() {
        return imageWanted;
    }

    public void setImageWanted(boolean imageWanted) {
        this.imageWanted = imageWanted;
    }

    public boolean isPdfWanted() {
        return pdfWanted;
    }

    public void setPdfWanted(boolean pdfWanted) {
        this.pdfWanted = pdfWanted;
    }

    public boolean isRtfWanted() {
        return rtfWanted;
    }

    public void setRtfWanted(boolean rtfWanted) {
        this.rtfWanted = rtfWanted;
    }

    public boolean isTextWanted() {
        return textWanted;
    }

    public void setTextWanted(boolean textWanted) {
        this.textWanted = textWanted;
    }

    
}
