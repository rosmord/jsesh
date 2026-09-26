package jsesh.model;

/// A comment in a Manuel de Codage text (`++some comment+s`).
///
/// JSesh doesn't write comments itself; it only preserves those found in the
/// files it reads. They are not displayed, but they are written back when the
/// text is saved.
///
/// (The `++` lines at the very beginning of a file, which carry document
/// metadata, are handled when the file is read, and never become comments.)
///
/// @author rosmord
public class MdcComment extends BasicItem implements TextContainer {

    private static final long serialVersionUID = 4270611961730213380L;

    private String text;

    public MdcComment(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
        notifyModification();
    }

    @Override
    public void accept(ModelElementVisitor v) {
        v.visitComment(this);
    }

    @Override
    public String toString() {
        return "(comment " + text + ")";
    }

    @Override
    protected int compareToAux(ModelElement e) {
        MdcComment other = (MdcComment) e;
        int result = text.compareTo(other.text);
        if (result == 0) {
            result = getState().compareTo(other.getState());
        }
        return result;
    }

    @Override
    public HorizontalListElement buildHorizontalListElement() {
        return new SubCadrat(deepCopy());
    }

    @Override
    public MdcComment deepCopy() {
        MdcComment copy = new MdcComment(text);
        copyStateTo(copy);
        return copy;
    }

    @Override
    protected boolean equalsIgnoreIdAux(ModelElement other) {
        return text.equals(((MdcComment) other).text);
    }
}
