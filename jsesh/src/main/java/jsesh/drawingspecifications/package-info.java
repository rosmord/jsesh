/**
 * Specifications for the rendering of manuel de codage texts.
 * 
 * 
 * The most fundamental class is {@link JSeshStyle}, which points to the others.
 * 
 * We group the specifications in four groups:
 * 
 * <ul>
 * <li>GeometrySpecification: everything related to dimensions, from line width to page size</li>
 * <li>ColorSpecification: colors the various colours used in rendering</li>
 * <li>FontSpecification: fonts the fonts for non-hieroglyphic text</li>
 * <li>RenderingOptions: rendering options (shading style, etc.)</li>
 * </ul>
 * 
 * Regarding technical specifications, we use the RenderingContext class to pass them. 
 * @author rosmord
 */
package jsesh.drawingspecifications;
