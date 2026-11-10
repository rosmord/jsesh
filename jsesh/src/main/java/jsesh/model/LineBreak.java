/*
 * Created on 19 mai 2004
 */
package jsesh.model;

import java.util.Objects;

import jsesh.sharedvocabulary.LineAlignment;

/// A line break, `-!`. It ends a line, and carries its alignment
/// (`-![align=center]`) and the skip after it (`-!=200%`).
///
/// @author rosmord
public class LineBreak extends TopItem implements LineEnd {

    private static final long serialVersionUID = 1228419796041957932L;

    /// Post-break vertical spacing: 100 is a full line.
    private int spacing;

    /// The alignment of the line ended by this break.
    private final LineAlignment alignment;

    /// @param spacing the post-break spacing (100 is a normal line skip).
    /// @param alignment the alignment of the line ended by this break.
    public LineBreak(int spacing, LineAlignment alignment) {
        this.spacing = spacing;
        this.alignment = Objects.requireNonNull(alignment);
    }

    /// @param spacing the post-break spacing (100 is a normal line skip).
    public LineBreak(int spacing) {
        this(spacing, LineAlignment.START);
    }

    /// A normal line break, ending a line aligned at the start.
    public LineBreak() {
        this(100);
    }

    /// A normal line break.
    ///
    /// @param alignment the alignment of the line ended by this break.
    public LineBreak(LineAlignment alignment) {
        this(100, alignment);
    }

    @Override
    public void accept(ModelElementVisitor v) {
        v.visitLineBreak(this);
    }

    /// Returns the spacing value.
    /// 100 is a full line, 200 is two lines worth, and so on.
    ///
    /// @return spacing.
    public int getSpacing() {
        return spacing;
    }

    public void setSpacing(int i) {
        spacing = i;
    }

    @Override
    public LineAlignment getAlignment() {
        return alignment;
    }

    @Override
    public LineBreak withAlignment(LineAlignment alignment) {
        LineBreak b = new LineBreak(spacing, alignment);
        copyStateTo(b);
        return b;
    }

    @Override
    public String toString() {
        return "lineBreak\n";
    }

    @Override
    public int compareToAux(ModelElement e) {
        LineBreak b = (LineBreak) e;
        int result = spacing - b.spacing;
        if (result == 0) {
            result = alignment.compareTo(b.alignment);
        }
        if (result == 0) {
            result = getState().compareTo(b.getState());
        }
        return result;
    }

    @Override
    public LineBreak deepCopy() {
        return withAlignment(alignment);
    }

    @Override
    public boolean isBreak() {
        return true;
    }

    @Override
    public HorizontalListElement buildHorizontalListElement() {
        return null;
    }

    @Override
    protected boolean equalsIgnoreIdAux(ModelElement other) {
        LineBreak otherBreak = (LineBreak) other;
        return this.spacing == otherBreak.spacing && this.alignment == otherBreak.alignment;
    }

    /// It structures the line, and keeps the direction of the text.
    @Override
    public boolean followsQuadratDirection() {
        return false;
    }
}
