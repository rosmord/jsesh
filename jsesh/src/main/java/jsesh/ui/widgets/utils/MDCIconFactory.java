package jsesh.ui.widgets.utils;

import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import jsesh.render.style.JSeshStyle;
import jsesh.glyphs.fonts.HieroglyphShapeRepository;
import jsesh.parser.MDCSyntaxError;
import jsesh.model.constants.LexicalSymbolsUtils;
import jsesh.model.constants.SymbolCodes;
import jsesh.model.Hieroglyph;
import jsesh.model.TopItemList;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.draw.MDCDrawingFacade;

/**
 * Icons Factory and Cache.
 * 
 * Will build icons for Mdc text, and cache them for later use.
 *
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
public class MDCIconFactory {


    /**
     * Cache for icons. Note that the cache will leak a bit of memory, but only
     * for MDC code... which are a finite set.
     */
    private final HashMap<String, SoftReference<ImageIcon>> iconMap = new HashMap<>();
    private final MDCDrawingFacade mdcDrawingFacade;

    public MDCIconFactory(HieroglyphShapeRepository hieroglyphShapeRepository) {
        JSeshStyle jseshStyle = 
            JSeshStyle.DEFAULT.copy()
            .geometry(
                g -> g.topMargin(0)
                    .leftMargin(0)
            )            
            .build();
        JSeshRenderContext ctx = new JSeshRenderContext(jseshStyle, hieroglyphShapeRepository);
        mdcDrawingFacade = new MDCDrawingFacade(ctx);
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

    /**
     * Options for icon creation.
     */
    public static record IconOption() {}

    /**
     * Clear the cache of icons.
     */
    public void clearCache() {
        iconMap.clear();
    }
}
