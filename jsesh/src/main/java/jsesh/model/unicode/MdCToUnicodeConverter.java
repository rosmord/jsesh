
/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020)
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license
 * under French law : "http://www.cecill.info".
 */
package jsesh.model.unicode;

import jsesh.model.constants.LexicalSymbolsUtils;
import jsesh.model.constants.SymbolCodes;
import jsesh.model.constants.TextDirection;
import jsesh.model.Cadrat;
import jsesh.model.LineBreak;
import jsesh.model.ModelElementDeepAdapter;
import jsesh.model.PageBreak;
import jsesh.model.HieroglyphicText;
import jsesh.model.Hieroglyph;

/// Converter from _Manuel de Codage_ to Unicode.
///
/// By default, the result is a plain sequence of signs. Optionally, the Unicode 17 Egyptian
/// Hieroglyph Format Controls (chapter 11 of the Unicode Standard) are used to
/// encode the layout of the quadrats, see [#setIncludeFormatControlChars(boolean)] and
/// [UnicodeFormatRenderer] for the details.
///
/// @author rosmord
public class MdCToUnicodeConverter {

    private boolean includeFormatControlChars = false;
    private TextDirection textDirection = TextDirection.LEFT_TO_RIGHT;

    /// Select (or deselect) insertion of Unicode format controls. Most fonts
    /// won't handle them.
    ///
    /// When selected, the converter encodes, using the controls of Unicode 17:
    ///
    /// - the quadrats (`:` and `*`) and their sub-groups (segments);
    /// - insertions (`%insert`), with the container first, then the control, then
    ///   the inserted sign, whatever the reading order of the MdC;
    /// - overlays (`##`), the signs keeping the order of the MdC (the last one is on top);
    /// - mirroring (`\`) and the rotations Unicode knows about (variation selectors);
    /// - shading of signs and quadrats (damage modifiers).
    ///
    /// If one of the signs of an insertion or overlay has no Unicode equivalent, or if the
    /// insertion direction has no Unicode control (`s` and `e`), no control
    /// is used: the signs are written one after the other, in reading order.
    ///
    /// Cartouches are not encoded yet.
    ///
    /// @param includeFormatControlChars true to use the format controls.
    public void setIncludeFormatControlChars(boolean includeFormatControlChars) {
        this.includeFormatControlChars = includeFormatControlChars;
    }

    public boolean isIncludeFormatControlChars() {
        return includeFormatControlChars;
    }

    /// The direction of the text. It matters for rotations only: Unicode rotates
    /// clockwise in left-to-right text, and counterclockwise in right-to-left text.
    /// The default is [TextDirection#LEFT_TO_RIGHT].
    ///
    /// @param textDirection the direction of the text to convert.
    public void setTextDirection(TextDirection textDirection) {
        this.textDirection = textDirection;
    }

    public TextDirection getTextDirection() {
        return textDirection;
    }

    /// Returns a Unicode representation of a text.
    ///
    /// @param topItemList the text to convert.
    /// @return a plain Unicode representation, with format controls if they were selected.
    public String convertToPlainUnicode(HieroglyphicText topItemList) {
        InnerUnicodeConverter converter = new InnerUnicodeConverter();
        topItemList.accept(converter);
        return converter.stringBuilder.toString();
    }

    private class InnerUnicodeConverter extends ModelElementDeepAdapter {

        public StringBuilder stringBuilder = new StringBuilder();

        @Override
        public void visitHieroglyph(Hieroglyph h) {
            int codeType = h.getType();
            switch (codeType) {
                case SymbolCodes.MDCCODE:
                    String unicode = MdcUnicodeTable.INSTANCE.getUnicodeFor(h.getCode());
                    stringBuilder.append(unicode);
                    break;
                case SymbolCodes.SMALLTEXT:
                    stringBuilder.append(h.getCode());
                    break;
                default:
                    stringBuilder.append(LexicalSymbolsUtils.getStringForPhilology(h.getType()));
            }
        }

        @Override
        public void visitCadrat(Cadrat elt) {
            if (includeFormatControlChars) {
                UnicodeFormatRenderer renderer = new UnicodeFormatRenderer(textDirection,
                        // Nested non-quadrat material (text...) is converted the plain way.
                        e -> {
                            InnerUnicodeConverter inner = new InnerUnicodeConverter();
                            e.accept(inner);
                            return inner.stringBuilder.toString();
                        });
                stringBuilder.append(renderer.render(elt));
            } else {
                super.visitCadrat(elt);
            }
        }

        @Override
        public void visitLineBreak(LineBreak b) {
            stringBuilder.append("\n");
        }

        @Override
        public void visitPageBreak(PageBreak b) {
            stringBuilder.append("\n");
        }

    }
}
