package jsesh.mdcreader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jsesh.model.AbsoluteGroup;
import jsesh.model.AlphabeticText;
import jsesh.model.BasicItem;
import jsesh.model.BasicItemList;
import jsesh.model.Cadrat;
import jsesh.model.Cartouche;
import jsesh.model.ComplexLigature;
import jsesh.model.HBox;
import jsesh.model.HRule;
import jsesh.model.Hieroglyph;
import jsesh.model.HorizontalListElement;
import jsesh.model.InnerGroup;
import jsesh.model.LineBreak;
import jsesh.model.Ligature;
import jsesh.model.Modifier;
import jsesh.model.ModifiersList;
import jsesh.model.Overwrite;
import jsesh.model.PageBreak;
import jsesh.model.Philology;
import jsesh.model.SubCadrat;
import jsesh.model.Superscript;
import jsesh.model.TabStop;
import jsesh.model.Tabbing;
import jsesh.model.TabbingClear;
import jsesh.model.TopItem;
import jsesh.model.TopItemList;
import jsesh.model.TopItemState;
import jsesh.model.constants.Dialect;
import jsesh.model.constants.SymbolCodes;
import jsesh.model.constants.TabbingJustification;
import jsesh.model.constants.TextOrientation;
import jsesh.model.constants.ToggleType;
import jsesh.model.constants.WordEndingCode;
import jsesh.parser.ast.AstAbsoluteGroup;
import jsesh.parser.ast.AstAlphabeticText;
import jsesh.parser.ast.AstBasicItemList;
import jsesh.parser.ast.AstCadrat;
import jsesh.parser.ast.AstCartouche;
import jsesh.parser.ast.AstComplexLigature;
import jsesh.parser.ast.AstDocument;
import jsesh.parser.ast.AstHBox;
import jsesh.parser.ast.AstHRule;
import jsesh.parser.ast.AstHieroglyph;
import jsesh.parser.ast.AstHorizontalListElement;
import jsesh.parser.ast.AstInnerGroup;
import jsesh.parser.ast.AstLigature;
import jsesh.parser.ast.AstLineBreak;
import jsesh.parser.ast.AstModifier;
import jsesh.parser.ast.AstModifierList;
import jsesh.parser.ast.AstNode;
import jsesh.parser.ast.AstOptionList;
import jsesh.parser.ast.AstOverwrite;
import jsesh.parser.ast.AstPageBreak;
import jsesh.parser.ast.AstPhilology;
import jsesh.parser.ast.AstStartHieroglyphicText;
import jsesh.parser.ast.AstSubCadrat;
import jsesh.parser.ast.AstSuperscript;
import jsesh.parser.ast.AstTabStop;
import jsesh.parser.ast.AstTabbing;
import jsesh.parser.ast.AstTabbingClear;
import jsesh.parser.ast.AstToggle;
import jsesh.parser.ast.AstTopItemList;
import jsesh.parser.ast.AstVisitor;
import jsesh.parser.ast.AstZoneStart;
import jsesh.parser.ast.WordEnding;
import jsesh.parser.lexer.PhilologyKind;
import jsesh.parser.lexer.SignSubType;

/// An AstVisitor which knows how to build a [TopItemList] model from an [AstDocument] AST.
/// @author rosmord
/// @see MDCParserModelGenerator
 

class AstModelBuilder implements AstVisitor {

    private final Dialect dialect;
    private final List<Object> stack = new ArrayList<>();
    private final TopItemState currentState = new TopItemState();

    AstModelBuilder(Dialect dialect) {
        this.dialect = dialect;
    }

    TopItemList build(AstDocument document) {
        document.accept(this);
        return pop(TopItemList.class);
    }

    private void push(Object o) {
        stack.add(o);
    }

    private Object pop() {
        return stack.remove(stack.size() - 1);
    }

    private <T> T pop(Class<T> type) {
        return type.cast(pop());
    }

    private <T> T build(AstNode node, Class<T> type) {
        node.accept(this);
        return pop(type);
    }

    @Override
    public void visitDocument(AstDocument node) {
        node.topItems().accept(this);
    }

    @Override
    public void visitTopItemList(AstTopItemList node) {
        TopItemList list = new TopItemList();
        for (AstNode item : node.items()) {
            item.accept(this);
            Object built = pop();
            if (built == null) {
                // Toggles (already folded into currentState by visitToggle),
                // zone starts and the "+s" marker: none of these ever became
                // model items, even under the old MDCModelBuilder.
                continue;
            }
            TopItem topItem = (TopItem) built;
            if (!(item instanceof AstTabbing) && !(item instanceof AstTabbingClear)) {
                topItem.setState(currentState.duplicate());
            }
            list.addTopItem(topItem);
        }
        push(list);
    }

    @Override
    public void visitBasicItemList(AstBasicItemList node) {
        BasicItemList list = new BasicItemList();
        for (AstNode item : node.items()) {
            item.accept(this);
            Object built = pop();
            if (built != null) {
                list.addBasicItem((BasicItem) built);
            }
        }
        push(list);
    }

    @Override
    public void visitCadrat(AstCadrat node) {
        Cadrat cadrat = new Cadrat();
        for (AstHBox hBox : node.hBoxes()) {
            cadrat.addHBox(build(hBox, HBox.class));
        }
        cadrat.setShading(node.shading());
        // node.options() is intentionally ignored here, exactly as
        // MDCModelBuilder's setOptionList(CadratInterface, ...) never
        // interpreted them either.
        push(cadrat);
    }

    @Override
    public void visitHBox(AstHBox node) {
        HBox hBox = new HBox();
        for (AstHorizontalListElement element : node.elements()) {
            hBox.addHorizontalListElement(build(element, HorizontalListElement.class));
        }
        push(hBox);
    }

    @Override
    public void visitHieroglyph(AstHieroglyph node) {
        Hieroglyph hieroglyph = new Hieroglyph(node.code());
        hieroglyph.setGrammar(node.isGrammar());
        hieroglyph.setType(toSignTypeCode(node.type()));
        hieroglyph.setEndingCode(toWordEndingCode(node.endingCode()));
        hieroglyph.setModifiers(build(node.modifiers(), ModifiersList.class));
        // (0, 0, 100) is both AstHieroglyph's "no explicit position" default
        // and Hieroglyph's own default, so skipping the call in that case
        // changes nothing observable.
        if (node.x() != 0 || node.y() != 0 || node.scale() != 100) {
            hieroglyph.setExplicitPosition(node.x(), node.y(), node.scale());
        }
        push(hieroglyph);
    }

    @Override
    public void visitCartouche(AstCartouche node) {
        BasicItemList content = build(node.content(), BasicItemList.class);
        push(new Cartouche(node.type(), node.startPart(), node.endPart(), content));
    }

    @Override
    public void visitLigature(AstLigature node) {
        Ligature ligature = new Ligature();
        for (AstHieroglyph hieroglyph : node.hieroglyphs()) {
            ligature.addHieroglyph(build(hieroglyph, Hieroglyph.class));
        }
        push(ligature);
    }

    @Override
    public void visitComplexLigature(AstComplexLigature node) {
        InnerGroup before = node.beforeGroup() == null ? null : buildInnerGroup(node.beforeGroup());
        Hieroglyph main = build(node.hieroglyph(), Hieroglyph.class);
        InnerGroup after = node.afterGroup() == null ? null : buildInnerGroup(node.afterGroup());
        push(new ComplexLigature(before, main, after));
    }

    private InnerGroup buildInnerGroup(AstInnerGroup group) {
        return build(group, InnerGroup.class);
    }

    @Override
    public void visitSubCadrat(AstSubCadrat node) {
        push(new SubCadrat(build(node.content(), BasicItemList.class)));
    }

    @Override
    public void visitOverwrite(AstOverwrite node) {
        Hieroglyph first = build(node.first(), Hieroglyph.class);
        Hieroglyph second = build(node.second(), Hieroglyph.class);
        push(new Overwrite(first, second));
    }

    @Override
    public void visitPhilology(AstPhilology node) {
        // Only the opening kind is kept, as MDCModelBuilder did: the closing
        // one is normally identical (see AstPhilology's javadoc).
        push(new Philology(philologySubCode(node.openingKind()), build(node.content(), BasicItemList.class)));
    }

    @Override
    public void visitAbsoluteGroup(AstAbsoluteGroup node) {
        AbsoluteGroup group = new AbsoluteGroup();
        for (AstHieroglyph hieroglyph : node.hieroglyphs()) {
            group.addHieroglyph(build(hieroglyph, Hieroglyph.class));
        }
        push(group);
    }

    @Override
    public void visitAlphabeticText(AstAlphabeticText node) {
        push(new AlphabeticText(node.scriptCode(), node.text()));
    }

    @Override
    public void visitHRule(AstHRule node) {
        push(new HRule(node.lineType(), node.startPos(), node.endPos()));
    }

    @Override
    public void visitLineBreak(AstLineBreak node) {
        push(new LineBreak(node.skip()));
    }

    @Override
    public void visitPageBreak(AstPageBreak node) {
        push(new PageBreak());
    }

    @Override
    public void visitTabStop(AstTabStop node) {
        push(new TabStop(node.stopWidth()));
    }

    @Override
    public void visitSuperscript(AstSuperscript node) {
        // Remove protection characters (only in front of "\" and "-").
        String text = node.text().replaceAll("\\\\(\\\\|-)", "$1");
        push(new Superscript(text));
    }

    @Override
    public void visitToggle(AstToggle node) {
        ToggleType toggle = toToggleType(node.toggleType());
        if (toggle == ToggleType.BLACK) {
            currentState.setRed(false);
        } else if (toggle == ToggleType.BLACKRED) {
            currentState.setRed(!currentState.isRed());
        } else if (toggle == ToggleType.RED) {
            currentState.setRed(true);
        } else if (toggle == ToggleType.SHADINGOFF) {
            currentState.setShaded(false);
        } else if (toggle == ToggleType.SHADINGON) {
            currentState.setShaded(true);
        } else if (toggle == ToggleType.SHADINGTOGGLE) {
            currentState.setShaded(!currentState.isShaded());
        }
        // LACUNA, LINELACUNA, OMMIT: not implemented, same as MDCModelBuilder.
        push(null);
    }

    @Override
    public void visitStartHieroglyphicText(AstStartHieroglyphicText node) {
        // Purely a lexer-level marker; MDCModelBuilder discarded it too.
        push(null);
    }

    @Override
    public void visitZoneStart(AstZoneStart node) {
        // MDCModelBuilder never turned zone starts into a model item either
        // (its addZoneStartToTopItemList was an empty stub).
        push(null);
    }

    @Override
    public void visitTabbing(AstTabbing node) {
        AstOptionList options = node.options();
        int id = options.getInt("id", 0);
        String orientationCode = options.getString("orientation", "horizontal");
        String justificationCode = options.getString("justification", "left");
        TextOrientation orientation = TextOrientation.HORIZONTAL;
        if (orientationCode.equalsIgnoreCase(TextOrientation.VERTICAL.toString())) {
            orientation = TextOrientation.VERTICAL;
        }
        TabbingJustification justification = TabbingJustification.valueOf(justificationCode.toUpperCase(Locale.ENGLISH));
        push(new Tabbing(id, justification, orientation));
    }

    @Override
    public void visitTabbingClear(AstTabbingClear node) {
        push(new TabbingClear());
    }

    @Override
    public void visitModifierList(AstModifierList node) {
        ModifiersList modifiersList = new ModifiersList();
        for (AstModifier modifier : node.modifiers()) {
            modifiersList.includeModifier(build(modifier, Modifier.class));
        }
        push(modifiersList);
    }

    @Override
    public void visitModifier(AstModifier node) {
        String name = node.name();
        // Deal with incompatibility between Winglyph and MacScribe: in
        // MacScribe, "r" is the same as "R".
        if ("r".equals(name) && dialect == Dialect.MACSCRIBE) {
            name = "R";
        }
        push(new Modifier(name, node.value()));
    }

    // ------------------------------------------------------------------
    // Lexical value -> model-code translation
    //
    // The AST stores the lexer's own enums, so that jsesh.parser does not
    // depend on jsesh.model; the mapping to jsesh.model.constants lives here.
    // ------------------------------------------------------------------

    /**
     * The {@link SymbolCodes} constant for a hieroglyph's subtype: the plain
     * subtypes map directly, while a philological bracket read as a sign
     * (philologyAsSigns mode) reproduces the original lexer's
     * {@code subtype * 2} / {@code subtype * 2 + 1} (begin/end) scheme.
     */
    private static int toSignTypeCode(SignSubType subtype) {
        return switch (subtype) {
            case SignSubType.Plain plain -> switch (plain) {
                case MDC_CODE -> SymbolCodes.MDCCODE;
                case RED_POINT -> SymbolCodes.REDPOINT;
                case BLACK_POINT -> SymbolCodes.BLACKPOINT;
                case SMALL_TEXT -> SymbolCodes.SMALLTEXT;
                case HALF_SPACE -> SymbolCodes.HALFSPACE;
                case FULL_SPACE -> SymbolCodes.FULLSPACE;
                case FULL_SHADE -> SymbolCodes.FULLSHADE;
                case VERTICAL_SHADE -> SymbolCodes.VERTICALSHADE;
                case QUARTER_SHADE -> SymbolCodes.QUATERSHADE;
                case HORIZONTAL_SHADE -> SymbolCodes.HORIZONTALSHADE;
            };
            case SignSubType.Philology(PhilologyKind kind, boolean begin) -> {
                int base = philologySubCode(kind);
                yield begin ? base * 2 : base * 2 + 1;
            }
        };
    }

    /** The {@link SymbolCodes} philology sub-type constant (50-56) for a bracket kind. */
    private static int philologySubCode(PhilologyKind kind) {
        return switch (kind) {
            case ERASED_SIGNS -> SymbolCodes.ERASEDSIGNS;
            case EDITOR_ADDITION -> SymbolCodes.EDITORADDITION;
            case EDITOR_SUPERFLUOUS -> SymbolCodes.EDITORSUPERFLUOUS;
            case PREVIOUSLY_READABLE -> SymbolCodes.PREVIOUSLYREADABLE;
            case SCRIBE_ADDITION -> SymbolCodes.SCRIBEADDITION;
            case MINOR_ADDITION -> SymbolCodes.MINORADDITION;
            case DUBIOUS -> SymbolCodes.DUBIOUS;
        };
    }

    private static ToggleType toToggleType(jsesh.parser.lexer.ToggleType toggle) {
        return switch (toggle) {
            case SHADING_TOGGLE -> ToggleType.SHADINGTOGGLE;
            case SHADING_ON -> ToggleType.SHADINGON;
            case SHADING_OFF -> ToggleType.SHADINGOFF;
            case RED -> ToggleType.RED;
            case BLACK -> ToggleType.BLACK;
            case BLACK_RED -> ToggleType.BLACKRED;
            case LACUNA -> ToggleType.LACUNA;
            case LINE_LACUNA -> ToggleType.LINELACUNA;
            case OMIT -> ToggleType.OMMIT;
        };
    }

    private static WordEndingCode toWordEndingCode(WordEnding ending) {
        return switch (ending) {
            case NONE -> WordEndingCode.NONE;
            case WORD_END -> WordEndingCode.WORD_END;
            case SENTENCE_END -> WordEndingCode.SENTENCE_END;
        };
    }
}
