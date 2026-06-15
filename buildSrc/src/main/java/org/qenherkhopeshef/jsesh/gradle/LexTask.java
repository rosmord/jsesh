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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Gradle task that runs the JLex lexer generator, mirroring what LexMojo did in Maven.
 *
 * JLex writes its output (lexFile + ".java") next to the input file.
 * This task copies the input to a temp dir first so the source tree stays clean,
 * then moves the result to: outputDirectory / lexerPackage dirs / baseName.java
 * (where baseName is the lex file name without the ".l" suffix).
 */
public abstract class LexTask extends DefaultTask {

    @InputFile
    public abstract RegularFileProperty getLexFile();

    @Input
    public abstract Property<String> getLexerPackage();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @InputFiles
    @Classpath
    public abstract ConfigurableFileCollection getLexToolsClasspath();

    @Inject
    protected abstract ExecOperations getExecOperations();

    @TaskAction
    public void execute() throws Exception {
        File lexFile = getLexFile().get().getAsFile();
        File tempDir = getTemporaryDir();
        tempDir.mkdirs();

        // Copy lex file to temp dir so JLex writes its output there (next to the input)
        File tempLexFile = new File(tempDir, lexFile.getName());
        Files.copy(lexFile.toPath(), tempLexFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        getExecOperations().javaexec(spec -> {
            spec.classpath(getLexToolsClasspath());
            spec.getMainClass().set("JLex.Main");
            spec.args(tempLexFile.getAbsolutePath());
        });

        // JLex creates <inputFile>.java next to the input
        File jlexOutput = new File(tempDir, lexFile.getName() + ".java");

        // Determine output file base name: strip ".l" extension
        String name = lexFile.getName();
        String base = name.endsWith(".l") ? name.substring(0, name.length() - 2) : name;

        File outputDir = getOutputDirectory().get().getAsFile();
        File lexerDir = createPackageDir(outputDir, getLexerPackage().get());
        Files.move(jlexOutput.toPath(), new File(lexerDir, base + ".java").toPath(),
                StandardCopyOption.REPLACE_EXISTING);
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
