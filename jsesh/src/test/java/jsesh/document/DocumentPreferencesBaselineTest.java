package jsesh.document;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import jsesh.model.constants.JSeshInfoConstants;
import jsesh.render.style.DocumentPreferencesStyleConverter;
import jsesh.render.style.JSeshStyle;

/// The alphabetic text baseline is saved with the document, with a default
/// for documents which don't store it.
public class DocumentPreferencesBaselineTest {

    @Test
    public void missingValueGivesTheDefault() {
        DocumentPreferences prefs = DocumentPreferences.fromStringMap(new HashMap<>());
        assertEquals(4.0, prefs.getAlphabeticTextBaseline(), 1e-6);
    }

    @Test
    public void storedValueIsRead() {
        Map<String, String> map = new HashMap<>();
        map.put(JSeshInfoConstants.JSESH_ALPHABETIC_TEXT_BASELINE, "7.5");
        DocumentPreferences prefs = DocumentPreferences.fromStringMap(map);
        assertEquals(7.5, prefs.getAlphabeticTextBaseline(), 1e-6);
    }

    @Test
    public void valueSurvivesStyleConversion() {
        JSeshStyle style = JSeshStyle.DEFAULT.copy()
                .geometry(g -> g.alphabeticTextBaseline(6.25f))
                .build();
        DocumentPreferences prefs = DocumentPreferencesStyleConverter.toDocumentPreferences(style);
        DocumentPreferences reread = DocumentPreferences.fromStringMap(prefs.getStringRepresentation());
        JSeshStyle back = DocumentPreferencesStyleConverter.applyDocumentPreferences(reread, JSeshStyle.DEFAULT);
        assertEquals(6.25f, back.geometry().alphabeticTextBaseline(), 1e-6);
    }
}
