package jsesh.render.context;

import java.util.function.Function;

import jsesh.model.ZoneParameters;
import jsesh.model.constants.TextOrientation;
import jsesh.render.style.JSeshStyle;
import jsesh.render.style.LayoutSettingsApplier;
import jsesh.render.style.PageSpecification;
import jsesh.glyphs.fonts.HieroglyphShapeRepository;

/**
 * A place to store rendering context technical information.
 * Currently, only FontRenderContext is stored here.
 * If it appears that it's always the only thing needed, we might
 * remove this class and use FontRenderContext directly.
 * The main point of this class is to be able to pass rendering context information
 * 
 * @param jseshStyle the style in use.
 * @param hieroglyphShapeRepository the current font catalogue.
 */
public record JSeshRenderContext(JSeshStyle jseshStyle, HieroglyphShapeRepository hieroglyphShapeRepository) {

    /**
     * Returns a copy builder.
     */
    public Builder copy() {
        return new Builder(this);
    }

    /**
     * A builder for JSeshRenderContext.
     * 
     * @author rosmord
     *
     */
    public static class Builder {
        private JSeshStyle jseshStyle;
        private HieroglyphShapeRepository hieroglyphShapeRepository;

        public Builder(JSeshRenderContext original) {
            this.jseshStyle = original.jseshStyle();
            this.hieroglyphShapeRepository = original.hieroglyphShapeRepository();
        }

        public Builder jseshStyle(Function<JSeshStyle.Builder, JSeshStyle.Builder> styleFuntion) {
            this.jseshStyle = styleFuntion.apply(this.jseshStyle.copy()).build();
            return this;
        }

        public Builder jseshStyle(JSeshStyle newStyle) {
            this.jseshStyle = newStyle;
            return this;
        }

        public Builder hieroglyphsDrawer(HieroglyphShapeRepository hieroglyphShapeRepository) {
            this.hieroglyphShapeRepository = hieroglyphShapeRepository;
            return this;
        }

        public JSeshRenderContext build() {
            return new JSeshRenderContext(jseshStyle, hieroglyphShapeRepository);
        }
    }

    /**
     * Create a margin-less copy of this context for use in embedded graphics.
     * @return a copy of this context with margins set to 1 pixel.
     */
    public  JSeshRenderContext marginLessContext() {
        return this.copy()
                .jseshStyle(style -> style.geometry(g -> g
                        .leftMargin(01)
                        .topMargin(01)
                        // A picture of a part of the text is not as tall as a page.
                        .withoutPagination()))
                .build();

    }

    /// The context in which the content of a zone is laid out and drawn.
    ///
    /// Settings which the zone doesn't set are inherited from this context
    /// (see [LayoutSettingsApplier], shared with the document preferences). A
    /// zone has no margins, and is never paged (a page break in a zone is a
    /// line break). A zone in lines wraps at its width, if it has one; a zone
    /// in columns aligns its columns in its height, if it has one. The
    /// alignment of its lines is never ignored: even when the text is wrapped
    /// to the width of the editor, a zone keeps its own width.
    ///
    /// The `scale` of a zone is not part of this context: the content is laid
    /// out at full size, and its view is scaled afterwards. So the lengths of
    /// the zone are in its own quadrats.
    ///
    /// @param p the settings of the zone.
    /// @return the context for the content of the zone.
    public JSeshRenderContext forZone(ZoneParameters p) {
        float unit = jseshStyle.geometry().percentUnit();
        TextOrientation orientation = p.orientation() != null ? p.orientation()
                : jseshStyle.options().textOrientation();
        float wrapWidth = orientation.isHorizontal() && p.width() != ZoneParameters.UNSET ? p.width() * unit : 0f;
        float columnHeight = !orientation.isHorizontal() && p.height() != ZoneParameters.UNSET
                ? p.height() * unit
                : 0f;
        return copy().jseshStyle(s -> {
            s.options(o -> o.paged(false).ignoreLineAlignment(false))
                    .geometry(g -> {
                        g.leftMargin(0).topMargin(0).wrapWidth(wrapWidth).columnHeight(columnHeight)
                                .page(PageSpecification.NONE);
                        if (p.width() != ZoneParameters.UNSET) {
                            g.textWidth(p.width() * unit);
                        }
                        return g;
                    });
            return LayoutSettingsApplier.apply(p.settings(), jseshStyle.geometry().unit(), s);
        }).build();
    }
}
