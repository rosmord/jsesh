package jsesh.mdcreader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jsesh.model.AbsoluteGroup;
import jsesh.model.AbsoluteTab;
import jsesh.model.AlphabeticCharacter;
import jsesh.model.BasicItem;
import jsesh.model.BasicItemList;
import jsesh.model.Cadrat;
import jsesh.model.Cartouche;
import jsesh.model.ComplexLigature;
import jsesh.model.HBox;
import jsesh.model.HRule;
import jsesh.model.Hieroglyph;
import jsesh.model.HieroglyphicText;
import jsesh.model.HorizontalListElement;
import jsesh.model.InnerGroup;
import jsesh.model.Insert;
import jsesh.model.Kerning;
import jsesh.model.LayoutSetting;
import jsesh.model.Ligature;
import jsesh.model.LineBreak;
import jsesh.model.MdcComment;
import jsesh.model.Modifier;
import jsesh.model.ModifiersList;
import jsesh.model.Overwrite;
import jsesh.model.PageBreak;
import jsesh.model.Philology;
import jsesh.model.RunningAnnotation;
import jsesh.model.SubCadrat;
import jsesh.model.Superscript;
import jsesh.model.Tab;
import jsesh.model.TextLayoutSettings;
import jsesh.model.TopItem;
import jsesh.model.TopItemState;
import jsesh.model.Zone;
import jsesh.model.ZoneContent;
import jsesh.model.ZoneParameters;
import jsesh.model.constants.Dialect;
import jsesh.model.constants.SymbolCodes;
import jsesh.model.constants.ToggleType;
import jsesh.parser.ast.AstAbsoluteGroup;
import jsesh.parser.ast.AstAbsoluteTab;
import jsesh.parser.ast.AstAlphabeticText;
import jsesh.parser.ast.AstBasicItem;
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
import jsesh.parser.ast.AstInsert;
import jsesh.parser.ast.AstKerning;
import jsesh.parser.ast.AstLigature;
import jsesh.parser.ast.AstLineBreak;
import jsesh.parser.ast.AstModifier;
import jsesh.parser.ast.AstModifierList;
import jsesh.parser.ast.AstNode;
import jsesh.parser.ast.AstOption;
import jsesh.parser.ast.AstOptionList;
import jsesh.parser.ast.AstOptionValue;
import jsesh.parser.ast.AstOverwrite;
import jsesh.parser.ast.AstPageBreak;
import jsesh.parser.ast.AstPhilology;
import jsesh.parser.ast.AstRunningAnnotation;
import jsesh.parser.ast.AstStartHieroglyphicText;
import jsesh.parser.ast.AstSubCadrat;
import jsesh.parser.ast.AstSuperscript;
import jsesh.parser.ast.AstTab;
import jsesh.parser.ast.AstToggle;
import jsesh.parser.ast.AstTopItem;
import jsesh.parser.ast.AstTopItemList;
import jsesh.parser.ast.AstVisitor;
import jsesh.parser.ast.AstZone;
import jsesh.parser.lexer.symbols.PhilologyKind;
import jsesh.parser.lexer.symbols.SignSubType;
import jsesh.sharedvocabulary.LineAlignment;

/// An AstVisitor which knows how to build a [HieroglyphicText] model from an [AstDocument] AST.
/// @author rosmord
/// @see MDCParserModelGenerator
 

class AstModelBuilder implements AstVisitor {

    private final Dialect dialect;
    private final List<Object> stack = new ArrayList<>();
    private final TopItemState currentState = new TopItemState();

    AstModelBuilder(Dialect dialect) {
        this.dialect = dialect;
    }

    HieroglyphicText build(AstDocument document) {
        document.accept(this);
        return pop(HieroglyphicText.class);
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

    /// A built value as a list of items: alphabetic text builds a list of
    /// characters, everything else a single item.
    private static <T> List<T> asItems(Object built, Class<T> type) {
        if (built instanceof List<?> items) {
            return items.stream().map(type::cast).toList();
        }
        return List.of(type.cast(built));
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
        HieroglyphicText list = new HieroglyphicText();
        list.addAll(buildTopItems(node));
        push(list);
    }

    /// How many zones enclose the item being built (0 outside any zone).
    private int zoneDepth = 0;

    /// Builds the items of a text or of a zone, giving each one the current
    /// red/shaded state.
    ///
    /// Toggles are read in sequence, across zone limits: `$r-A1-%zone(A2)`
    /// makes A2 red.
    private List<TopItem> buildTopItems(AstTopItemList node) {
        List<TopItem> result = new ArrayList<>();
        for (AstTopItem item : node.items()) {
            item.accept(this);
            Object built = pop();
            if (built == null) {
                // Toggles (already folded into currentState by visitToggle)
                // and the "+s" marker: none of these become model items.
                continue;
            }
            for (TopItem topItem : asItems(built, TopItem.class)) {
                // A zone has no state of its own: its content has.
                if (!(item instanceof AstZone)) {
                    topItem.setState(currentState.duplicate());
                }
                result.add(topItem);
            }
        }
        return result;
    }

    @Override
    public void visitBasicItemList(AstBasicItemList node) {
        BasicItemList list = new BasicItemList();
        for (AstBasicItem item : node.items()) {
            item.accept(this);
            Object built = pop();
            if (built != null) {
                for (BasicItem basicItem : asItems(built, BasicItem.class)) {
                    list.addBasicItem(basicItem);
                }
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
        // node.options() is intentionally ignored here: the model has
        // no use for them.
        // `i-(mn:n)` is read as `i-mn:n`, `a:(b:c)` as `a:b:c`: useless sub-quadrats are only syntax.
        push(cadrat.withoutUselessSubCadrats().orElse(cadrat));
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
        hieroglyph.setEndingCode(node.endingCode());
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

    @Override
    public void visitInsert(AstInsert astInsert) {
        SubCadrat first = new SubCadrat(build(astInsert.firstItems(), BasicItemList.class));
        SubCadrat second = new SubCadrat(build(astInsert.secondItems(), BasicItemList.class));
        AstOptionList options = astInsert.options();
        push(new Insert(astInsert.direction(), first, second, astInsert.target(),
                options.getInt("dw", 0), options.getInt("dh", 0)));
    }

    @Override
    public void visitKerning(AstKerning astKerning) {
        push(new Kerning(astKerning.axis(), optionsText(astKerning.options())));
    }

    /// The options, written back as they would appear between brackets.
    private static String optionsText(AstOptionList options) {
        StringBuilder result = new StringBuilder();
        for (AstOption option : options.options()) {
            if (!result.isEmpty()) {
                result.append(',');
            }
            result.append(option.name());
            switch (option.value()) {
                case AstOptionValue.Flag f -> {
                }
                case AstOptionValue.Text t -> result.append('=').append(t.value());
                case AstOptionValue.Int i -> result.append('=').append(i.value());
            }
        }
        return result.toString();
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
        // Only the opening kind is kept: the closing
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
        // One text block gives one element per character (a List, flattened by
        // the list visitors); "++" blocks are comments.
        if (node.scriptCode() == '+') {
            push(new MdcComment(node.text()));
        } else {
            push(AlphabeticCharacter.fromMdcText(node.scriptCode(), node.text()));
        }
    }

    @Override
    public void visitHRule(AstHRule node) {
        push(new HRule(node.lineType(), node.startPos(), node.endPos()));
    }

    @Override
    public void visitLineBreak(AstLineBreak node) {
        push(new LineBreak(node.skip(), lineAlignment(node.options())));
    }

    @Override
    public void visitPageBreak(AstPageBreak node) {
        if (zoneDepth > 0) {
            // In a zone, a page break is read as a simple line break (`-!`).
            push(new LineBreak(lineAlignment(node.options())));
        } else {
            push(new PageBreak(lineAlignment(node.options())));
        }
    }

    /// The alignment given by the `align` option of a line or page break, or of
    /// a tab.
    private static LineAlignment lineAlignment(AstOptionList options) {
        return LineAlignment.fromMdc(options.getString("align", ""));
    }

    @Override
    public void visitAbsoluteTab(AstAbsoluteTab node) {
        push(new AbsoluteTab(node.stopWidth()));
    }

    /// Reads `n` (the tab stop index; an invalid one is ignored) and `align`
    /// (the alignment of the cell before the tab).
    @Override
    public void visitTab(AstTab node) {
        push(new Tab(node.options().getInt("n", Tab.AUTO),
                lineAlignment(node.options())));
    }

    /// Reads `size`: `fill`, or a length in percents of a quadrat. A missing
    /// or invalid size is one quadrat.
    @Override
    public void visitRunningAnnotation(AstRunningAnnotation node) {
        if ("fill".equals(node.options().getString("size", ""))) {
            push(RunningAnnotation.filling(node.kind()));
        } else {
            push(new RunningAnnotation(node.kind(),
                    node.options().getInt("size", RunningAnnotation.DEFAULT_PERCENT)));
        }
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
        // LACUNA, LINELACUNA, OMMIT: not implemented.
        push(null);
    }

    @Override
    public void visitStartHieroglyphicText(AstStartHieroglyphicText node) {
        // Purely a lexer-level marker.
        push(null);
    }

    @Override
    public void visitZone(AstZone node) {
        zoneDepth++;
        ZoneContent content;
        try {
            content = new ZoneContent(buildTopItems(node.list()));
        } finally {
            zoneDepth--;
        }
        push(new Zone(zoneParameters(node.options()), content));
    }

    /// Reads the shared layout settings (`layout`, `dir`, `linegap`,
    /// `fitsigns`..., see [LayoutSetting]) and the settings proper to zones:
    /// `scale`, `width`, `height`, `tabgap`, `separator`, `frame` and `fill`
    /// (`yes`/`no`). Unknown
    /// options and values are ignored.
    private static ZoneParameters zoneParameters(AstOptionList options) {
        TextLayoutSettings settings = TextLayoutSettings.INHERIT_ALL;
        boolean fill = false;
        for (AstOption option : options.options()) {
            String text = switch (option.value()) {
                case AstOptionValue.Int i -> Integer.toString(i.value());
                case AstOptionValue.Text t -> t.value();
                case AstOptionValue.Flag f -> null;
            };
            if (option.name().equals("fill")) {
                // A bare `fill` is read as `fill=yes`.
                fill = text == null || LayoutSetting.parseFlag(text).orElse(false);
            }
            Optional<LayoutSetting> setting = LayoutSetting.byName(option.name());
            if (text != null && setting.isPresent()) {
                Optional<Object> value = setting.get().parse(text);
                if (value.isPresent()) {
                    settings = settings.with(setting.get(), value.get());
                }
            }
        }
        return new ZoneParameters(settings,
                options.getInt("scale", ZoneParameters.UNSET),
                options.getInt("width", ZoneParameters.UNSET),
                options.getInt("height", ZoneParameters.UNSET),
                options.getInt("tabgap", ZoneParameters.UNSET),
                options.getInt("separator", ZoneParameters.UNSET),
                options.hasOption("frame"),
                fill);
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
     * (philologyAsSigns mode) uses the
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

    private static ToggleType toToggleType(jsesh.parser.lexer.symbols.ToggleType toggle) {
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
}
