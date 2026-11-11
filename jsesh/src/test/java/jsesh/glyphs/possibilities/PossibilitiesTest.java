package jsesh.glyphs.possibilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jsesh.defaults.HieroglyphDatabaseFactory;
import jsesh.glossary.Glossary;
import jsesh.glossary.GlossaryEntry;
import jsesh.glossary.GlossaryPossibilities;
import jsesh.glyphs.fonts.HieroglyphShapeRepository;
import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.glyphs.shape.ShapeChar;
import jsesh.glyphs.signdata.HieroglyphDatabase;
import jsesh.glyphs.signdata.SignDescriptionConstants;
import jsesh.signcodes.CanonicalCode;

/// Possibility lists and their sources.
class PossibilitiesTest {

    private static HieroglyphDatabase database;

    @BeforeAll
    static void loadDatabase() {
        database = HieroglyphDatabaseFactory.buildPlainDefault(PredefinedFonts.buildAllEmbeddedFonts());
    }

    /// A shape repository holding codes only (no actual shapes).
    private record CodesOnly(Set<String> codes) implements HieroglyphShapeRepository {
        @Override
        public ShapeChar get(CanonicalCode code) {
            return null;
        }

        @Override
        public ShapeChar getSmallBody(CanonicalCode code) {
            return null;
        }

        @Override
        public Set<String> getCodes() {
            return codes;
        }

        @Override
        public boolean hasNewSigns() {
            return true;
        }
    }

    private static List<String> codes(PossibilitiesList list) {
        return list.asList().stream().map(Possibility::asMdC).toList();
    }

    @Test
    void listsAreImmutable() {
        PossibilitiesList l = PossibilitiesList.ofSigns("m", List.of("G17"));
        assertThrows(UnsupportedOperationException.class,
                () -> l.asList().add(new Possibility.Sign("U1")));
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
        assertEquals(new Possibility.Sign("m"), m.get(0));
        assertTrue(codes(m).contains("G17"));
    }

    @Test
    void signDatabaseProposesTheExactCodeFirst() {
        PossibilitiesList a1 = new SignDatabasePossibilities(database).getPossibilityListFor("a1");
        assertEquals(new Possibility.Sign("A1"), a1.get(0));
    }

    @Test
    void signDatabaseSeesTheSignsAddedToItsShapes() {
        // Like a sign file added to the user font directory: the codes change,
        // and no event is sent.
        Set<String> fontCodes = new HashSet<>(Set.of("A1"));
        HieroglyphDatabase db = HieroglyphDatabaseFactory.buildPlainDefault(new CodesOnly(fontCodes));
        SignDatabasePossibilities possibilities = new SignDatabasePossibilities(db);
        assertEquals(List.of("A1"), codes(possibilities.getPossibilityListFor("a1")));
        fontCodes.add("A1Z");
        assertEquals(List.of("A1", "A1Z"), codes(possibilities.getPossibilityListFor("a1")));
    }

    @Test
    void signDatabaseGivesAnEmptyListForAnUnknownValue() {
        PossibilitiesList l = new SignDatabasePossibilities(database).getPossibilityListFor("zzqqx");
        assertTrue(l.isEmpty());
    }

    @Test
    void readingTheDatabaseDoesNotChangeIt() {
        List<String> before = database.getSignsForValue("zzqqx", SignDescriptionConstants.KEYBOARD);
        List<String> again = database.getSignsForValue("zzqqx", SignDescriptionConstants.KEYBOARD);
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
        assertInstanceOf(Possibility.MdCText.class, l.get(0));
        glossary.remove(new GlossaryEntry("nfr", "nfr-f:r"));
        assertTrue(possibilities.getPossibilityListFor("nfr").isEmpty());
    }
}
