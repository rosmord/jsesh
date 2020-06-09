package jsesh.mdc.model;

import jsesh.hieroglyphs.data.HieroglyphDatabaseRepository;
import jsesh.hieroglyphs.data.HieroglyphDatabaseInterface;
import jsesh.mdc.constants.LexicalSymbolsUtils;
import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdc.constants.WordEndingCode;
import jsesh.mdc.interfaces.HieroglyphInterface;

/**
 *
 * Hieroglyph
 *
 * @author rosmord
 *
 * This code is published under the GNU LGPL.
 */
public class Hieroglyph extends InnerGroup implements HieroglyphInterface {

    /**
     *
     */
    private static final long serialVersionUID = -5396127914583416286L;

    /**
     * The code used to designate this sign.
     */
    private String code;

    /**
     * optional code to indicate if the sign is a word limit.
     */
    private WordEndingCode endingCode;

    /**
     * is this sign part of a grammatical ending ?
     */
    private boolean grammar;

    /**
     * a code which helps to distinguish between real hieroglyphs (MDCCODE) and
     * another kind of sign (shading, red points...).
     *
     * @see SymbolCodes
     */
    private int type;

    /**
     * Modifiers are quite tricky, because their syntax is not very regular.
     * Basically, there are : boolean modifiers, which work as a trigger integer
     * modifiers, which set an integer value.
     *
     * available operations are :
     *
     * adding a new modifier. removing a modifier getting the collection of
     * modifiers for easy inspection ? (it should return an iterator !)
     *
     * For ease of use : get/set operation for boolean modifiers
     * get/set/unset/existence operations for integer modifiers.
     *
     * The invariants concerning modifiers are for a given type (boolean or
     * integer) of modifier, there is only one value with a given name.
     */
    /**
     * Sign position relative to the "normal" position (as set by layout
     * algorithms). Mostly used for explicit positioning. Position unit is
     * 1/1000 of an A1 sign height.
     */
    private int x;
    private int y;

    private Hieroglyph() {
        code = null;
        endingCode = WordEndingCode.NONE;
        grammar = false;
        type = SymbolCodes.MDCCODE;
        addChild(new ModifiersList());
    }

    /**
     * Create a sign of a particular type. May be a hieroglyph, or some other
     * symbols like red points.
     *
     * @param type : the type of the symbol, from SymbolCodes.
     */
    public Hieroglyph(int type) {
        this();
        switch (type) {
            case SymbolCodes.MDCCODE:
                setCode("A1");
                break;
            default:
                this.type = type;
                setCode(LexicalSymbolsUtils.getStringForLexicalItem(type));
                break;
        }
    }

    public Hieroglyph(ModifiersList listMod) {
        this();
        clearChildren();
        addChild(listMod);
    }

    public Hieroglyph(String code) {
        this();
        setCode(code);
    }

    /*
	 * @see jsesh.mdc.model.ModelElement#Accept(jsesh.mdc.model.ModelElementVisitor)
     */
    @Override
    public void accept(ModelElementVisitor v) {
        v.visitHieroglyph(this);
    }

    /* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#compareToAux(jsesh.mdc.model.ModelElement)
     */
    @Override
    public int compareToAux(ModelElement e) {
        Hieroglyph h = (Hieroglyph) e;
        int result = getCode().compareTo(h.getCode());
        if (result == 0) {
            result = getType() - h.getType();
            if (result == 0) {
                result = x - h.x;
                if (result == 0) {
                    result = y - h.y;
                    if (result == 0) {
                        result = endingCode.getId() - h.endingCode.getId();
                        if (result == 0) {
                            int i1 = (grammar ? 1 : 0);
                            int i2 = (h.grammar ? 1 : 0);
                            result = i1 - i2;
                            if (result == 0) {
                                result = compareContents(h);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#deepCopy()
     */
    public Hieroglyph deepCopy() {
        Hieroglyph result;
        result = new Hieroglyph(code);
        result.endingCode = endingCode;
        result.grammar = grammar;
        result.type = type;
        result.x = x;
        result.y = y;
        copyContentTo(result);
        return result;
    }

    public int getAngle() {
        // The angle is quite a complex problem.
        return getModifiers().getAngle();
    }

    public String getCode() {
        return code;
    }

    /**
     * an integer used to say if the sign is a word ending (1), a sentence
     * ending (2), or nothing of this kind (0).
     *
     * @return the ending code.
     */
    public WordEndingCode getEndingCode() {
        return endingCode;
    }

    /**
     * Returns the modifiers.
     *
     * @return ModifiersList
     */
    public ModifiersList getModifiers() {
        return (ModifiersList) getChildAt(0);
    }

    /**
     * Return the scale (as an integer percentage) of this sign.
     *
     * @return
     */
    public int getRelativeSize() {
        return getModifiers().getScale();
    }

    /**
     * a code which helps to distinguish between real hieroglyphs (MDCCODE) and
     * another kind of sign (shading, red points...).
     *
     * @return the type.
     */
    public int getType() {
        return type;
    }

    /**
     * An explicit positioning for this sign. Expressed in 1/1000 of A1 sign
     * height.
     *
     * @return x.
     */
    public int getX() {
        return x;
    }

    /**
     * An explicit positioning for this sign. Expressed in 1/1000 of A1 sign
     * height.
     *
     * * @return y.
     */
    public int getY() {
        return y;
    }

    public boolean isGrammar() {
        return grammar;
    }

    public boolean isReversed() {
        return getModifiers().isReversed();
    }

    /**
     * @return true if the sign can be stretched to fit a cadrat width.
     */
    public boolean isWide() {
        boolean result = false;
        result = getModifiers().getBoolean("l");
        return result;
    }

    public void setAngle(int _angle) {
        getModifiers().setAngle(_angle);
    }

    public final void setCode(String code) {
        // This code should stand elsewhere. For instance, we could use a 
        // factory...
        if (code.startsWith("J")) {
            code = "Aa" + code.substring(1);
        } else if ("Ff2".equals(code)) {
            code = "V49A";
        }
        this.code = code;
        int typeCode = LexicalSymbolsUtils.getCodeForString(code);
        if (typeCode != -1) {
            type = typeCode;
        } else {
            type = SymbolCodes.MDCCODE;
        }
        notifyModification();
    }

    /**
     * Is the sign is a word ending, a sentence ending, or nothing of this kind.
     *
     * @param _endingCode
     */
    public void setEndingCode(WordEndingCode _endingCode) {
        endingCode = _endingCode;
        notifyModification();
    }

    /**
     * Sets the position for this sign.
     * <p>
     * when translated in MDC code, position unit is 1/1000 of an A1 sign
     * height.
     *
     * @param x
     * @param y
     * @param scale : the relative scale for this sign, in percentage.
     */
    public void setExplicitPosition(int x, int y, int scale) {
        this.x = x;
        this.y = y;
        getModifiers().setScale(scale);
    }

    public void setGrammar(boolean _grammar) {
        grammar = _grammar;
        notifyModification();
    }

    /**
     * Sets the modifiers.
     *
     * @param modifiers The modifiers to set
     */
    public void setModifiers(ModifiersList modifiers) {
        setChildAt(0, modifiers);
        notifyModification();
    }

    public void setRelativeSize(int _relativeSize) {
        getModifiers().setScale(_relativeSize);
    }

    /**
     * Returns a float value of the sign scale, 1.0 being "no change" scale.
     *
     * @return
     */
    public float getFLoatScale() {
        return (float) (getRelativeSize() / 100.0);
    }

    public void setReversed(boolean _reversed) {
        getModifiers().setReversed(_reversed);
    }

    public void setType(int _type) {
        type = _type;
        notifyModification();
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String result = "(glyph " + code;
        if (getRelativeSize() != 100 || getX() != 0 || getY() != 0) {
            result += " [" + getX() + "," + getY() + ", " + getRelativeSize() + "%]";
        }
        result += ")";
        return result;
    }

    @Override
    public boolean containsOnlyOneSign() {
        return true;
    }

    @Override
    public Hieroglyph getLoneSign() {
        return this;
    }

    /**
     * Get the text when the symbol is in fact a "small text".
     *
     * @throws RuntimeException if the text is not a small text (I will choose a
     * better one later).
     * @return
     */
    public String getSmallText() {
        if (type != SymbolCodes.SMALLTEXT) {
            throw new RuntimeException("Incorrect call");
        }
        String smallText = getCode();
        smallText = smallText.substring(smallText.indexOf('"') + 1, smallText
                .length() - 1);
        return smallText;
    }

    /**
     * Returns true if this sign stands alone in a quadrant.
     *
     * @return
     */
    public boolean isAloneInQuadrant() {
        ModelElement elt = getParent();
        while (elt != null && !(elt instanceof Cadrat)) {
            elt = elt.getParent();
        }
        if (elt != null) {
            return elt.containsOnlyOneSign();
        } else {
            return false;
        }
    }

    /**
     * Is this sign a shading sign (horizontal, vertical, full or quarter
     * shading ?).
     *
     * @return
     */
    public boolean isShadingSign() {
        switch (getType()) {
            case SymbolCodes.HORIZONTALSHADE:
            case SymbolCodes.VERTICALSHADE:
            case SymbolCodes.FULLSHADE:
            case SymbolCodes.QUATERSHADE:
                return true;
            default:
                return false;
        }
    }

    @Override
    protected boolean equalsIgnoreIdAux(ModelElement other) {
        Hieroglyph o = (Hieroglyph) other;
        HieroglyphDatabaseInterface manager = HieroglyphDatabaseRepository.getHieroglyphDatabase();
        return manager.getCanonicalCode(this.code).equals(manager.getCanonicalCode(o.code))
                && this.endingCode.equals(o.endingCode)
                && this.grammar == o.grammar
                && this.type == o.type
                && this.x == o.x
                && this.y == o.y;

    }

} // end Hieroglyph
