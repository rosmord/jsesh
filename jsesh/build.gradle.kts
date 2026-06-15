import org.qenherkhopeshef.jsesh.gradle.CupTask
import org.qenherkhopeshef.jsesh.gradle.LexTask

plugins {
    java
    `maven-publish`
    id("org.qenherkhopeshef.jsesh.cupandlex")
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
    implementation("com.lowagie:itext:2.1.5")
    implementation("org.qenherkhopeshef:jvectclipboard:8.1.0")
    implementation("com.jgoodies:forms:1.0.7")
    implementation("com.miglayout:miglayout:3.7.4:swing")
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

// Filtered resources: replace ${project.version} (Maven token) with the actual version
tasks.processResources {
    from("src/main/filtered-resources") {
        filter { line -> line.replace("\${project.version}", project.version.toString()) }
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
