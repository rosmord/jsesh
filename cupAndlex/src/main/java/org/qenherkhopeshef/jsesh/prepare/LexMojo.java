package org.qenherkhopeshef.jsesh.prepare;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Process lex files.
 * 
 * //@goal lex
 * //@phase generate-sources
 * @author rosmord
 */
@Mojo(name = "lex", defaultPhase = LifecyclePhase.GENERATE_SOURCES, 
	requiresDependencyResolution = ResolutionScope.COMPILE, requiresProject = true, threadSafe = true)
public class LexMojo extends AbstractMojo {

	/**
	 * The output directory.

	 */
	@Parameter(property = "outputDirName", defaultValue = "${project.build.directory}", required = true)
	private String outputDirName;


	@Parameter(defaultValue = "${project}", property = "project")
	private MavenProject project;

	/**
	 * //@parameter property="cup.lexerPackage"
	 * //@required
	 */
	@Parameter(property = "cup.lexerPackage")
	private String lexerPackage;

	/**
	 * Path to lexicon definition file.
	 *
	 */
	@Parameter(property = "lex.lexDef", required = true)

	private String lexDef;

	@Component
	private BuildContext buildContext;

	public void execute() throws MojoExecutionException, MojoFailureException {
		String[] args = { lexDef };
		try {
			File sourceFile = new File(lexDef);
			buildContext.refresh(sourceFile);
			buildContext.removeMessages(sourceFile);

			// JLex is supposed to create a file in the SAME folder as the source.
			// We will need to move it.
			String resultPath = lexDef + ".java";
			// The file created by JLex
			File tempResult = new File(resultPath);
			// Compute the resulting file name
			int endPos = tempResult.getName().lastIndexOf(".l.java");
			if (endPos == -1)
				throw new MojoExecutionException("lex file names must end in '.l' " + lexDef);

			String finalResultName = tempResult.getName().substring(0, endPos) + ".java";
			File finalResultFile = new File(createPackage(lexerPackage), finalResultName);

			// Avoid unnecessary processing
			if (finalResultFile.lastModified() > sourceFile.lastModified())
				return;
			JLex.Main.main(args);
			if (finalResultFile.exists()) {
				finalResultFile.delete();
			}
			if (tempResult.exists()) {
				tempResult.renameTo(finalResultFile);
				project.addCompileSourceRoot(getJavaSourcePath().getAbsolutePath());
			}
		} catch (IOException e) {
			throw new MojoExecutionException("error in jlex", e);
		}

	}

	/**
	 * Create a package and returns the corresponding file.
	 * 
	 * @param packageName
	 * @return
	 */
	private File createPackage(String packageName) {
		File result = getJavaSourcePath();
		// ok. Now use the package name...
		String[] folders = packageName.trim().split("\\.");
		for (String folder : folders) {
			result = new File(result, folder);
		}
		result.mkdirs();
		return result;
	}

	private File getJavaSourcePath() {
		File result = new File(outputDirName);
		result = new File(result, "generated-sources");
		result = new File(result, "lex");
		// result= new File(result, "java");
		return result;
	}
}
