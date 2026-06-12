package jsesh.hieroglyphs.utils;


/**
 * Rendering options for {@link HieroglyphPictureBuilder}.
 * 
 * <p> TODO : fit is not a good name. fixedSize would be easier to understand (and it would be !fit) 
 * @param dimension the dimension of the bitmap, in pixels
 * @param transparent do we want a transparent picture ?
 * @param fit the dimension of the bitmap depends on the dimension of the sign.
 * @param border the size of the border to add around the sign, in pixels.
 */
public record IconRenderOptions(
        PictureDimension dimension,        
        boolean transparent,
        boolean fit,
        int border
) {
    
  
        public static final IconRenderOptions DEFAULT = new IconRenderOptions(new PictureDimension(50, 50), false, false, 4);
        
        /**
         * Create a builder initialized with the current values.
         */

        public Builder copy() {
                return new Builder(dimension, transparent, fit, border);
        }

        public static class Builder {
                private PictureDimension dimension;
                private boolean transparent;
                private boolean fit;
                private int border;

                public Builder(PictureDimension dimension, boolean transparent, boolean fit, int border) {
                        this.dimension = dimension;                        
                        this.transparent = transparent;
                        this.fit = fit;
                        this.border = border;
                }

                public Builder dimension(PictureDimension dimension) {
                        this.dimension = dimension;
                        return this;
                }

                 public Builder dimension(int width, int height) {
                        this.dimension = new PictureDimension(width, height);
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
                        return new IconRenderOptions(dimension, transparent, fit, border);
                }
        }
}

