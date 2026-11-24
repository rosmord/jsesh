package jsesh.model.unicode;

import static jsesh.model.unicode.UnicodeHieroglyphControls.BEGIN_SEGMENT;
import static jsesh.model.unicode.UnicodeHieroglyphControls.END_SEGMENT;
import static jsesh.model.unicode.UnicodeHieroglyphControls.HORIZONTAL_JOINER;
import static jsesh.model.unicode.UnicodeHieroglyphControls.MIRROR_HORIZONTALLY;
import static jsesh.model.unicode.UnicodeHieroglyphControls.OVERLAY_MIDDLE;
import static jsesh.model.unicode.UnicodeHieroglyphControls.VERTICAL_JOINER;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import jsesh.model.Cadrat;
import jsesh.model.HBox;
import jsesh.model.Hieroglyph;
import jsesh.model.Insert;
import jsesh.model.ModelElement;
import jsesh.model.Overwrite;
import jsesh.model.ShadingCode;
import jsesh.model.SubCadrat;
import jsesh.model.constants.LexicalSymbolsUtils;
import jsesh.model.constants.SymbolCodes;
import jsesh.model.constants.TextDirection;

/// Writes a quadrat as Unicode signs and Egyptian Hieroglyph Format Controls.
///
/// Used by [MdCToUnicodeConverter]. The rules come from chapter 11 of the Unicode
/// Standard (version 17), §11.4.3:
///
/// - the controls have a precedence. From the tightest to the loosest: insertions, overlay
///   (`OM`), horizontal joiner (`HJ`, `*`), vertical joiner (`VJ`, `:`). A group is written between
///   `BEGIN SEGMENT` and `END SEGMENT` only when this precedence requires it;
/// - an insertion or overlay control applies to the single sign, or to the segment, which
///   follows it. An insertion is written _container, control, inserted sign_;
/// - the signs of an overlay are written in graphical order (the last one is on top), which is
///   the order of the MdC (the order of the signs of an overlay is not sorted by `kEH_Cat`);
/// - when a sign is rotated and mirrored, the variation selector comes first, then the
///   mirror control; the damage modifier comes last.
///
/// Cartouches are not handled yet.
final class UnicodeFormatRenderer {

    // Binding levels. Smaller means "binds tighter".
    private static final int ATOM = 0;
    private static final int INSERTION = 1;
    private static final int OVERLAY = 2;
    private static final int HJ = 3;
    private static final int VJ = 4;
    /// Signs written one after the other, with no operator.
    private static final int SEQUENCE = 5;

    /// A rendered piece of text.
    ///
    /// @param text the Unicode string.
    /// @param level binding level of the outermost operator.
    /// @param encoded true if every sign of the text has a Unicode equivalent.
    /// @param firstSign code point of the first sign, or -1. Used to sort overlaid signs.
    private record Expr(String text, int level, boolean encoded, int firstSign) {

        static Expr empty() {
            return new Expr("", ATOM, true, -1);
        }
    }

    private final TextDirection textDirection;
    private final Function<ModelElement, String> plainRenderer;

    /// @param textDirection direction of the text (matters for rotations).
    /// @param plainRenderer converts an element which is not a quadrat's constituent (text, etc.)
    ///        to a plain string.
    UnicodeFormatRenderer(TextDirection textDirection,
            Function<ModelElement, String> plainRenderer) {
        this.textDirection = textDirection;
        this.plainRenderer = plainRenderer;
    }

    /// Writes a quadrat.
    String render(Cadrat cadrat) {
        return renderElement(cadrat).text();
    }

    private Expr renderElement(ModelElement e) {
        if (e instanceof Hieroglyph h) {
            return renderHieroglyph(h);
        } else if (e instanceof Cadrat c) {
            return renderCadrat(c);
        } else if (e instanceof HBox b) {
            return join(children(b), HORIZONTAL_JOINER, HJ);
        } else if (e instanceof Insert i) {
            return renderInsert(i);
        } else if (e instanceof Overwrite o) {
            return renderOverlay(List.of(renderHieroglyph(o.getFirst()), renderHieroglyph(o.getSecond())));
        } else if (e instanceof SubCadrat || e.getNumberOfChildren() > 0) {
            // SubCadrat, BasicItemList, ligatures... Their layout has no Unicode counterpart:
            // the children are written one after the other.
            return sequence(children(e));
        } else {
            return new Expr(plainRenderer.apply(e), ATOM, true, -1);
        }
    }

    private List<Expr> children(ModelElement e) {
        List<Expr> result = new ArrayList<>();
        for (int i = 0; i < e.getNumberOfChildren(); i++) {
            result.add(renderElement(e.getChildAt(i)));
        }
        return result;
    }

    private Expr sequence(List<Expr> items) {
        if (items.isEmpty()) {
            return Expr.empty();
        } else if (items.size() == 1) {
            return items.get(0);
        }
        StringBuilder text = new StringBuilder();
        boolean encoded = true;
        for (Expr x : items) {
            text.append(x.text());
            encoded &= x.encoded();
        }
        return new Expr(text.toString(), SEQUENCE, encoded, items.get(0).firstSign());
    }

    private Expr renderCadrat(Cadrat cadrat) {
        Expr result = join(children(cadrat), VERTICAL_JOINER, VJ);
        int damage = UnicodeHieroglyphControls.damageModifier(cadrat.getShading());
        if (damage != -1 && result.encoded() && !result.text().isEmpty()) {
            Expr atom = group(result, ATOM);
            result = new Expr(atom.text() + Character.toString(damage), ATOM, true, atom.firstSign());
        }
        return result;
    }

    /// Joins the items with a joiner. As the joiners associate to the left, the items which bind
    /// more loosely than the joiner are grouped.
    private Expr join(List<Expr> items, int joiner, int level) {
        List<Expr> nonEmpty = items.stream().filter(x -> !x.text().isEmpty()).toList();
        if (nonEmpty.size() <= 1) {
            return nonEmpty.isEmpty() ? Expr.empty() : nonEmpty.get(0);
        }
        StringBuilder text = new StringBuilder();
        boolean encoded = true;
        for (int i = 0; i < nonEmpty.size(); i++) {
            if (i > 0) {
                text.appendCodePoint(joiner);
            }
            Expr item = group(nonEmpty.get(i), level);
            text.append(item.text());
            encoded &= item.encoded();
        }
        return new Expr(text.toString(), level, encoded, nonEmpty.get(0).firstSign());
    }

    /// Surrounds an expression with segment controls if it binds more loosely than `maxLevel`.
    private Expr group(Expr e, int maxLevel) {
        if (e.level() <= maxLevel) {
            return e;
        }
        return new Expr(Character.toString(BEGIN_SEGMENT) + e.text() + Character.toString(END_SEGMENT),
                ATOM, e.encoded(), e.firstSign());
    }

    private Expr renderInsert(Insert insert) {
        Expr main = renderElement(insert.getMain());
        Expr inserted = renderElement(insert.getInserted());
        int control = UnicodeHieroglyphControls.insertionControl(insert.getDirection());
        if (control == -1 || !main.encoded() || !inserted.encoded()
                || main.text().isEmpty() || inserted.text().isEmpty()) {
            // No way to say it in Unicode: no control, and the reading order.
            return sequence(List.of(renderElement(insert.getFirst()), renderElement(insert.getSecond())));
        }
        // The container is whatever precedes the control; the inserted element must be an atom.
        Expr container = group(main, INSERTION);
        Expr contained = group(inserted, ATOM);
        return new Expr(container.text() + Character.toString(control) + contained.text(),
                INSERTION, true, container.firstSign());
    }

    /// Writes an overlay.
    ///
    /// The elements are kept in the order of the MdC (the last one is on top).
    private Expr renderOverlay(List<Expr> elements) {
        if (elements.stream().anyMatch(x -> !x.encoded() || x.text().isEmpty())) {
            return sequence(elements);
        }
        List<Expr> sorted = elements;
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < sorted.size(); i++) {
            Expr x = i == 0 ? group(sorted.get(i), OVERLAY) : group(sorted.get(i), ATOM);
            if (i > 0) {
                text.appendCodePoint(OVERLAY_MIDDLE);
            }
            text.append(x.text());
        }
        return new Expr(text.toString(), OVERLAY, true, sorted.get(0).firstSign());
    }

    private Expr renderHieroglyph(Hieroglyph h) {
        if (h.getType() == SymbolCodes.SMALLTEXT) {
            return new Expr(h.getCode(), ATOM, true, -1);
        } else if (h.getType() != SymbolCodes.MDCCODE) {
            return new Expr(LexicalSymbolsUtils.getStringForPhilology(h.getType()), ATOM, true, -1);
        }
        String unicode = MdcUnicodeTable.INSTANCE.getUnicodeFor(h.getCode());
        if (!MdcUnicodeTable.INSTANCE.hasUnicode(h.getCode())) {
            // The MdC code itself. No control makes sense.
            return new Expr(unicode, ATOM, false, -1);
        }
        StringBuilder text = new StringBuilder(unicode);
        int first = unicode.codePointAt(0);
        // Unicode: the rotation (variation sequence) first, then the mirror, then the damage.
        if (unicode.codePointCount(0, unicode.length()) == 1) {
            appendRotation(text, first, h.getAngle());
        }
        if (h.isReversed()) {
            text.appendCodePoint(MIRROR_HORIZONTALLY);
        }
        if (h.getModifiers().hasInteger("shading")) {
            int damage = UnicodeHieroglyphControls
                    .damageModifier(ShadingCode.parse("" + h.getModifiers().getInteger("shading")));
            if (damage != -1) {
                text.appendCodePoint(damage);
            }
        }
        return new Expr(text.toString(), ATOM, true, first);
    }

    /// Adds the variation selector for a rotation, if Unicode defines a sequence for this sign and angle.
    private void appendRotation(StringBuilder text, int sign, int angle) {
        int a = textDirection.isLeftToRight() ? angle : (360 - angle) % 360;
        int selector = UnicodeHieroglyphControls.rotationSelector(a);
        if (selector != -1 && UnikemetData.INSTANCE.hasVariationSequence(sign, selector)) {
            text.appendCodePoint(selector);
        }
    }
}
