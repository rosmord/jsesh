package jsesh.drawingspecifications;

/**
 * Specifications not related to layout.
 * 
 * @param geometry everything related to dimensions, from line width to page size
 * @param colorSpec colors the various colours used in rendering
 * @param fontsSpec fonts the fonts for non-hieroglyphic text
 * @param renderSpec rendering options (shading style, etc.)
 */
public record JSeshStyle (
    GeometrySpecification geometry,
    ColorSpecification colors,
    FontSpecification fonts,
    RenderingOptions options
){

}
