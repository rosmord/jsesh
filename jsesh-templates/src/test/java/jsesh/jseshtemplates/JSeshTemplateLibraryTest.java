package jsesh.jseshtemplates;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;

import jsesh.document.MDCDocument;
import jsesh.io.document.MDCDocumentReader;
import jsesh.io.document.MDCDocumentWriter;
import jsesh.model.Cadrat;

class JSeshTemplateLibraryTest {

    private final JSeshTemplateLibrary library = JSeshTemplateLibrary.getInstance();

    @Test
    void bundledTemplatesAreCorrectDocuments() {
        List<JSeshTemplateLibrary.Template> templates = library.getTemplates();
        assertFalse(templates.isEmpty());
        for (JSeshTemplateLibrary.Template template : templates) {
            assertFalse(template.name().isBlank(), template.path());
            MDCDocumentReader reader = new MDCDocumentReader();
            reader.failFast();
            assertDoesNotThrow(() -> reader.readStream(
                    new ByteArrayInputStream(library.readDocument(template.path())),
                    new File(template.path())), template.path());
        }
    }

    /// A template written with JSesh itself must be readable.
    @Test
    void headerSurvivesSavingByJSesh() throws Exception {
        MDCDocument document = new MDCDocumentReader().readString(
                "++JSesh_Info 2.0 +s\n+bName+s-!\n+lDescription \\+ more+s-!\n+bEND+s-!\nA1-!\n",
                new File("t.gly"));
        String saved = new MDCDocumentWriter().toMdC(document);
        var parsed = TemplateParser.parse(saved);
        assertEquals("Name", parsed.name());
        assertEquals("Description + more", parsed.description());
        MDCDocument created = new MDCDocumentReader().readString(parsed.document(), new File("t.gly"));
        assertEquals(1, created.getHieroglyphicText().asList().stream()
                .filter(item -> item instanceof Cadrat).count());
    }
}
