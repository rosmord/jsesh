package jsesh.parser.ast;

/**
 * A no-op implementation of {@link AstVisitor}: every method delegates to
 * {@link #visitDefault(AstNode)}, which does nothing. Override only the
 * methods you care about, or {@code visitDefault} to handle several node
 * types uniformly.
 *
 * @author rosmord
 */
public class AstVisitorAdapter implements AstVisitor {

    protected void visitDefault(AstNode node) {
    }

    @Override
    public void visitDocument(AstDocument node) {
        visitDefault(node);
    }

    @Override
    public void visitTopItemList(AstTopItemList node) {
        visitDefault(node);
    }

    @Override
    public void visitBasicItemList(AstBasicItemList node) {
        visitDefault(node);
    }

    @Override
    public void visitCadrat(AstCadrat node) {
        visitDefault(node);
    }

    @Override
    public void visitHBox(AstHBox node) {
        visitDefault(node);
    }

    @Override
    public void visitHieroglyph(AstHieroglyph node) {
        visitDefault(node);
    }

    @Override
    public void visitCartouche(AstCartouche node) {
        visitDefault(node);
    }

    @Override
    public void visitLigature(AstLigature node) {
        visitDefault(node);
    }

    @Override
    public void visitComplexLigature(AstComplexLigature node) {
        visitDefault(node);
    }

    @Override
    public void visitSubCadrat(AstSubCadrat node) {
        visitDefault(node);
    }

    @Override
    public void visitOverwrite(AstOverwrite node) {
        visitDefault(node);
    }

    @Override
    public void visitPhilology(AstPhilology node) {
        visitDefault(node);
    }

    @Override
    public void visitAbsoluteGroup(AstAbsoluteGroup node) {
        visitDefault(node);
    }

    @Override
    public void visitAlphabeticText(AstAlphabeticText node) {
        visitDefault(node);
    }

    @Override
    public void visitHRule(AstHRule node) {
        visitDefault(node);
    }

    @Override
    public void visitLineBreak(AstLineBreak node) {
        visitDefault(node);
    }

    @Override
    public void visitPageBreak(AstPageBreak node) {
        visitDefault(node);
    }

    @Override
    public void visitTabStop(AstTabStop node) {
        visitDefault(node);
    }

    @Override
    public void visitSuperscript(AstSuperscript node) {
        visitDefault(node);
    }

    @Override
    public void visitToggle(AstToggle node) {
        visitDefault(node);
    }

    @Override
    public void visitStartHieroglyphicText(AstStartHieroglyphicText node) {
        visitDefault(node);
    }

    @Override
    public void visitZoneStart(AstZoneStart node) {
        visitDefault(node);
    }

    @Override
    public void visitTabbing(AstTabbing node) {
        visitDefault(node);
    }

    @Override
    public void visitTabbingClear(AstTabbingClear node) {
        visitDefault(node);
    }

    @Override
    public void visitModifierList(AstModifierList node) {
        visitDefault(node);
    }

    @Override
    public void visitModifier(AstModifier node) {
        visitDefault(node);
    }
}
