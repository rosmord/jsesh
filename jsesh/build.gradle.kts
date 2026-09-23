import org.gradle.plugins.ide.eclipse.model.SourceFolder
import org.gradle.plugins.ide.eclipse.model.Classpath
import org.qenherkhopeshef.jsesh.gradle.LexTask

description = "The core JSesh library"

plugins {
    java
    id("org.qenherkhopeshef.jsesh.cupandlex")
    id("jsesh.java-conventions")
}

// Directory where JLex puts its generated Java sources
val lexGenDir = layout.buildDirectory.dir("generated-sources/lex")

dependencies {
    // Build-time tool: JLex lexer generator
    "cuptools"(project(":cupAndlex"))

    implementation(project(":qenherkhopeshefUtils"))
    implementation(project(":jseshLabels"))
    implementation(project(":cupruntime"))
    implementation(project(":mdwlexer"))
    implementation(libs.itext)
    implementation(libs.jvectclipboard)
    implementation(libs.miglayout) {
        artifact {
            classifier = "swing"
        }
    }
}

// Run JLex lexer generator on MDCLexAux.l
val runLex = tasks.register<LexTask>("runLex") {
    lexFile.set(file("src/jlex/MDCLexAux.l"))
    lexerPackage.set("jsesh.parser.lex")
    outputDirectory.set(lexGenDir)
    lexToolsClasspath.from(configurations["cuptools"])
}

sourceSets {
    main {
        // Add generated source directory so compileJava finds the generated files
        java {
            srcDir(lexGenDir)
        }
    }
}

tasks.compileJava {
    dependsOn(runLex)
}

// Pass the build directory to the tests, so that they can create files there if needed.
// used to test bitmap creation.
tasks.test {
    systemProperty("buildDir", layout.buildDirectory.get().asFile.absolutePath)
}

val projectVersionToken = version.toString()

// Filtered resources: replace ${project.version} (Maven token) with the actual version
tasks.processResources {
    from("src/main/filtered-resources") {
        filter { line -> line.replace("\${project.version}", projectVersionToken) }
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

eclipse {
    classpath {
        file.whenMerged (Action<Classpath> {
            val generatedSourcesDir = layout.buildDirectory.dir("generated-sources").get().asFile
            generatedSourcesDir.listFiles() {
                f -> f.isDirectory
            } .forEach {
                d -> entries.add(SourceFolder(project.relativePath(d), null))
            }
            val generatedResourcesDir = layout.buildDirectory.dir("resources/main").get().asFile
            entries.add(SourceFolder(project.relativePath(generatedResourcesDir), null))
        })
    }
}
