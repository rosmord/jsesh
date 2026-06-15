package org.qenherkhopeshef.jsesh.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Gradle task that runs the CUP parser generator, mirroring what CupMojo did in Maven.
 *
 * CUP generates two files in the temp dir:
 *   - parserName.java  → outputDirectory / parserPackage dirs / parserName.java
 *   - symbolsName.java → (package line rewritten) → outputDirectory / lexerPackage dirs / symbolsName.java
 */
public abstract class CupTask extends DefaultTask {

    @InputFile
    public abstract RegularFileProperty getGrammarFile();

    @Input
    public abstract Property<String> getParserName();

    @Input
    public abstract Property<String> getSymbolsName();

    @Input
    public abstract Property<String> getParserPackage();

    @Input
    public abstract Property<String> getLexerPackage();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getCupToolsClasspath();

    @Inject
    protected abstract ExecOperations getExecOperations();

    @TaskAction
    public void execute() throws Exception {
        String parserPkg = getParserPackage().get();
        String lexerPkg = getLexerPackage().get();
        String parserName = getParserName().get();
        String symbolsName = getSymbolsName().get();
        File outputDir = getOutputDirectory().get().getAsFile();
        File tempDir = getTemporaryDir();
        tempDir.mkdirs();

        getExecOperations().javaexec(spec -> {
            spec.classpath(getCupToolsClasspath());
            spec.getMainClass().set("java_cup.Main");
            spec.args(
                    "-outputDir", tempDir.getAbsolutePath(),
                    "-package", parserPkg,
                    "-dump", "-interface",
                    "-parser", parserName,
                    "-symbols", symbolsName,
                    getGrammarFile().get().getAsFile().getAbsolutePath()
            );
        });

        // Move parser file to its package directory
        File tempParser = new File(tempDir, parserName + ".java");
        File parserDir = createPackageDir(outputDir, parserPkg);
        Files.move(tempParser.toPath(), new File(parserDir, parserName + ".java").toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        // Rewrite package declaration in symbols file and move to lexer package directory
        File tempSymbols = new File(tempDir, symbolsName + ".java");
        String content = Files.readString(tempSymbols.toPath(), StandardCharsets.UTF_8);
        content = content.replace("package " + parserPkg + ";", "package " + lexerPkg + ";");
        File lexerDir = createPackageDir(outputDir, lexerPkg);
        Files.writeString(new File(lexerDir, symbolsName + ".java").toPath(), content, StandardCharsets.UTF_8);
        tempSymbols.delete();
    }

    private File createPackageDir(File base, String pkg) {
        File dir = base;
        for (String part : pkg.split("\\.")) {
            dir = new File(dir, part);
        }
        dir.mkdirs();
        return dir;
    }
}
