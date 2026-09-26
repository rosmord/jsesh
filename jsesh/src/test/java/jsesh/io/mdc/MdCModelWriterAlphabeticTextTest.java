package jsesh.io.mdc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.AlphabeticCharacter;
import jsesh.model.TopItemList;
import jsesh.parser.MDCSyntaxError;

/// Alphabetic text is stored one character per element; the writer must
/// gather them back into `+x...+s` blocks.
public class MdCModelWriterAlphabeticTextTest {

    private TopItemList parse(String mdc) throws MDCSyntaxError {
        return new MDCParserModelGenerator().parse(mdc);
    }

    private String write(TopItemList list) {
        return new MdCModelWriter().toMdC(list);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "+lhello world+s-A1",
        "+t^xpr+s",
        "+lab+s-+icd+s",
        "++a comment+s-A1",
        "+la\\+b+s",
        "+fsome translation+s",
        "<-+lab+s-ra->",
    })
    public void canonicalTextIsWrittenBack(String mdc) throws MDCSyntaxError {
        assertEquals(mdc, write(parse(mdc)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "$r-+lab+s-$b-+lcd+s",
        "+lab+s-$r-+lcd+s-$b-+lef+s",
        "+la\\\\b+s",
        "+la\\b+s",
        "+t#pr+s",
    })
    public void roundTripKeepsTheModel(String mdc) throws MDCSyntaxError {
        TopItemList first = parse(mdc);
        TopItemList second = parse(write(first));
        assertEquals(first.getNumberOfChildren(), second.getNumberOfChildren());
        assertTrue(first.equalsIgnoreId(second), () -> write(first));
        for (int i = 0; i < first.getNumberOfChildren(); i++) {
            assertEquals(first.getTopItemAt(i).getState(), second.getTopItemAt(i).getState());
        }
    }

    @Test
    public void stateChangeSplitsTheRun() throws MDCSyntaxError {
        TopItemList list = parse("+labcd+s");
        list.getTopItemAt(2).setRed(true);
        list.getTopItemAt(3).setRed(true);
        String mdc = write(list);
        assertEquals("+lab+s-$r-+lcd+s$b-", mdc);
    }

    @Test
    public void backslashIsEscaped() {
        TopItemList list = new TopItemList();
        list.addAll(AlphabeticCharacter.fromMdcText('l', "a\\\\b"));
        assertEquals("+la\\\\\\\\b+s", write(list));
    }
}
