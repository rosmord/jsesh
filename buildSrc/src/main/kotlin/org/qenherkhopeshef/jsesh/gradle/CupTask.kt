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
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.inject.Inject

/**
 * Gradle task that runs the CUP parser generator, mirroring what CupMojo did in Maven.
 *
 * CUP generates two files in the temp dir:
 * - parserName.java  -> outputDirectory / parserPackage dirs / parserName.java
 * - symbolsName.java -> (package line rewritten) -> outputDirectory / lexerPackage dirs / symbolsName.java
 */
abstract class CupTask : DefaultTask() {

    @get:InputFile
    abstract val grammarFile: RegularFileProperty

    @get:Input
    abstract val parserName: Property<String>

    @get:Input
    abstract val symbolsName: Property<String>

    @get:Input
    abstract val parserPackage: Property<String>

    @get:Input
    abstract val lexerPackage: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:InputFiles
    @get:Classpath
    abstract val cupToolsClasspath: ConfigurableFileCollection

    @get:Inject
    protected abstract val execOperations: ExecOperations

    @TaskAction
    fun executeTask() {
        val parserPkg = parserPackage.get()
        val lexerPkg = lexerPackage.get()
        val parser = parserName.get()
        val symbols = symbolsName.get()
        val outputDir = outputDirectory.get().asFile
        val tempDir = temporaryDir
        tempDir.mkdirs()

        execOperations.javaexec {
            classpath(cupToolsClasspath)
            mainClass.set("java_cup.Main")
            args(
                "-outputDir", tempDir.absolutePath,
                "-package", parserPkg,
                "-dump", "-interface",
                "-parser", parser,
                "-symbols", symbols,
                grammarFile.get().asFile.absolutePath
            )
        }

        val tempParser = File(tempDir, "$parser.java")
        val parserDir = createPackageDir(outputDir, parserPkg)
        Files.move(
            tempParser.toPath(),
            File(parserDir, "$parser.java").toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        val tempSymbols = File(tempDir, "$symbols.java")
        var content = Files.readString(tempSymbols.toPath(), StandardCharsets.UTF_8)
        content = content.replace("package $parserPkg;", "package $lexerPkg;")
        val lexerDir = createPackageDir(outputDir, lexerPkg)
        Files.writeString(
            File(lexerDir, "$symbols.java").toPath(),
            content,
            StandardCharsets.UTF_8
        )
        tempSymbols.delete()
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
