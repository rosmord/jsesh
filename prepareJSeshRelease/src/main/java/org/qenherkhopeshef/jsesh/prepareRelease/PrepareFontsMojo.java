package org.qenherkhopeshef.jsesh.prepareRelease;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import jsesh.hieroglyphs.graphics.ResourcesHieroglyphicFontManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Process JSesh fonts for release
 *
 * @goal prepareFonts
 * @phase process-classes
 */
public class PrepareFontsMojo extends AbstractMojo {

    /**
     * package where the fonts are stored.
     * @parameter glyphsPackage="glyphsPackage"
     * @required
     */
    private String glyphsPackage;
    
    /**
     * The output directory.
     * @parameter default-value="${project.build.directory}"
     * @readonly
     */
    private String outputDirPath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            File outputDir = new File(outputDirPath, "classes");
            if (glyphsPackage == null) {
                throw new MojoExecutionException("glyphs package unset");

            }
            String folders[] = glyphsPackage.split("\\.");
            File fontPath = buildDirName(outputDir, Arrays.asList(folders));
            if (!fontPath.exists()) {
                throw new MojoExecutionException("Folder "
                        + fontPath.getAbsolutePath() + " must exist.");

            }
            ResourcesHieroglyphicFontManager.initDirectory(fontPath);
        } catch (IOException ex) {
            throw new MojoExecutionException("error", ex);
        }
    }

    private File buildDirName(File outputDir, List<String> folders) {
        if (folders.isEmpty()) {
            return outputDir;

        } else {
            File dir = buildDirName(outputDir, folders.subList(0, folders.size() - 1));
            return new File(dir, folders.get(folders.size() - 1));
        }
    }
}
