package jsesh.model.unicode;

import jsesh.model.ShadingCode;
import jsesh.sharedvocabulary.InsertionDirection;

/// Egyptian Hieroglyph Format Controls (Unicode 17, block U+13430–U+1345F) and the
/// variation selectors used for rotations.
///
/// The names are the Unicode names, without the `EGYPTIAN HIEROGLYPH` prefix. See chapter 11
/// of the Unicode Standard (section 11.4) for their meaning.
///
/// Precedence of the controls, from the tightest to the loosest binding (Unicode 17, §11.4.3):
/// insertions, then [#OVERLAY_MIDDLE], then [#HORIZONTAL_JOINER], then [#VERTICAL_JOINER].
/// An insertion or an overlay control applies to the single sign, or to the segment
/// ([#BEGIN_SEGMENT]...[#END_SEGMENT]), which follows it.
public final class UnicodeHieroglyphControls {

    private UnicodeHieroglyphControls() {
    }

    public static final int VERTICAL_JOINER = 0x13430;
    public static final int HORIZONTAL_JOINER = 0x13431;
    public static final int INSERT_AT_TOP_START = 0x13432;
    public static final int INSERT_AT_BOTTOM_START = 0x13433;
    public static final int INSERT_AT_TOP_END = 0x13434;
    public static final int INSERT_AT_BOTTOM_END = 0x13435;
    public static final int OVERLAY_MIDDLE = 0x13436;
    public static final int BEGIN_SEGMENT = 0x13437;
    public static final int END_SEGMENT = 0x13438;
    public static final int INSERT_AT_MIDDLE = 0x13439;
    public static final int INSERT_AT_TOP = 0x1343A;
    public static final int INSERT_AT_BOTTOM = 0x1343B;
    public static final int BEGIN_ENCLOSURE = 0x1343C;
    public static final int END_ENCLOSURE = 0x1343D;
    public static final int BEGIN_WALLED_ENCLOSURE = 0x1343E;
    public static final int END_WALLED_ENCLOSURE = 0x1343F;
    public static final int MIRROR_HORIZONTALLY = 0x13440;
    public static final int FULL_BLANK = 0x13441;
    public static final int HALF_BLANK = 0x13442;
    public static final int LOST_SIGN = 0x13443;
    public static final int HALF_LOST_SIGN = 0x13444;
    public static final int TALL_LOST_SIGN = 0x13445;
    public static final int WIDE_LOST_SIGN = 0x13446;

    /// The damage modifiers are U+13447..U+13455. [#damageModifier(int)] computes them.
    public static final int MODIFIER_DAMAGED_FIRST = 0x13447;
    /// `MODIFIER DAMAGED`: the whole surface is damaged.
    public static final int MODIFIER_DAMAGED = 0x13455;

    /// Variation selector used for a 90° rotation.
    public static final int VS1_ROTATE_90 = 0xFE00;
    /// Variation selector used for a 180° rotation.
    public static final int VS2_ROTATE_180 = 0xFE01;
    /// Variation selector used for a 270° rotation.
    public static final int VS3_ROTATE_270 = 0xFE02;
    /// Variation selector used for rotations of 15 to 75 degrees.
    public static final int VS4_ROTATE_15_75 = 0xFE03;
    /// Variation selector used for rotations of 285 to 345 degrees.
    public static final int VS7_ROTATE_285_345 = 0xFE06;

    /// The control which inserts a sign at a given place of the preceding sign.
    ///
    /// @param direction an insertion direction.
    /// @return the control code point, or `-1` if Unicode has no control for that direction
    ///         (this is the case for [InsertionDirection#START] and [InsertionDirection#END]).
    public static int insertionControl(InsertionDirection direction) {
        return switch (direction) {
            case TOP_START -> INSERT_AT_TOP_START;
            case BOTTOM_START -> INSERT_AT_BOTTOM_START;
            case TOP_END -> INSERT_AT_TOP_END;
            case BOTTOM_END -> INSERT_AT_BOTTOM_END;
            case CENTER -> INSERT_AT_MIDDLE;
            case TOP -> INSERT_AT_TOP;
            case BOTTOM -> INSERT_AT_BOTTOM;
            case START, END -> -1;
        };
    }

    /// The damage modifier for a set of damaged quarters.
    ///
    /// The 15 modifiers U+13447..U+13455 are the non-empty combinations of the four quarters,
    /// in the binary order top start (1), bottom start (2), top end (4), bottom end (8).
    ///
    /// @param shading a combination of [ShadingCode] values.
    /// @return the code point of the modifier, or `-1` if no quarter is damaged.
    public static int damageModifier(int shading) {
        int mask = 0;
        if ((shading & ShadingCode.TOP_START) != 0) {
            mask |= 1;
        }
        if ((shading & ShadingCode.BOTTOM_START) != 0) {
            mask |= 2;
        }
        if ((shading & ShadingCode.TOP_END) != 0) {
            mask |= 4;
        }
        if ((shading & ShadingCode.BOTTOM_END) != 0) {
            mask |= 8;
        }
        return mask == 0 ? -1 : MODIFIER_DAMAGED_FIRST - 1 + mask;
    }

    /// The variation selector which requests a given rotation.
    ///
    /// @param clockwiseAngle the angle, in degrees, clockwise, between 0 and 359.
    /// @return the variation selector's code point, or `-1` if the angle has no selector.
    public static int rotationSelector(int clockwiseAngle) {
        int a = ((clockwiseAngle % 360) + 360) % 360;
        if (a == 90) {
            return VS1_ROTATE_90;
        } else if (a == 180) {
            return VS2_ROTATE_180;
        } else if (a == 270) {
            return VS3_ROTATE_270;
        } else if (a >= 15 && a <= 75) {
            return VS4_ROTATE_15_75;
        } else if (a >= 285 && a <= 345) {
            return VS7_ROTATE_285_345;
        }
        return -1;
    }
}
