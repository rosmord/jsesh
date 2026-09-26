package jsesh.model.tools;

import java.util.List;
import java.util.function.IntFunction;

import jsesh.model.AlphabeticCharacter;
import jsesh.model.ModelElement;
import jsesh.model.TopItem;
import jsesh.model.transliteration.TransliterationEncoding;

/// Finds *runs* of alphabetic text in a list of model elements.
///
/// Alphabetic text is stored one [AlphabeticCharacter] per element, but it is
/// usually processed by runs: a run is a maximal sequence of consecutive
/// characters written in the same script (for instance, `+lhello+s` in MdC is
/// one run of five characters).
///
/// All methods work either on the children of a container (a
/// `TopItemList`, a `BasicItemList`...) or on a plain list of elements.
///
/// @author rosmord
public final class AlphabeticRuns {

    private AlphabeticRuns() {
    }

    /// End (exclusive) of the run which starts at `start` among the children of
    /// `container`.
    ///
    /// @param container the element whose children are considered.
    /// @param start index of the first character of the run.
    /// @param splitOnState if true, a change of [jsesh.model.TopItemState]
    /// (red, shaded...) also ends the run, as it does in MdC.
    /// @return the index after the last character of the run; `start` if the
    /// element at `start` is not an [AlphabeticCharacter].
    public static int runEnd(ModelElement container, int start, boolean splitOnState) {
        return runEnd(container::getChildAt, container.getNumberOfChildren(), start, splitOnState);
    }

    /// Same as [#runEnd(ModelElement, int, boolean)], for a list of elements.
    public static int runEnd(List<? extends ModelElement> items, int start, boolean splitOnState) {
        return runEnd(items::get, items.size(), start, splitOnState);
    }

    /// Start of the run which contains the character at `index` among the
    /// children of `container` (states are not considered).
    ///
    /// @return the index of the first character of the run; `index` if the
    /// element at `index` is not an [AlphabeticCharacter].
    public static int runStart(ModelElement container, int index) {
        if (!(container.getChildAt(index) instanceof AlphabeticCharacter c)) {
            return index;
        }
        int i = index;
        while (i > 0 && container.getChildAt(i - 1) instanceof AlphabeticCharacter p && p.isSameScriptAs(c)) {
            i--;
        }
        return i;
    }

    /// The MdC form of the characters between `start` and `end` (before MdC
    /// escaping), e.g. `^xpr` for transliteration.
    ///
    /// @param items the elements; those in the range must all be
    /// [AlphabeticCharacter]s.
    public static String mdcText(List<? extends ModelElement> items, int start, int end) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < end; i++) {
            builder.append(((AlphabeticCharacter) items.get(i)).getMdcText());
        }
        return builder.toString();
    }

    /// Same as [#mdcText(List, int, int)], for the children of `container`.
    public static String mdcText(ModelElement container, int start, int end) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < end; i++) {
            builder.append(((AlphabeticCharacter) container.getChildAt(i)).getMdcText());
        }
        return builder.toString();
    }

    /// The displayed form of the characters between `start` and `end` (e.g.
    /// `ḫpr` for transliteration).
    ///
    /// @param items the elements; those in the range must all be
    /// [AlphabeticCharacter]s.
    /// @param transliterationEncoding how transliteration is displayed.
    public static String displayText(List<? extends ModelElement> items, int start, int end,
            TransliterationEncoding transliterationEncoding) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < end; i++) {
            builder.append(((AlphabeticCharacter) items.get(i)).getDisplayString(transliterationEncoding));
        }
        return builder.toString();
    }

    /// Same as [#displayText(List, int, int, TransliterationEncoding)], for the
    /// children of `container`.
    public static String displayText(ModelElement container, int start, int end,
            TransliterationEncoding transliterationEncoding) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < end; i++) {
            builder.append(((AlphabeticCharacter) container.getChildAt(i)).getDisplayString(transliterationEncoding));
        }
        return builder.toString();
    }

    private static int runEnd(IntFunction<? extends ModelElement> get, int size, int start, boolean splitOnState) {
        if (start >= size || !(get.apply(start) instanceof AlphabeticCharacter first)) {
            return start;
        }
        int end = start + 1;
        while (end < size
                && get.apply(end) instanceof AlphabeticCharacter c
                && c.isSameScriptAs(first)
                && (!splitOnState || sameState(c, first))) {
            end++;
        }
        return end;
    }

    private static boolean sameState(TopItem a, TopItem b) {
        return a.getState().equals(b.getState());
    }
}
