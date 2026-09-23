package jsesh.jseshtexts;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import jsesh.document.MDCDocument;
import jsesh.io.document.MDCDocumentReader;

/**
 * Parses every ".gly" file of the jsesh-texts sample library (searched
 * recursively, as some texts are grouped in subdirectories, e.g. "KV34") and
 * checks that the current MDC parser reads it without falling back to
 * {@link MDCDocumentReader}'s per-line error recovery.
 *
 * <p>jsesh-texts is a sibling Gradle module; rather than depending on it,
 * this test reaches its sample files directly through the relative path
 * shared by both module directories, since Gradle test tasks run with the
 * module directory as their working directory.
 */
public class JseshTextsParsingTest {

    private static final Path JSESH_TEXTS_DIR = new File(
            "../jsesh-texts/src/main/assets/jsesh-texts").toPath();

    @TestFactory
    List<DynamicTest> allJseshTextsParseCorrectly() throws IOException {
        assertTrue(Files.isDirectory(JSESH_TEXTS_DIR),
                "jsesh-texts sample directory not found: "
                        + JSESH_TEXTS_DIR.toAbsolutePath());

        List<Path> files;
        try (Stream<Path> walk = Files.walk(JSESH_TEXTS_DIR)) {
            files = walk.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".gly"))
                    .sorted(Comparator.comparing(Path::toString))
                    .collect(Collectors.toList());
        }
        assertTrue(!files.isEmpty(), "no .gly files found in "
                + JSESH_TEXTS_DIR.toAbsolutePath());

        return files.stream()
                .map(file -> DynamicTest.dynamicTest(
                        JSESH_TEXTS_DIR.relativize(file).toString(),
                        () -> parse(file.toFile())))
                .collect(Collectors.toList());
    }

    private void parse(File file) throws Exception {
        MDCDocumentReader reader = new MDCDocumentReader();
        reader.failFast();
        MDCDocument document = reader.loadFile(file);
        assertNotNull(document.getTopItemList());
    }
}
