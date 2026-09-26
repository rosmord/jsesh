package jsesh.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import jsesh.model.constants.ScriptCode;

public class AlphabeticCharacterTest {

    @Test
    public void uppercaseTransliterationIsOneCharacter() {
        List<AlphabeticCharacter> chars = AlphabeticCharacter.fromMdcText('t', "^xpr");
        assertEquals(3, chars.size());
        assertEquals('x', chars.get(0).getCodePoint());
        assertTrue(chars.get(0).isUppercase());
        assertEquals("^x", chars.get(0).getMdcText());
        assertFalse(chars.get(1).isUppercase());
        assertEquals(ScriptCode.TRANSLITERATION, chars.get(1).getScript());
    }

    @Test
    public void utrechtEncodingIsConverted() {
        // "#" is the Utrecht font's uppercase x.
        List<AlphabeticCharacter> chars = AlphabeticCharacter.fromMdcText('t', "#pr");
        assertEquals(3, chars.size());
        assertEquals('x', chars.get(0).getCodePoint());
        assertTrue(chars.get(0).isUppercase());
    }

    @Test
    public void trailingCaretIsDropped() {
        List<AlphabeticCharacter> chars = AlphabeticCharacter.fromMdcText('t', "nfr^");
        assertEquals(3, chars.size());
    }

    @Test
    public void caretIsPlainOutsideTransliteration() {
        List<AlphabeticCharacter> chars = AlphabeticCharacter.fromMdcText('l', "a^b");
        assertEquals(3, chars.size());
        assertEquals('^', chars.get(1).getCodePoint());
        assertFalse(chars.get(2).isUppercase());
    }

    @Test
    public void supplementaryCodePointIsOneCharacter() {
        String text = "a" + Character.toString(0x1D11E) + "b";
        List<AlphabeticCharacter> chars = AlphabeticCharacter.fromMdcText('l', text);
        assertEquals(3, chars.size());
        assertEquals(0x1D11E, chars.get(1).getCodePoint());
    }

    @Test
    public void lineBreaksBecomeSpaces() {
        List<AlphabeticCharacter> chars = AlphabeticCharacter.fromMdcText('l', "a\nb");
        assertEquals(' ', chars.get(1).getCodePoint());
        assertTrue(chars.get(1).isSpace());
    }

    @Test
    public void unknownScriptLetterIsKept() {
        AlphabeticCharacter c = AlphabeticCharacter.fromMdcText('f', "x").get(0);
        assertEquals(ScriptCode.OTHER, c.getScript());
        assertEquals('f', c.getMdcScriptCode());
        assertFalse(c.isSameScriptAs(new AlphabeticCharacter('q', 'x', false)));
    }

    @Test
    public void cyrillicAndCopticAreDistinct() {
        assertEquals(ScriptCode.COPTIC, ScriptCode.forMdcCode('c'));
        assertEquals(ScriptCode.CYRILLIC, ScriptCode.forMdcCode('r'));
    }

    @Test
    public void deepCopyKeepsStateAndValue() {
        AlphabeticCharacter c = new AlphabeticCharacter('t', 'x', true);
        c.setRed(true);
        AlphabeticCharacter copy = c.deepCopy();
        assertTrue(copy.equalsIgnoreId(c));
        assertTrue(copy.isRed());
        assertEquals(0, copy.compareTo(c));
    }
}
