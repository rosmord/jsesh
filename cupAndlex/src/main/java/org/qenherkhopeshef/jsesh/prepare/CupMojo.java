package org.qenherkhopeshef.jsesh.prepare;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Process CUP grammar and produces the corresponding java sources.
 * 
 * @goal cup
 * @phase generate-sources
 */
public class CupMojo extends AbstractMojo {

	/**
	 * The output directory.
	 * @parameter  default-value="${project.build.directory}"
	 * @readonly
	 */
	private String outputDirName;
	
	/**
	 * @parameter default-value="${project}";
	 */
	private MavenProject project;

	/**
	 * Name of the parser class to create.
	 * @parameter expression="${cup.parserName}" default-value="Toto"
	 * @required
	 */
	private String parserName;

	/**
	 * @parameter expression="${cup.symbolName}"
	 * @required
	 */

	private String symbolsName;

	/**
	 * @parameter expression="${cup.grammarFilePath}"
	 * @required
	 */
	private String grammarFilePath;
	
	/**
	 * Name of the directory where temporary files will be created.
	 * @parameter expression="${cup.tempDirName}" default-value="tmp"
	 * @required
	 */

	private String tempDirName;

	/**
	 * @parameter expression="${cup.parserPackage}" 
	 * @required
	 */

	private String parserPackage;
	
	/**
	 * @parameter expression="${cup.lexerPackage}"
	 * @required
	 */
	private String lexerPackage;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		// A) build the directory for temporary outputs
		File outputDir= new File(outputDirName);		
		File temporaryDir= new File(outputDir, tempDirName);
		temporaryDir.mkdirs();
		// Run CUP
		String[] args= {
				"-outputDir", temporaryDir.getPath(),
				"-package", parserPackage,
				"-dump", "-interface", 
				"-parser", parserName,
				"-symbols", symbolsName,
				grammarFilePath
		};	
		try {
			// The source file
			File sourceFile= new File(grammarFilePath);
			// The parser file created by cups
			File tempParserFile= new File(temporaryDir, parserName + ".java");		
			// The parser file in its final folder.
			File targetParserFile = new File(createParserDir(), parserName+ ".java");
			
			// Test the creation date
			if (sourceFile.lastModified() < targetParserFile.lastModified())
				return;
		
			java_cup.Main.main(args);
			// Move the generated files where they belong
			tempParserFile.renameTo(targetParserFile);
			
			// The symbol file must have its package name changed too.
			// We work in UTF-8
			
			File symbolFile= new File(temporaryDir, symbolsName + ".java");
			File symbolDir= createSymbolDir();

			BufferedReader symbolReader= new BufferedReader(new InputStreamReader(new FileInputStream(symbolFile), "UTF-8"));
			StringWriter s= new StringWriter();
			int c;
			while ((c= symbolReader.read())!= -1) {
				s.write(c);
			}
			symbolReader.close();
			String newSymbols= s.toString().replaceAll("package "+parserPackage+";", "package "+lexerPackage+";");
			Writer w= new OutputStreamWriter(new FileOutputStream(new File(symbolDir, symbolsName + ".java")), "UTF-8");
			w.write(newSymbols);
			w.close();
			project.addCompileSourceRoot(getJavaSourcePath().getPath());
			symbolFile.delete();
		} catch (Exception e) {
			throw new MojoExecutionException("Cup failure",e);
		}
	}

	/**
	 * Create and return parser folder.
	 * @return
	 */
	private File createParserDir() {
		return createPackage(parserPackage);
	}

	/**
	 * Create and return symbol folder.
	 * @return
	 */
	private File createSymbolDir() {
		return createPackage(lexerPackage);
	}
	
	private File getJavaSourcePath() {
		File result= new File(outputDirName);
		result= new File(result, "generated-sources");
		result= new File(result, "cup");
		return result;
	}
	
	private File createPackage(String packageName) {
		File result= getJavaSourcePath();
		// ok. Now use the package name...
		String[] folders = packageName.trim().split("\\.");
		for (String folder : folders) {
			result= new File(result, folder);
		}
		result.mkdirs();
		return result;
		
	}
	
}
