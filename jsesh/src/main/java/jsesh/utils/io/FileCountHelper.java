/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 * 
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.utils.io;

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
