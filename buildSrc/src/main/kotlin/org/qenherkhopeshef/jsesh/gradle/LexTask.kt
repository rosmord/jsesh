package org.qenherkhopeshef.jsesh.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject

/**
 * Gradle task that runs the JLex lexer generator, mirroring what LexMojo did in Maven.
 *
 * JLex writes its output (lexFile + ".java") next to the input file.
 * This task copies the input to a temp dir first so the source tree stays clean,
 * then moves the result to: outputDirectory / lexerPackage dirs / baseName.java
 * (where baseName is the lex file name without the ".l" suffix).
 */
abstract class LexTask : DefaultTask() {

    @get:InputFile
    abstract val lexFile: RegularFileProperty

    @get:Input
    abstract val lexerPackage: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:InputFiles
    @get:Classpath
    abstract val lexToolsClasspath: ConfigurableFileCollection

    @get:Inject
    protected abstract val execOperations: ExecOperations

    @TaskAction
    fun executeTask() {
        val sourceLex = lexFile.get().asFile
        val tempDir = temporaryDir
        tempDir.mkdirs()

        val tempLexFile = File(tempDir, sourceLex.name)
        Files.copy(sourceLex.toPath(), tempLexFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

        execOperations.javaexec {
            classpath(lexToolsClasspath)
            mainClass.set("JLex.Main")
            args(tempLexFile.absolutePath)
        }

        val jlexOutput = File(tempDir, sourceLex.name + ".java")
        val baseName = if (sourceLex.name.endsWith(".l")) {
            sourceLex.name.substring(0, sourceLex.name.length - 2)
        } else {
            sourceLex.name
        }

        val lexerDir = createPackageDir(outputDirectory.get().asFile, lexerPackage.get())
        Files.move(
            jlexOutput.toPath(),
            File(lexerDir, "$baseName.java").toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )
    }

    private fun createPackageDir(base: File, pkg: String): File {
        var dir = base
        for (part in pkg.split('.')) {
            dir = File(dir, part)
        }
        dir.mkdirs()
        return dir
    }
}
