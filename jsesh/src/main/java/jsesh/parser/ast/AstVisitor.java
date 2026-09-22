package jsesh.parser.ast;

/**
 * Visitor for the literal Manuel de Codage AST, one method per node type.
 * Mirrors {@link jsesh.model.ModelElementVisitor}.
 *
 * @author rosmord
 * @see AstVisitorAdapter
 */
public interface AstVisitor {

    void visitDocument(AstDocument node);

    void visitTopItemList(AstTopItemList node);

    void visitBasicItemList(AstBasicItemList node);

    void visitCadrat(AstCadrat node);

    void visitHBox(AstHBox node);

    void visitHieroglyph(AstHieroglyph node);

    void visitCartouche(AstCartouche node);

    void visitLigature(AstLigature node);

    void visitComplexLigature(AstComplexLigature node);

    void visitSubCadrat(AstSubCadrat node);

    void visitOverwrite(AstOverwrite node);

    void visitPhilology(AstPhilology node);

    void visitAbsoluteGroup(AstAbsoluteGroup node);

    void visitAlphabeticText(AstAlphabeticText node);

    void visitHRule(AstHRule node);

    void visitLineBreak(AstLineBreak node);

    void visitPageBreak(AstPageBreak node);

    void visitTabStop(AstTabStop node);

    void visitSuperscript(AstSuperscript node);

    void visitToggle(AstToggle node);

    void visitStartHieroglyphicText(AstStartHieroglyphicText node);

    void visitZoneStart(AstZoneStart node);

    void visitTabbing(AstTabbing node);

    void visitTabbingClear(AstTabbingClear node);

    void visitModifierList(AstModifierList node);

    void visitModifier(AstModifier node);
}
