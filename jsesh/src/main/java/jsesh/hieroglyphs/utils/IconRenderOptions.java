package jsesh.hieroglyphs.utils;


/**
 * Rendering options for {@link HieroglyphPictureBuilder}.
 * 
 * <p>Note about <code>fit</code> : Normally, the 
 * height of the sign is scaled to fit the height of the <code>PictureDimension</code>.
 * If the sign is too wide :
 * <ul>
 * <li> if <code>fit</code> is false, the system will produce a bitmap 
 * with a <em>larger</em> width than requested.
 * <li> if <code>fit</code> is true, the system will reduce the scale of the sign to fit the width of the bitmap.
 * </ul>
 * 
 * @param dimension the dimension of the bitmap, in pixels
 * @param transparent do we want a transparent background ?
 * @param fit if the picture is too large, should its scale be reduced to fit the bitmap?
 * @param padding the size of the border to add around the sign, in pixels.
 */
public record IconRenderOptions(
        PictureDimension dimension,
        boolean transparent,
        /**
         * Should the picture be resized to fit the PictureDimension?
         */
        boolean fit,
        /**
         * Inner padding, in pixels.
         */
        int padding
) {
    
  
        public static final IconRenderOptions DEFAULT = new IconRenderOptions(new PictureDimension(60, 50), false, true, 4);
        

        /**
         * Returns the actual dimension of the picture itself, considering the padding.
         * @return
         */
        public PictureDimension innerDimension() {
                return new PictureDimension(dimension.width() - 2 * padding, dimension.height() - 2 * padding); 
        }

        /**
         * Create a builder initialized with the current values.
         */

        public Builder copy() {
                return new Builder(dimension, transparent, fit, padding);
        }

        public static class Builder {
                private PictureDimension dimension;
                private boolean transparent;
                private boolean fit;
                private int padding;

                public Builder(PictureDimension dimension, boolean transparent, boolean fit, int padding) {
                        this.dimension = dimension;                        
                        this.transparent = transparent;
                        this.fit = fit;
                        this.padding = padding;
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

                public Builder padding(int padding) {
                        this.padding = padding;
                        return this;
                }

                public IconRenderOptions build() {
                        return new IconRenderOptions(dimension, transparent, fit, padding);
                }
        }
}

