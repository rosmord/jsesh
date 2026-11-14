package jsesh.glyphs.signsource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/// The SVG of a document comes from an untrusted source: no XXE.
public class SVGSignSourceSecurityTest {

    private static final String PATH = "<path d='M 0 0 L 1000 0 L 1000 1800 L 0 1800 Z'/>";

    private static SVGSignSource read(String svg) {
        return new SVGSignSource("test", new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void externalEntityIsNotResolved(@TempDir Path dir) throws IOException {
        Path secret = dir.resolve("secret.txt");
        Files.writeString(secret, "SECRET-CONTENT");
        String svg = "<?xml version='1.0'?>\n<!DOCTYPE svg [<!ENTITY x SYSTEM '" + secret.toUri() + "'>]>\n"
                + "<svg xmlns='http://www.w3.org/2000/svg' width='1000' height='1800'>"
                + "<desc>&x;</desc>" + PATH + "</svg>";
        SVGSignSource source = read(svg);
        String doc = source.getCurrentShape().getDocumentation();
        assertFalse(doc != null && doc.contains("SECRET-CONTENT"), "external entity was resolved");
    }

    @Test
    void entityExpansionBombIsRejectedOrBounded() {
        StringBuilder sb = new StringBuilder("<?xml version='1.0'?>\n<!DOCTYPE svg [<!ENTITY a0 'aaaaaaaaaa'>");
        for (int i = 1; i <= 12; i++) {
            sb.append("<!ENTITY a").append(i).append(" '");
            for (int j = 0; j < 10; j++) {
                sb.append("&a").append(i - 1).append(";");
            }
            sb.append("'>");
        }
        sb.append("]>\n<svg xmlns='http://www.w3.org/2000/svg'><desc>&a12;</desc>").append(PATH).append("</svg>");
        long start = System.currentTimeMillis();
        read(sb.toString()); // must not hang nor exhaust memory
        assertTrue(System.currentTimeMillis() - start < 10_000);
    }

    @Test
    void plainSvgStillWorks() {
        SVGSignSource source = read("<svg xmlns='http://www.w3.org/2000/svg' width='1000' height='1800'>" + PATH
                + "</svg>");
        assertNotNull(source.getCurrentShape());
    }
}
