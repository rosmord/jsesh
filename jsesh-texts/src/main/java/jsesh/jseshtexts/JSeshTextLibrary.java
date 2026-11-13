/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jseshtexts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/// Read access to the texts of the JSesh library, directly from the bundled zip file,
/// without exporting them first.
///
/// The library is read once, on first use. Only the `.gly` files are considered "texts".
/// Each text has a *title*, see [#extractTitle(String, String)].
public final class JSeshTextLibrary {

    private static final String TEXTS_ZIP_RESOURCE = "jsesh-texts.zip";
    private static final String TEXT_EXTENSION = ".gly";

    /// How many lines are inspected when looking for a title.
    private static final int MAX_LINES_FOR_TITLE = 60;

    /// A text of the library.
    ///
    /// @param path  path of the text inside the library (with `/` as separator), e.g. `jsesh-texts/Sinuhe.gly`.
    /// @param title the human-readable title: the first line of alphabetic text, or the file name.
    public record Entry(String path, String title) {
    }

    private static JSeshTextLibrary instance;

    private final Map<String, byte[]> contents;
    private final List<Entry> entries;

    private JSeshTextLibrary(Map<String, byte[]> contents) {
        this.contents = contents;
        List<Entry> list = new ArrayList<>();
        for (Map.Entry<String, byte[]> e : contents.entrySet()) {
            String text = new String(e.getValue(), StandardCharsets.UTF_8);
            list.add(new Entry(e.getKey(), extractTitle(e.getKey(), text)));
        }
        Collator collator = Collator.getInstance();
        list.sort(Comparator.comparing(Entry::title, collator)
                .thenComparing(Entry::path));
        this.entries = Collections.unmodifiableList(list);
    }

    /// @return the library bundled with the software.
    public static synchronized JSeshTextLibrary getInstance() {
        if (instance == null) {
            instance = new JSeshTextLibrary(readZip());
        }
        return instance;
    }

    /// @return all the texts, sorted alphabetically by title.
    public List<Entry> getEntries() {
        return entries;
    }

    /// Raw content of a text.
    ///
    /// @param path path of the text, as in [Entry#path()].
    /// @return a copy of the bytes of the text file.
    /// @throws IllegalArgumentException if there is no such text.
    public byte[] readBytes(String path) {
        byte[] bytes = contents.get(path);
        if (bytes == null) {
            throw new IllegalArgumentException("No such text in the library: " + path);
        }
        return bytes.clone();
    }

    /// Compute the title of a text.
    ///
    /// The title is the first non-empty segment of alphabetic text (`+b`, `+i`, `+l`, ...
    /// up to the next `+s` switch back to signs), in the first lines of the file.
    /// Header lines (`++...`) are skipped. If there is no such segment, the title is the
    /// file name without its extension.
    ///
    /// @param path        path or file name of the text.
    /// @param fileContent content of the text file (already decoded).
    /// @return the title, never empty.
    public static String extractTitle(String path, String fileContent) {
        String[] lines = fileContent.split("\r\n|\r|\n", MAX_LINES_FOR_TITLE + 1);
        for (int i = 0; i < lines.length && i < MAX_LINES_FOR_TITLE; i++) {
            String line = lines[i];
            if (line.startsWith("++") || line.startsWith("+O")) {
                continue;
            }
            String title = firstAlphabeticSegment(line);
            if (title != null) {
                return title;
            }
        }
        return fileNameWithoutExtension(path);
    }

    /// @return the first non-blank alphabetic segment of a line, or null.
    private static String firstAlphabeticSegment(String line) {
        int pos = 0;
        while (pos < line.length()) {
            int start = line.indexOf('+', pos);
            if (start == -1 || start + 1 >= line.length()) {
                return null;
            }
            char code = line.charAt(start + 1);
            if (code == 'b' || code == 'i' || code == 'l' || code == 'o' || code == 'u') {
                int end = line.indexOf("+s", start + 2);
                String segment = end == -1 ? line.substring(start + 2) : line.substring(start + 2, end);
                segment = segment.replace("\\+", "+").strip();
                if (!segment.isEmpty()) {
                    return segment;
                }
                pos = end == -1 ? line.length() : end + 2;
            } else {
                pos = start + 2;
            }
        }
        return null;
    }

    private static String fileNameWithoutExtension(String path) {
        String name = path.substring(path.lastIndexOf('/') + 1);
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private static Map<String, byte[]> readZip() {
        Map<String, byte[]> result = new LinkedHashMap<>();
        try (InputStream in = JSeshTextLibrary.class.getResourceAsStream(TEXTS_ZIP_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Resource " + TEXTS_ZIP_RESOURCE + " not found");
            }
            try (ZipInputStream zip = new ZipInputStream(in)) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(TEXT_EXTENSION)) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        zip.transferTo(out);
                        result.put(entry.getName(), out.toByteArray());
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return result;
    }
}
