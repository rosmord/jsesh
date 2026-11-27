/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jseshtemplates;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/// Read access to the document templates bundled with JSesh.
///
/// The templates are read once, on first use, from a zip file. Only the `.gly`
/// files are templates. Each one starts with a header giving its name and its
/// description, ended by a line containing the text `END` (see the README of
/// the module).
public final class JSeshTemplateLibrary {

    private static final String TEMPLATES_ZIP_RESOURCE = "jsesh-templates.zip";
    private static final String TEMPLATE_EXTENSION = ".gly";

    /// A template.
    ///
    /// @param path        path of the template inside the library (with `/` as
    ///                    separator), e.g. `jsesh-templates/Numbered lines.gly`.
    /// @param name        its name, as given by its header.
    /// @param description its description, possibly empty, with `\n` between lines.
    public record Template(String path, String name, String description) {
    }

    private static JSeshTemplateLibrary instance;

    private final Map<String, String> documents = new HashMap<>();
    private final List<Template> templates;

    private JSeshTemplateLibrary(Map<String, String> contents) {
        List<Template> list = new ArrayList<>();
        contents.forEach((path, content) -> {
            TemplateParser.ParsedTemplate parsed;
            try {
                parsed = TemplateParser.parse(content);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Incorrect template " + path, e);
            }
            list.add(new Template(path, parsed.name(), parsed.description()));
            documents.put(path, parsed.document());
        });
        Collator collator = Collator.getInstance();
        list.sort(Comparator.comparing(Template::name, collator)
                .thenComparing(Template::path));
        this.templates = Collections.unmodifiableList(list);
    }

    /// @return the templates bundled with the software.
    public static synchronized JSeshTemplateLibrary getInstance() {
        if (instance == null) {
            instance = new JSeshTemplateLibrary(readZip());
        }
        return instance;
    }

    /// @return all the templates, sorted alphabetically by name.
    public List<Template> getTemplates() {
        return templates;
    }

    /// The document to create from a template: its document preferences and its
    /// content, without the template header.
    ///
    /// @param path path of the template, as in [Template#path()].
    /// @return the content of a new `.gly` file, in UTF-8.
    /// @throws IllegalArgumentException if there is no such template.
    public byte[] readDocument(String path) {
        String document = documents.get(path);
        if (document == null) {
            throw new IllegalArgumentException("No such template: " + path);
        }
        return document.getBytes(StandardCharsets.UTF_8);
    }

    private static Map<String, String> readZip() {
        Map<String, String> result = new HashMap<>();
        try (InputStream in = JSeshTemplateLibrary.class.getResourceAsStream(TEMPLATES_ZIP_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Resource " + TEMPLATES_ZIP_RESOURCE + " not found");
            }
            try (ZipInputStream zip = new ZipInputStream(in)) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(TEMPLATE_EXTENSION)) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        zip.transferTo(out);
                        result.put(entry.getName(), out.toString(StandardCharsets.UTF_8));
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return result;
    }
}
