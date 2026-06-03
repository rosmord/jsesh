package jsesh.hieroglyphs.utils;

/**
 * Rendering options for {@link HieroglyphPictureBuilder}.
 * 
 * <p> TODO : fit is not a good name. fixedSize would be easier to understand (and it would be !fit) 
 * @param size the size of the bitmap, in pixels
 * @param transparent do we want a transparent picture ?
 * @param fit the size of the bitmap depends on the size of the sign.
 * @param border the size of the border to add around the sign, in pixels.
 */
public record IconRenderOptions(
        int size,        
        boolean transparent,
        boolean fit,
        int border
) {
    
  
        public static final IconRenderOptions DEFAULT = new IconRenderOptions(50, false, false, 4);
        /**
         * Create a builder initialized with the current values.
         */

        public Builder copy() {
                return new Builder(size, transparent, fit, border);
        }

        public static class Builder {
                private int size;
                private boolean transparent;
                private boolean fit;
                private int border;

                public Builder(int size, boolean transparent, boolean fit, int border) {
                        this.size = size;                        
                        this.transparent = transparent;
                        this.fit = fit;
                        this.border = border;
                }

                public Builder size(int size) {
                        this.size = size;
                        return this;
                }

                public Builder transparent(boolean transparent) {
                        this.transparent = transparent;
                        return this;
                }

                public Builder fit(boolean fit) {
                        this.fit = fit;
                        return this;
                }

                public Builder border(int border) {
                        this.border = border;
                        return this;
                }

                public IconRenderOptions build() {
                        return new IconRenderOptions(size, transparent, fit, border);
                }
        }
}

