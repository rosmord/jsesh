package jsesh.drawingspecifications;

import jsesh.drawingspecifications.graphical.ColorSpec;
import jsesh.drawingspecifications.graphical.DeviceSpec;
import jsesh.drawingspecifications.graphical.FontsSpec;
import jsesh.drawingspecifications.graphical.StrokeSpec;

public record GraphicalSpecs (
    ColorSpec colorSpec,
    DeviceSpec deviceSpec,
    FontsSpec fontsSpec,
    StrokeSpec strokeSpec
){

    public Builder copy() {
        return new Builder(this);
    }
    
    public static class Builder {
        private ColorSpec colorSpec;
        private DeviceSpec deviceSpec;
        private FontsSpec fontsSpec;
        private StrokeSpec strokeSpec;

        public Builder(GraphicalSpecs specs) {
            this.colorSpec = specs.colorSpec;
            this.deviceSpec = specs.deviceSpec;
            this.fontsSpec = specs.fontsSpec;
            this.strokeSpec = specs.strokeSpec;
        }

        public Builder colorSpec(ColorSpec colorSpec) {
            this.colorSpec = colorSpec;
            return this;
        }

        public Builder deviceSpec(DeviceSpec deviceSpec) {
            this.deviceSpec = deviceSpec;
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
            return new GraphicalSpecs(colorSpec, deviceSpec, fontsSpec, strokeSpec);
        }
    }
}
