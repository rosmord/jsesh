package org.qenherkhopeshef.jsesh.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Rewrites every localized label file so that it mirrors the structure of the
 * reference file (labels.properties):
 *
 *  * comments and blank lines of the reference file are kept;
 *  * each definition is rewritten with the same key, but with the value found
 *    in the localized file;
 *  * when the localized file has no value for a key, the reference (English)
 *    value is copied instead, preceded by a "TO TRANSLATE" comment;
 *  * keys of the localized file which are unknown to the reference file are
 *    kept in a trailing section, so that no translation is ever lost;
 *  * keys matching [nonTranslatableKeys] (accelerators, and the like) always
 *    take the reference value, and get no "TO TRANSLATE" comment.
 *
 * Values are copied verbatim: a value written with \\uXXXX escapes stays
 * escaped, a value written in plain UTF-8 stays plain. The task only moves
 * text around, it never re-encodes it.
 *
 * Files are rewritten in place; the task is meant to be run on demand by a
 * developer, and its result reviewed with git.
 */
abstract class UpdateLabelsTask : DefaultTask() {

    companion object {
        const val TO_TRANSLATE_MARKER = "# TO TRANSLATE"

        private val ORPHAN_HEADER = listOf(
            "",
            "# ============================================================",
            "# Keys which are not in labels.properties.",
            "# They are probably obsolete, or their key is misspelled.",
            "# This section is preserved by the update-labels task.",
            "# ============================================================",
            ""
        )
    }

    /**
     * In-memory representation of lines in the property files.
     */
    private sealed interface LogicalLine {
        /** A comment or a blank line. */
        data class Verbatim(val text: String) : LogicalLine

        /**
         * A definition. [key] and [rawValue] the source text, still escaped;
         */
        data class Definition(val key: String, val rawValue: String) : LogicalLine
    }


    /** The reference file: gives the structure, the keys, and the default values. */
    @get:InputFile
    abstract val referenceFile: RegularFileProperty

    /**
     * Directory holding the localized files. Every "*.properties" file in it,
     * save the reference file itself, is rewritten in place.
     *
     * Not an @InputDirectory: the task rewrites the very files it reads, so
     * up-to-date checking would be meaningless here.
     */
    @get:Internal
    abstract val labelsDirectory: DirectoryProperty

    /**
     * Keys which are technical, and thus never translated: they always keep the
     * reference value, and are not flagged as "TO TRANSLATE". Patterns are globs,
     * where "*" stands for any text.
     *
     * The default covers the keyboard accelerators, including their platform
     * variants ("edit.redo.AcceleratorKey[mac]"), and the MdC codes of the icons.
     */
    @get:Input
    abstract val nonTranslatableKeys: ListProperty<String>

    init {
        nonTranslatableKeys.convention(
            listOf(
                "accelerator.*",
                "*.accelerator",
                "*.AcceleratorKey",
                "*.AcceleratorKey[*]",
                "*.iconMdC",
                "*.IconMdC"
            )
        )
        // The task's inputs are also its outputs: never consider it up-to-date.
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun updateLabels() {
        val reference = referenceFile.get().asFile
        val referenceLines = readLogicalLines(reference)

        val localizedFiles = labelsDirectory.get().asFile
            .listFiles { f -> f.isFile && f.name.endsWith(".properties") && f != reference }
            ?.sortedBy { it.name }
            ?: emptyList()

        if (localizedFiles.isEmpty()) {
            logger.warn("No localized file found in ${labelsDirectory.get().asFile}")
            return
        }
        val nonTranslatable = nonTranslatableKeys.get().map { globToRegex(it) }
        for (localizedFile in localizedFiles) {
            update(localizedFile, referenceLines, nonTranslatable)
        }
    }

    private fun update(localizedFile: File, referenceLines: List<LogicalLine>, nonTranslatable: List<Regex>) {
        val translations = readTranslations(localizedFile)
        val used = mutableSetOf<String>()
        val result = StringBuilder()
        var missing = 0
        var technical = 0
        var translated = 0

        for (line in referenceLines) {
            when (line) {
                is LogicalLine.Verbatim -> result.append(line.text).append('\n')
                is LogicalLine.Definition -> {
                    val translation = translations[line.key]
                    if (nonTranslatable.any { it.matches(line.key) }) {
                        // Technical key: the reference always wins, so that it can never
                        // drift away from labels.properties.
                        technical++
                        used += line.key
                        if (translation != null && translation != line.rawValue) {
                            logger.warn(
                                "${localizedFile.name}: '${line.key}' is a technical key: its value " +
                                    "'$translation' is replaced by the reference value '${line.rawValue}'."
                            )
                        }
                        result.append(line.key).append('=').append(line.rawValue).append('\n')
                    } else if (translation != null) {
                        translated++
                        used += line.key
                        result.append(line.key).append('=').append(translation).append('\n')
                    } else {
                        missing++
                        result.append(TO_TRANSLATE_MARKER).append('\n')
                        result.append(line.key).append('=').append(line.rawValue).append('\n')
                    }
                }
            }
        }

        val orphans = translations.filterKeys { it !in used }
        if (orphans.isNotEmpty()) {
            ORPHAN_HEADER.forEach { result.append(it).append('\n') }
            orphans.forEach { (key, value) -> result.append(key).append('=').append(value).append('\n') }
        }

        localizedFile.writeText(result.toString(), Charsets.UTF_8)
        logger.lifecycle(
            "${localizedFile.name}: $translated translated, $missing to translate, " +
                "$technical technical, ${orphans.size} unknown key(s) kept."
        )
        if (orphans.isNotEmpty()) {
            logger.lifecycle("    unknown keys: ${orphans.keys.joinToString(", ")}")
        }
    }

    /**
     * Values of the localized file, by key. An entry preceded by the
     * "TO TRANSLATE" marker is *not* a translation: it is left out, so that the
     * marker is put back by the next run and only disappears once someone has
     * really translated the entry.
     */
    private fun readTranslations(file: File): Map<String, String> {
        val translations = LinkedHashMap<String, String>()
        var toTranslate = false
        for (line in readLogicalLines(file)) {
            when (line) {
                is LogicalLine.Verbatim ->
                    if (line.text.isNotBlank()) toTranslate = line.text.trim() == TO_TRANSLATE_MARKER
                is LogicalLine.Definition -> {
                    if (!toTranslate) translations[line.key] = line.rawValue
                    toTranslate = false
                }
            }
        }
        return translations
    }

    /** Turns a glob, where "*" stands for any text, into the matching regex. */
    private fun globToRegex(glob: String): Regex =
        Regex(glob.split('*').joinToString(".*") { Regex.escape(it) })


    private fun readLogicalLines(file: File): List<LogicalLine> {
        val lines = file.readLines(Charsets.UTF_8)
        val result = mutableListOf<LogicalLine>()
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trimStart()
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("!")) {
                result += LogicalLine.Verbatim(line)
                i++
                continue
            }
            // A definition may be continued on the next lines with a trailing backslash.
            val logical = StringBuilder(line)
            while (endsWithContinuation(logical) && i + 1 < lines.size) {
                i++
                logical.append('\n').append(lines[i])
            }
            i++
            result += parseDefinition(logical.toString())
        }
        return result
    }

    /** True when the line ends with an odd number of backslashes, i.e. an escaped end of line. */
    private fun endsWithContinuation(line: CharSequence): Boolean {
        var backslashes = 0
        var i = line.length - 1
        while (i >= 0 && line[i] == '\\') {
            backslashes++
            i--
        }
        return backslashes % 2 == 1
    }

    private fun parseDefinition(logical: String): LogicalLine {
        val separator = indexOfSeparator(logical)
        if (separator < 0) {
            // No separator at all: a key with an empty value. Keep the line as it is.
            return LogicalLine.Verbatim(logical)
        }
        val rawKey = logical.substring(0, separator).trim()
        val rawValue = logical.substring(separator + 1).trimStart()
        return LogicalLine.Definition(rawKey, rawValue)
    }

    /** Index of the first unescaped '=' or ':'. */
    private fun indexOfSeparator(line: String): Int {
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\\') {
                i += 2
                continue
            }
            if (c == '=' || c == ':') return i
            i++
        }
        return -1
    }
}
