package jsesh.jseshtexts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JSeshTextExporterTest {

    @Test
    void exportTextsExtractsFilesAndSubdirectories(@TempDir Path destination) {
        JSeshTextExporter.exportTexts(destination);

        Path textsRoot = destination.resolve("jsesh-texts");
        assertTrue(Files.isRegularFile(textsRoot.resolve("Sinuhe.gly")));
        assertTrue(Files.isRegularFile(
                textsRoot.resolve("KV34/Complements-KV34/US22A13B.svg")));
    }
}
