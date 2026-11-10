package jsesh.glyphs.possibilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jsesh.defaults.HieroglyphDatabaseFactory;
import jsesh.glossary.Glossary;
import jsesh.glossary.GlossaryEntry;
import jsesh.glossary.GlossaryPossibilities;
import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.glyphs.signdata.HieroglyphDatabase;
import jsesh.glyphs.signdata.SignDescriptionConstants;

/// Possibility lists and their sources.
class PossibilitiesTest {

    private static HieroglyphDatabase database;

    @BeforeAll
    static void loadDatabase() {
        database = HieroglyphDatabaseFactory.buildPlainDefault(PredefinedFonts.buildAllEmbeddedFonts());
    }

    private static List<String> codes(PossibilitiesList list) {
        return list.asList().stream().map(Possibility::getCode).toList();
    }

    @Test
    void listsAreImmutable() {
        PossibilitiesList l = PossibilitiesList.ofSigns("m", List.of("G17"));
        assertThrows(UnsupportedOperationException.class,
                () -> l.asList().add(Possibility.createSignPossibility("U1")));
    }

    @Test
    void combineKeepsTheOrderAndDropsDuplicates() {
        PossibilitiesList a = PossibilitiesList.ofSigns("m", List.of("G17", "U1"));
        PossibilitiesList b = PossibilitiesList.builder("m").addSign("U1").addSign("Aa15").addMdCText("m").build();
        PossibilitiesList c = a.combine(b);
        assertEquals(List.of("G17", "U1", "Aa15", "m"), codes(c));
        assertEquals("m", c.code());
        assertEquals(List.of("G17", "U1"), codes(a));
    }

    @Test
    void aSignAndATextWithTheSameCodeAreDifferent() {
        PossibilitiesList l = PossibilitiesList.builder("A1").addSign("A1").addMdCText("A1").build();
        assertEquals(2, l.size());
    }

    @Test
    void compositeFollowsTheOrderOfItsSources() {
        PossibilityRepository first = code -> PossibilitiesList.ofSigns(code, List.of("G17", "U1"));
        PossibilityRepository empty = PossibilitiesList::empty;
        PossibilityRepository second = code -> PossibilitiesList.ofSigns(code, List.of("U1", "Aa15"));
        CompositePossibilityRepository composite = new CompositePossibilityRepository(first, empty, second);
        assertEquals(List.of("G17", "U1", "Aa15"), codes(composite.getPossibilityListFor("m")));
        assertTrue(new CompositePossibilityRepository().getPossibilityListFor("m").isEmpty());
    }

    @Test
    void signDatabaseProposesSignsForATransliteration() {
        PossibilitiesList m = new SignDatabasePossibilities(database).getPossibilityListFor("m");
        // The "official" Manuel de Codage code comes first.
        assertEquals("m", m.get(0).getCode());
        assertTrue(codes(m).contains("G17"));
    }

    @Test
    void signDatabaseProposesTheExactCodeFirst() {
        PossibilitiesList a1 = new SignDatabasePossibilities(database).getPossibilityListFor("a1");
        assertEquals("A1", a1.get(0).getCode());
    }

    @Test
    void signDatabaseGivesAnEmptyListForAnUnknownValue() {
        PossibilitiesList l = new SignDatabasePossibilities(database).getPossibilityListFor("zzqqx");
        assertTrue(l.isEmpty());
    }

    @Test
    void readingTheDatabaseDoesNotChangeIt() {
        List<String> before = database.getPossibilityFor("zzqqx", SignDescriptionConstants.KEYBOARD);
        List<String> again = database.getPossibilityFor("zzqqx", SignDescriptionConstants.KEYBOARD);
        assertEquals(List.of(), before);
        assertEquals(before, again);
    }

    @Test
    void glossaryChangesAreSeenAtOnce() {
        Glossary glossary = new Glossary();
        GlossaryPossibilities possibilities = new GlossaryPossibilities(glossary);
        assertTrue(possibilities.getPossibilityListFor("nfr").isEmpty());
        glossary.add("nfr", "nfr-f:r");
        PossibilitiesList l = possibilities.getPossibilityListFor("nfr");
        assertEquals(List.of("nfr-f:r"), codes(l));
        assertTrue(!l.get(0).isSingleSign());
        glossary.remove(new GlossaryEntry("nfr", "nfr-f:r"));
        assertTrue(possibilities.getPossibilityListFor("nfr").isEmpty());
    }
}
