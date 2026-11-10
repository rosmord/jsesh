/*
 * Created on 19 mai 2004
 *
 */
package jsesh.model;

import java.util.Objects;

import jsesh.sharedvocabulary.LineAlignment;

/// A page break, `-!!`. It also ends a line, and carries its alignment
/// (`-!![align=center]`).
public class PageBreak extends TopItem implements LineEnd {

    /// The alignment of the line ended by this break.
    private final LineAlignment alignment;

    /// A page break ending a line aligned at the start.
    public PageBreak() {
        this(LineAlignment.START);
    }

    /// @param alignment the alignment of the line ended by this break.
    public PageBreak(LineAlignment alignment) {
        this.alignment = Objects.requireNonNull(alignment);
    }

    @Override
    public void accept(ModelElementVisitor v) {
        v.visitPageBreak(this);
    }

    @Override
    public LineAlignment getAlignment() {
        return alignment;
    }

    @Override
    public PageBreak withAlignment(LineAlignment alignment) {
        PageBreak b = new PageBreak(alignment);
        copyStateTo(b);
        return b;
    }

    @Override
    public int compareToAux(ModelElement e) {
        PageBreak b = (PageBreak) e;
        int result = alignment.compareTo(b.alignment);
        if (result == 0) {
            result = getState().compareTo(b.getState());
        }
        return result;
    }

    @Override
    public PageBreak deepCopy() {
        return withAlignment(alignment);
    }

    @Override
    public HorizontalListElement buildHorizontalListElement() {
        return null;
    }

    @Override
    public boolean isBreak() {
        return true;
    }

    @Override
    protected boolean equalsIgnoreIdAux(ModelElement other) {
        return alignment == ((PageBreak) other).alignment;
    }

    /// It structures the line, and keeps the direction of the text.
    @Override
    public boolean followsQuadratDirection() {
        return false;
    }
}
