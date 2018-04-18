package jsesh.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Count of files in a folder.
 */
public class FileCountHelper {

    public static long countFiles(Path folder) {
        try {
            long count= Files.walk(folder)
                    .filter(path -> Files.isRegularFile(path))
                    .count();
            return count;
        } catch (IOException e) {
            throw new RuntimeException();
        }

    }

    /** Helper class, no instances. */
    private FileCountHelper() {
    }
}
