package jsesh.drawingspecifications;

import jsesh.drawingspecifications.graphical.ColorSpec;
import jsesh.drawingspecifications.graphical.FontsSpec;
import jsesh.drawingspecifications.graphical.StrokeSpec;

/**
 * Specifications not related to layout.
 * 
 * @param colorSpec colors the various colours used in rendering
 * @param fontsSpec fonts the fonts for non-hieroglyphic text
 * @param strokeSpec stroke specifications line width and shading style.
 */
public record GraphicalSpecs (
    ColorSpec colorSpec,
    FontsSpec fontsSpec,
    StrokeSpec strokeSpec
){

    public Builder copy() {
        return new Builder(this);
    }
    
    public static class Builder {
        private ColorSpec colorSpec;
        private FontsSpec fontsSpec;
        private StrokeSpec strokeSpec;

        public Builder(GraphicalSpecs specs) {
            this.colorSpec = specs.colorSpec;
            this.fontsSpec = specs.fontsSpec;
            this.strokeSpec = specs.strokeSpec;
        }

        public Builder colorSpec(ColorSpec colorSpec) {
            this.colorSpec = colorSpec;
            return this;
        }


        public Builder fontsSpec(FontsSpec fontsSpec) {
            this.fontsSpec = fontsSpec;
            return this;
        }

        public Builder strokeSpec(StrokeSpec strokeSpec) {
            this.strokeSpec = strokeSpec;
            return this;
        }

        public GraphicalSpecs build() {
            return new GraphicalSpecs(colorSpec, fontsSpec, strokeSpec);
        }
    }
}
