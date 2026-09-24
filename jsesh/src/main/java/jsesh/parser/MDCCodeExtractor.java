package jsesh.parser;

import java.util.LinkedList;
import java.util.List;

import jsesh.parser.ast.AstAbsoluteGroup;
import jsesh.parser.ast.AstBasicItemList;
import jsesh.parser.ast.AstCadrat;
import jsesh.parser.ast.AstCartouche;
import jsesh.parser.ast.AstComplexLigature;
import jsesh.parser.ast.AstDocument;
import jsesh.parser.ast.AstHBox;
import jsesh.parser.ast.AstHieroglyph;
import jsesh.parser.ast.AstInnerGroup;
import jsesh.parser.ast.AstLigature;
import jsesh.parser.ast.AstOverwrite;
import jsesh.parser.ast.AstPhilology;
import jsesh.parser.ast.AstSubCadrat;
import jsesh.parser.ast.AstTopItemList;
import jsesh.parser.ast.AstVisitorAdapter;
import jsesh.signcodes.GardinerCode;
import jsesh.signcodes.ManuelDeCodage;

/**
 * An extractor is able to fetch codes from a manuel de codage STRING
 * (see jsesh.model.tools.HieroglyphExtractor otherwise) and, if needed, to normalize them.
 *
 *
 * @see jsesh.model.tools.HieroglyphExtractor for a class working on already parsed text.
 * @author rosmord
 *
 */
public class MDCCodeExtractor {

    private boolean normalize = true;

    private boolean suppressNonGlyphs = true;

    public String[] getCodes(String manuelDeCodageText) throws MDCSyntaxError {
        List<String> l = getCodesAsList(manuelDeCodageText);
        return l.toArray(new String[l.size()]);
    }

    public List<String> getCodesAsList(String manuelDeCodageText) throws MDCSyntaxError {
        AstDocument document = new MDCParserAstGenerator().parse(manuelDeCodageText);
        HieroglyphCodeCollector collector = new HieroglyphCodeCollector();
        document.accept(collector);
        return collector.result;
    }

    /**
     * Walks the whole AST in document order, collecting the (normalized)
     * code of every {@link AstHieroglyph} it finds.
     */
    private class HieroglyphCodeCollector extends AstVisitorAdapter {

        final List<String> result = new LinkedList<>();

        @Override
        public void visitDocument(AstDocument node) {
            node.topItems().accept(this);
        }

        @Override
        public void visitTopItemList(AstTopItemList node) {
            for (var item : node.items()) {
                item.accept(this);
            }
        }

        @Override
        public void visitBasicItemList(AstBasicItemList node) {
            for (var item : node.items()) {
                item.accept(this);
            }
        }

        @Override
        public void visitCadrat(AstCadrat node) {
            for (AstHBox hBox : node.hBoxes()) {
                hBox.accept(this);
            }
        }

        @Override
        public void visitHBox(AstHBox node) {
            for (var element : node.elements()) {
                element.accept(this);
            }
        }

        @Override
        public void visitCartouche(AstCartouche node) {
            node.content().accept(this);
        }

        @Override
        public void visitLigature(AstLigature node) {
            for (AstHieroglyph hieroglyph : node.hieroglyphs()) {
                hieroglyph.accept(this);
            }
        }

        @Override
        public void visitComplexLigature(AstComplexLigature node) {
            AstInnerGroup before = node.beforeGroup();
            if (before != null) {
                before.accept(this);
            }
            node.hieroglyph().accept(this);
            AstInnerGroup after = node.afterGroup();
            if (after != null) {
                after.accept(this);
            }
        }

        @Override
        public void visitSubCadrat(AstSubCadrat node) {
            node.content().accept(this);
        }

        @Override
        public void visitOverwrite(AstOverwrite node) {
            node.first().accept(this);
            node.second().accept(this);
        }

        @Override
        public void visitPhilology(AstPhilology node) {
            node.content().accept(this);
        }

        @Override
        public void visitAbsoluteGroup(AstAbsoluteGroup node) {
            for (AstHieroglyph hieroglyph : node.hieroglyphs()) {
                hieroglyph.accept(this);
            }
        }

        @Override
        public void visitHieroglyph(AstHieroglyph node) {
            String code = node.code();
            if (GardinerCode.isCanonicalCode(code)) {
                result.add(code);
            } else {
                String canonicalCode = ManuelDeCodage.getInstance().getCanonicalCode(code).code();
                if (!suppressNonGlyphs || GardinerCode.isCanonicalCode(canonicalCode)) {
                    result.add(canonicalCode);
                }
            }
        }
    }

    /**
     * Should we normalize the codes to "Gardiner" code ?
     *
     * @return the normalize
     */
    public boolean isNormalize() {
        return normalize;
    }

    /**
     * Should we normalize the codes to "Gardiner" code ? Note that currently,
     * the codes are always normalized. We are not really interested in non
     * normalized codes, so if you need the alternative, write it.
     *
     * @param normalize
     */
    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    public boolean isSuppressNonGlyphs() {
        return suppressNonGlyphs;
    }

    /**
     * Should we suppress codes that are not in Gardiner form (for instance
     * codes for parenthesis or dots) ?
     *
     * @param suppressNonGlyphs
     */
    public void setSuppressNonGlyphs(boolean suppressNonGlyphs) {
        this.suppressNonGlyphs = suppressNonGlyphs;
    }

    public static void main(String[] args) throws MDCSyntaxError {
        MDCCodeExtractor mDCCodeExtractor = new MDCCodeExtractor();
        String[] l = mDCCodeExtractor.getCodes("i-w-r:a-O-$b-ra-[[-m-]]-p*t:pt-$r");
        for (int i = 0; i < l.length; i++) {
            System.out.print(l[i] + " ");
        }
        System.out.println();
    }
}
