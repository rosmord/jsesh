package jsesh.swing.utils;

import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.constants.LexicalSymbolsUtils;
import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.draw.MDCDrawingFacade;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.DrawingSpecificationsImplementation;
import jsesh.mdcDisplayer.preferences.PageLayout;

/**
 * Static icons repository.
 *
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
public class ImageIconFactory {

    private static final ImageIconFactory INSTANCE = new ImageIconFactory();

    public static ImageIconFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Cache for icons. Note that the cache will leak a bit of memory, but only
     * for MDC code... which are a finite set.
     */
    private final HashMap<String, SoftReference<ImageIcon>> iconMap = new HashMap<>();
    private final MDCDrawingFacade mdcDrawingFacade;

    private ImageIconFactory() {
        mdcDrawingFacade = new MDCDrawingFacade();
        DrawingSpecification drawingSpecifications = new DrawingSpecificationsImplementation();
        PageLayout pageLayout = drawingSpecifications.getPageLayout();
        pageLayout.setLeftMargin(0);
        pageLayout.setTopMargin(0);
        drawingSpecifications.setPageLayout(pageLayout);
        mdcDrawingFacade.setDrawingSpecifications(drawingSpecifications);
        mdcDrawingFacade.setPhilologySign(true);
        setCadratHeight(30);
    }

    public synchronized final void setCadratHeight(int cadratHeight) {
        this.iconMap.clear();
        this.mdcDrawingFacade.setCadratHeight(cadratHeight);
    }

    /**
     * @param mdcText
     * @return an image for the given manuel de codage text.
     */
    public synchronized ImageIcon buildImage(String mdcText) {
        ImageIcon imageIcon = null;
        if (iconMap.containsKey(mdcText)) {
            imageIcon = iconMap.get(mdcText).get();
        }
        if (imageIcon == null) {
            BufferedImage bufferedImage = null;
            try {
                bufferedImage = mdcDrawingFacade.createImage(mdcText);
            } catch (MDCSyntaxError e) {
                throw new RuntimeException("Error when parsing " + mdcText, e);
            }
            imageIcon = new ImageIcon(bufferedImage);
            iconMap.put(mdcText, new SoftReference<>(imageIcon));
        }
        return imageIcon;
    }

    /**
     * Build the picture of a single symbol (not real glyphs, usually
     * parenthesis and the like).
     *
     * @param symbolCode the code for the symbol, from {@link SymbolCodes}
     * @return an image for the given manuel de codage text.
     */
    public ImageIcon buildImage(int symbolCode) {
        String mdc = LexicalSymbolsUtils.getCodeForLexicalItem(symbolCode);
        return buildImage(mdc);
    }

    /**
     * Build a picture for a given glyph.
     *
     * TODO : cleanup to avoid pasted code.
     *
     * @param code
     * @return
     */
    public Icon buildGlyphImage(String code) {
        ImageIcon imageIcon = null;
        if (iconMap.containsKey(code)) {
            imageIcon = iconMap.get(code).get();
        }
        if (imageIcon == null) {
            BufferedImage bufferedImage;

            TopItemList text = new TopItemList();
            text.addTopItem(new Hieroglyph(code).buildTopItem());
            bufferedImage = mdcDrawingFacade.createImage(text);

            imageIcon = new ImageIcon(bufferedImage);
            iconMap.put(code, new SoftReference<>(imageIcon));
        }

        return imageIcon;
    }

}
