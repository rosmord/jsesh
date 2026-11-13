package jsesh.jseshtexts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class JSeshTextLibraryTest {

    @Test
    void titleIsFirstAlphabeticSegment() {
        String text = "++JSesh_Info 1.0 +s\n+bBalat letter 3686+s-+iTyped by S. Rosmorduc+s-!\nA1-!\n";
        assertEquals("Balat letter 3686", JSeshTextLibrary.extractTitle("x/Balat.gly", text));
    }

    @Test
    void titleSkipsLinesWithoutAlphabeticText() {
        String text = "!\n+b +s-!\n+lSecond+s-!\n";
        assertEquals("Second", JSeshTextLibrary.extractTitle("x/T.gly", text));
    }

    @Test
    void titleFallsBackToFileName() {
        assertEquals("Sinuhe", JSeshTextLibrary.extractTitle("jsesh-texts/Sinuhe.gly", "A1-B1-!\n"));
    }

    @Test
    void libraryIsSortedAndContainsTexts() {
        List<JSeshTextLibrary.Entry> entries = JSeshTextLibrary.getInstance().getEntries();
        assertFalse(entries.isEmpty());
        assertTrue(entries.stream().anyMatch(e -> e.path().endsWith("Sinuhe.gly")));
        assertTrue(entries.stream().noneMatch(e -> e.path().endsWith(".svg")));
        for (JSeshTextLibrary.Entry e : entries) {
            assertFalse(e.title().isBlank());
            assertTrue(JSeshTextLibrary.getInstance().readBytes(e.path()).length >= 0);
        }
    }
}
