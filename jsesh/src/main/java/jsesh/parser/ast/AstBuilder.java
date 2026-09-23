package jsesh.parser.ast;

import jsesh.model.api.AbsoluteGroupInterface;
import jsesh.model.api.BasicItemListInterface;
import jsesh.model.api.CadratInterface;
import jsesh.model.api.CartoucheInterface;
import jsesh.model.api.ComplexLigatureInterface;
import jsesh.model.api.HBoxInterface;
import jsesh.model.api.HieroglyphInterface;
import jsesh.model.api.HorizontalListElementInterface;
import jsesh.model.api.InnerGroupInterface;
import jsesh.model.api.LigatureInterface;
import jsesh.model.api.MDCBuilder;
import jsesh.model.api.MDCFileInterface;
import jsesh.model.api.ModifierListInterface;
import jsesh.model.api.OptionListInterface;
import jsesh.model.api.OverwriteInterface;
import jsesh.model.api.PhilologyInterface;
import jsesh.model.api.SubCadratInterface;
import jsesh.model.api.TopItemListInterface;
import jsesh.model.api.VBoxInterface;
import jsesh.model.api.ZoneStartInterface;
import jsesh.model.constants.ToggleType;
import jsesh.model.constants.WordEndingCode;

/**
 * A {@link MDCBuilder} which builds a literal AST (see {@link AstDocument})
 * instead of the usable document model that {@link jsesh.parser.AstModelBuilder}
 * builds from it. Every construct recognized by the grammar becomes a node,
 * with no further interpretation (toggles are not folded into item state,
 * cadrat and zone options are not discarded, dialect-specific modifier
 * renaming is not applied).
 *
 * @author rosmord
 * @see jsesh.parser.MDCParserAstGenerator
 */
public class AstBuilder implements MDCBuilder {

    private AstDocument result;

    public AstDocument getResult() {
        return result;
    }

    @Override
    public void reset() {
        result = null;
    }

    @Override
    public TopItemListInterface buildTopItemList() {
        return new AstTopItemList();
    }

    @Override
    public BasicItemListInterface buildBasicItemList() {
        return new AstBasicItemList();
    }

    @Override
    public MDCFileInterface buildMDCFileInterface(TopItemListInterface l) {
        result = new AstDocument((AstTopItemList) l);
        return result;
    }

    @Override
    public VBoxInterface buildVBox() {
        return new AstCadrat();
    }

    @Override
    public CadratInterface buildCadrat(VBoxInterface e) {
        return (CadratInterface) e;
    }

    @Override
    public void addToVerticalList(VBoxInterface l, HBoxInterface h) {
        ((AstCadrat) l).addHBox((AstHBox) h);
    }

    @Override
    public void addCadratToTopItemList(TopItemListInterface l, CadratInterface e, int shading) {
        AstCadrat cadrat = (AstCadrat) e;
        cadrat.setShading(shading);
        ((AstTopItemList) l).addItem(cadrat);
    }

    @Override
    public void addCadratToBasicItemList(BasicItemListInterface l, CadratInterface c, int shading) {
        AstCadrat cadrat = (AstCadrat) c;
        cadrat.setShading(shading);
        ((AstBasicItemList) l).addItem(cadrat);
    }

    @Override
    public void setOptionList(CadratInterface result, OptionListInterface e1) {
        ((AstCadrat) result).setOptions((AstOptionList) e1);
    }

    @Override
    public HBoxInterface buildHBox() {
        return new AstHBox();
    }

    @Override
    public void addToHorizontalList(HBoxInterface h, HorizontalListElementInterface elt) {
        ((AstHBox) h).addElement(finishHorizontalListElement(elt));
    }

    @Override
    public HieroglyphInterface buildHieroglyph(boolean isGrammar, int type, String code, ModifierListInterface m,
            int isEnd) {
        WordEndingCode endingCode;
        switch (isEnd) {
            case 1:
                endingCode = WordEndingCode.WORD_END;
                break;
            case 2:
                endingCode = WordEndingCode.SENTENCE_END;
                break;
            default:
                endingCode = WordEndingCode.NONE;
                break;
        }
        return new HieroglyphDraft(isGrammar, type, code, (AstModifierList) m, endingCode);
    }

    @Override
    public void setHieroglyphPosition(HieroglyphInterface h, int x, int y, int scale) {
        ((HieroglyphDraft) h).setPosition(x, y, scale);
    }

    /**
     * Turns a still-pending {@link HieroglyphDraft} (or an already-finished
     * {@link AstInnerGroup}) coming from the {@code innerGroup} nonterminal
     * into a real {@link AstHorizontalListElement}.
     */
    private static AstHorizontalListElement finishHorizontalListElement(HorizontalListElementInterface elt) {
        if (elt instanceof HieroglyphDraft draft) {
            return draft.toAstHieroglyph();
        }
        return (AstHorizontalListElement) elt;
    }

    /**
     * Same as {@link #finishHorizontalListElement}, but for the narrower
     * {@code InnerGroupInterface}-typed positions (e.g. the optional "insert
     * zones" of a complex ligature), which may legitimately be {@code null}.
     */
    private static AstInnerGroup finishInnerGroup(InnerGroupInterface group) {
        if (group == null) {
            return null;
        }
        if (group instanceof HieroglyphDraft draft) {
            return draft.toAstHieroglyph();
        }
        return (AstInnerGroup) group;
    }

    /**
     * A bare {@code hieroglyph} nonterminal is always, at the Java level,
     * exactly what {@link #buildHieroglyph} returned: a still-pending
     * {@link HieroglyphDraft}.
     */
    private static AstHieroglyph finishHieroglyph(HieroglyphInterface h) {
        return ((HieroglyphDraft) h).toAstHieroglyph();
    }

    @Override
    public CartoucheInterface buildCartouche(int type, int leftPart, BasicItemListInterface e, int rightPart) {
        return new AstCartouche(type, leftPart, rightPart, (AstBasicItemList) e);
    }

    @Override
    public void addCartoucheToTopItemList(TopItemListInterface l, CartoucheInterface c) {
        ((AstTopItemList) l).addItem((AstCartouche) c);
    }

    @Override
    public LigatureInterface buildLigature() {
        return new AstLigature();
    }

    @Override
    public void addToLigature(LigatureInterface i, HieroglyphInterface h) {
        ((AstLigature) i).addHieroglyph(finishHieroglyph(h));
    }

    @Override
    public void completeLigature(LigatureInterface i) {
        // Nothing to do: unlike the model builder, there is no ligature table
        // lookup to trigger here; the literal AST needs no post-processing.
    }

    @Override
    public ComplexLigatureInterface buildComplexLigature(InnerGroupInterface e1, HieroglyphInterface e2,
            InnerGroupInterface e3) {
        return new AstComplexLigature(finishInnerGroup(e1), finishHieroglyph(e2), finishInnerGroup(e3));
    }

    @Override
    public SubCadratInterface buildSubCadrat(BasicItemListInterface e) {
        return new AstSubCadrat((AstBasicItemList) e);
    }

    @Override
    public OverwriteInterface buildOverwrite(HieroglyphInterface e1, HieroglyphInterface e2) {
        return new AstOverwrite(finishHieroglyph(e1), finishHieroglyph(e2));
    }

    @Override
    public PhilologyInterface buildPhilology(int code1, BasicItemListInterface e, int code2) {
        return new AstPhilology(code1, code2, (AstBasicItemList) e);
    }

    @Override
    public AbsoluteGroupInterface buildAbsoluteGroup() {
        return new AstAbsoluteGroup();
    }

    @Override
    public void addHieroglyphToAbsoluteGroup(AbsoluteGroupInterface group, HieroglyphInterface e) {
        ((AstAbsoluteGroup) group).addHieroglyph(finishHieroglyph(e));
    }

    @Override
    public ModifierListInterface buildModifierList() {
        return new AstModifierList();
    }

    @Override
    public void addModifierToModifierList(ModifierListInterface mods, String name, Integer value) {
        ((AstModifierList) mods).addModifier(name, value);
    }

    @Override
    public void addTextToTopItemList(TopItemListInterface l, char scriptCode, String text) {
        ((AstTopItemList) l).addItem(new AstAlphabeticText(scriptCode, text));
    }

    @Override
    public void addTextToBasicItemList(BasicItemListInterface l, char scriptCode, String text) {
        ((AstBasicItemList) l).addItem(new AstAlphabeticText(scriptCode, text));
    }

    @Override
    public void addTextSuperscriptToTopItemList(TopItemListInterface l, String text) {
        ((AstTopItemList) l).addItem(new AstSuperscript(text));
    }

    @Override
    public void addToggleToTopItemList(TopItemListInterface l, ToggleType toggle) {
        ((AstTopItemList) l).addItem(new AstToggle(toggle));
    }

    @Override
    public void addToggleToBasicItemList(BasicItemListInterface l, ToggleType toggleCode) {
        ((AstBasicItemList) l).addItem(new AstToggle(toggleCode));
    }

    @Override
    public void addStartHieroglyphicTextToTopItemList(TopItemListInterface l) {
        ((AstTopItemList) l).addItem(new AstStartHieroglyphicText());
    }

    @Override
    public void addStartHieroglyphicTextToBasicItemList(BasicItemListInterface l) {
        ((AstBasicItemList) l).addItem(new AstStartHieroglyphicText());
    }

    @Override
    public void addLineBreakToTopItemList(TopItemListInterface l, int skip) {
        ((AstTopItemList) l).addItem(new AstLineBreak(skip));
    }

    @Override
    public void addPageBreakToTopItemList(TopItemListInterface l) {
        ((AstTopItemList) l).addItem(new AstPageBreak());
    }

    @Override
    public void addHRuleToTopItemList(TopItemListInterface l, char lineType, int startPos, int endPos) {
        ((AstTopItemList) l).addItem(new AstHRule(lineType, startPos, endPos));
    }

    @Override
    public void addTabStopToTopItemList(TopItemListInterface l, int stopWidth) {
        ((AstTopItemList) l).addItem(new AstTabStop(stopWidth));
    }

    @Override
    public ZoneStartInterface buildZone() {
        return new ZoneStartDraft();
    }

    @Override
    public void addZoneStartToTopItemList(TopItemListInterface e1, ZoneStartInterface e2) {
        ((AstTopItemList) e1).addItem(((ZoneStartDraft) e2).toAstZoneStart());
    }

    @Override
    public void setOptionList(ZoneStartInterface result, OptionListInterface e1) {
        ((ZoneStartDraft) result).setOptions((AstOptionList) e1);
    }

    @Override
    public void addTabbingToTopItemList(TopItemListInterface e1, OptionListInterface e3) {
        ((AstTopItemList) e1).addItem(new AstTabbing((AstOptionList) e3));
    }

    @Override
    public void addTabbingClearToTopItemList(TopItemListInterface e1) {
        ((AstTopItemList) e1).addItem(new AstTabbingClear());
    }

    @Override
    public OptionListInterface buildOptionList() {
        return new AstOptionList();
    }

    @Override
    public void addOption(OptionListInterface e1, String optName) {
        ((AstOptionList) e1).addOption(optName);
    }

    @Override
    public void addOption(OptionListInterface e1, String optName, String val) {
        ((AstOptionList) e1).addOption(optName, val);
    }

    @Override
    public void addOption(OptionListInterface e1, String optName, int val) {
        ((AstOptionList) e1).addOption(optName, val);
    }
}
