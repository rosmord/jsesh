package jsesh.jseshtexts;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JSeshTextLibraryExporter {

    private static final String TEXTS_ZIP_RESOURCE = "jsesh-texts.zip";

    /**
     * Unzip the JSesh text library into a "jsesh-texts" subfolder of the given destination folder.
     *
     * @param destination the folder under which the text library is extracted
     */
    public static void exportTexts(Path destination) {
        try (InputStream in = JSeshTextLibraryExporter.class.getResourceAsStream(TEXTS_ZIP_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Resource " + TEXTS_ZIP_RESOURCE + " not found");
            }
            Path textsRoot = destination;
            Files.createDirectories(textsRoot);
            try (ZipInputStream zip = new ZipInputStream(in)) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    Path entryPath = resolveEntry(textsRoot, entry);
                    if (entry.isDirectory()) {
                        Files.createDirectories(entryPath);
                    } else {
                        Files.createDirectories(entryPath.getParent());
                        Files.copy(zip, entryPath);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Path resolveEntry(Path destination, ZipEntry entry) {
        Path entryPath = destination.resolve(entry.getName()).normalize();
        if (!entryPath.startsWith(destination)) {
            throw new IllegalStateException("Zip entry escapes destination folder: " + entry.getName());
        }
        return entryPath;
    }
}
