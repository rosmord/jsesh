package jsesh.render.draw;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BaseMultiResolutionImage;
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
import jsesh.model.HieroglyphicText;
import jsesh.render.context.JSeshRenderContext;

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
        mdcDrawingFacade = MDCDrawingFacade.builder(ctx)
                .philologySign(true)
                .cadratHeight(30)
                .build();
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
            double screenScale = getScreenScale();
            try {
                BufferedImage base = mdcDrawingFacade.createImage(mdcText);
                BufferedImage hiDpi = screenScale > 1
                        ? mdcDrawingFacade.createImage(mdcText, screenScale)
                        : null;
                imageIcon = new ImageIcon(combine(base, hiDpi));
            } catch (MDCSyntaxError e) {
                throw new RuntimeException("Error when parsing " + mdcText, e);
            }
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
            HieroglyphicText text = new HieroglyphicText();
            text.addTopItem(new Hieroglyph(code).buildTopItem());
            double screenScale = getScreenScale();
            BufferedImage base = mdcDrawingFacade.createImage(text);
            BufferedImage hiDpi = screenScale > 1
                    ? mdcDrawingFacade.createImage(text, screenScale)
                    : null;
            imageIcon = new ImageIcon(combine(base, hiDpi));
            iconMap.put(code, new SoftReference<>(imageIcon));
        }

        return imageIcon;
    }

    /// Wraps the nominal picture and its high resolution version (if any) in one image.
    ///
    /// Swing picks the variant which matches the screen when it paints the icon,
    /// so the icon is sharp on Retina or scaled Windows screens; its size, for
    /// layout, is the nominal one.
    private static Image combine(BufferedImage base, BufferedImage hiDpi) {
        if (hiDpi == null) {
            return base;
        }
        return new BaseMultiResolutionImage(base, hiDpi);
    }

    /// The largest pixel scale of the connected screens (2 for a Retina screen,
    /// 1.5 for Windows at 150 %...), or 1 if there is no screen.
    private static double getScreenScale() {
        if (GraphicsEnvironment.isHeadless()) {
            return 1;
        }
        double scale = 1;
        for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            scale = Math.max(scale, device.getDefaultConfiguration().getDefaultTransform().getScaleX());
        }
        return scale;
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
