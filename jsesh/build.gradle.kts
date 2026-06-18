import org.qenherkhopeshef.jsesh.gradle.CupTask
import org.qenherkhopeshef.jsesh.gradle.LexTask

description = "The core JSesh library"

plugins {
    java
    id("org.qenherkhopeshef.jsesh.cupandlex")
    id("jsesh.java-conventions")
}

// Directories where CUP and JLex put their generated Java sources
val cupGenDir = layout.buildDirectory.dir("generated-sources/cup")
val lexGenDir = layout.buildDirectory.dir("generated-sources/lex")

dependencies {
    // Build-time tool: CUP parser generator + JLex lexer generator
    "cuptools"(project(":cupAndlex"))

    implementation(project(":qenherkhopeshefUtils"))
    implementation(project(":jseshLabels"))
    implementation(project(":cupruntime"))
    implementation(libs.itext)
    implementation(libs.jvectclipboard)
    implementation(libs.forms)
    implementation(libs.miglayout) {
        artifact {
            classifier = "swing"
        }
    }
}

// Run CUP parser generator on MDCParse.y
val runCup = tasks.register<CupTask>("runCup") {
    grammarFile.set(file("src/jcup/MDCParse.y"))
    parserName.set("MDCParse")
    symbolsName.set("MDCSymbols")
    parserPackage.set("jsesh.mdc.parser")
    lexerPackage.set("jsesh.mdc.lex")
    outputDirectory.set(cupGenDir)
    cupToolsClasspath.from(configurations["cuptools"])
}

// Run JLex lexer generator on MDCLexAux.l
val runLex = tasks.register<LexTask>("runLex") {
    lexFile.set(file("src/jlex/MDCLexAux.l"))
    lexerPackage.set("jsesh.mdc.lex")
    outputDirectory.set(lexGenDir)
    lexToolsClasspath.from(configurations["cuptools"])
}

sourceSets {
    main {
        // Add generated source directories so compileJava finds the generated files
        java {
            srcDir(cupGenDir)
            srcDir(lexGenDir)
        }
    }
}

tasks.compileJava {
    dependsOn(runCup, runLex)
}

val projectVersionToken = version.toString()

// Filtered resources: replace ${project.version} (Maven token) with the actual version
tasks.processResources {
    from("src/main/filtered-resources") {
        filter { line -> line.replace("\${project.version}", projectVersionToken) }
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
