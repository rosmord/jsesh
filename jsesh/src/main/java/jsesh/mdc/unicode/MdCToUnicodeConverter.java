
/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.mdc.unicode;

import jsesh.mdc.constants.LexicalSymbolsUtils;
import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.HBox;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.LineBreak;
import jsesh.mdc.model.ModelElementDeepAdapter;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.SubCadrat;
import jsesh.mdc.model.TopItemList;

/**
 * Converter from <em>Manuel de Codage</em> to Unicode.
 * <p>
 * The Unicode 12 format Controls (akin to * and : in MdC) are optionally
 * handled.
 *
 * @author rosmord
 */
public class MdCToUnicodeConverter {

    private boolean includeFormatControlChars = false;

    /**
     * Select (or deselect) insertion of Unicode 12 format chars. Most fonts
     * won't handle it.
     *
     * @param includeFormatControlChars
     */
    public void setIncludeFormatControlChars(boolean includeFormatControlChars) {
        this.includeFormatControlChars = includeFormatControlChars;
    }

    /**
     * Returns a plain Unicode representation of a topItemList.
     *
     * @param topItemList
     * @return a plain Unicode representation.
     */
    public String convertToPlainUnicode(TopItemList topItemList) {
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
                for (int i = 0; i < elt.getNumberOfChildren(); i++) {
                    if (i > 0) {
                        stringBuilder.append(Character.toString(0x13430));
                    }
                    elt.getChildAt(i).accept(this);
                }
            } else {
                super.visitCadrat(elt);
            }

        }

        @Override
        public void visitSubCadrat(SubCadrat c) {
            if (includeFormatControlChars) {
                stringBuilder.append(Character.toString(0x13437));
                super.visitSubCadrat(c);
                stringBuilder.append(Character.toString(0x13438));
            } else {
                super.visitSubCadrat(c);
            }
        }

        @Override
        public void visitHBox(HBox b) {
            if (includeFormatControlChars) {
                for (int i = 0; i < b.getNumberOfChildren(); i++) {
                    if (i > 0) {
                        stringBuilder.append(Character.toString(0x13431));
                    }
                    b.getChildAt(i).accept(this);
                }
            } else {
                super.visitHBox(b);
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
