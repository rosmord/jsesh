description = "The core library for JSesh"

plugins {
    id("org.qenherkhopeshef.jsesh.java-conventions")
}

// Custom configurations for code generation tools
val jlexClasspath: Configuration by configurations.creating
val cupClasspath: Configuration by configurations.creating

dependencies {
    api(project(":qenherkhopeshefUtils"))
    api(project(":jseshLabels"))
    api(project(":cupruntime"))
    api("com.lowagie:itext:2.1.5")
    api("org.qenherkhopeshef:jvectclipboard:8.1.0")
    api("com.jgoodies:forms:1.0.7")
    api("org.swinglabs:swing-layout:1.0.3")
    
    // Add cupAndlex project for code generation
    jlexClasspath(project(":cupAndlex"))
    cupClasspath(project(":cupAndlex"))
}

// Directories for generated sources
val generatedLexDir = layout.buildDirectory.dir("generated-sources/lex")
val generatedCupDir = layout.buildDirectory.dir("generated-sources/cup")

// Task to generate lexer using JLex
val generateLex by tasks.registering(JavaExec::class) {
    classpath = jlexClasspath
    mainClass.set("JLex.Main")
    
    val inputFile = file("src/jlex/MDCLexAux.l")
    val outputDir = generatedLexDir.get().asFile
    
    inputs.file(inputFile)
    outputs.dir(outputDir)
    
    args(inputFile.absolutePath)
    
    doFirst {
        outputDir.mkdirs()
    }
    
    // JLex outputs to the same directory as input, so we need to move it
    doLast {
        val lexFile = file("src/jlex/MDCLexAux.l.java")
        if (lexFile.exists()) {
            val targetDir = file("$outputDir/jsesh/mdc/lex")
            targetDir.mkdirs()
            lexFile.renameTo(file("$targetDir/MDCLexAux.java"))
        }
    }
}

// Task to generate parser using CUP  
val generateCup by tasks.registering(JavaExec::class) {
    dependsOn(generateLex)

    val parserPackage = "jsesh.mdc.parser"
    val lexPackage = "jsesh.mdc.lex"
    val parserClass = "MDCParse"
    val symbolsClass = "MDCSymbols"

    // Inputs and outputs
    val inputFile = file("src/jcup/$parserClass.y")
    val outputDir = generatedCupDir.get().asFile
    inputs.file(inputFile)
    outputs.dir(outputDir)

    val parserPackagePath = parserPackage.replace('.', '/')
    val lexPackagePath = lexPackage.replace('.', '/')
    val parserDir = file("$outputDir/$parserPackagePath")
    val lexDir = file("$outputDir/$lexPackagePath")

    // Configure execution
    classpath = cupClasspath
    mainClass.set("java_cup.Main")

    args(
        "-interface",
        "-parser", parserClass,
        "-symbols", symbolsClass,
        "-package", parserPackage,
        "-outputDir", parserDir.absolutePath,
        inputFile.absolutePath,
    )
    
    doFirst {
        // Create directories for generated files
        parserDir.mkdirs()
        lexDir.mkdirs()
    }

    doLast {
        // Move symbols to the lex package
        val symbolsFilename = "$symbolsClass.java"

        val sourceFile = file("$parserDir/$symbolsFilename")
        val targetFile = file("$lexDir/$symbolsFilename")

        require(sourceFile.exists()) {
            "Expected CUP-generated $sourceFile not found"
        }

        val updatedText = sourceFile.readText()
            .replaceFirst(
                "package $parserPackage;",
                "package $lexPackage;"
            )

        targetFile.writeText(updatedText)
        sourceFile.delete()
    }
}

// Add generated sources to source sets
sourceSets {
    main {
        java {
            srcDir(generatedLexDir)
            srcDir(generatedCupDir)
        }
    }
}

// Make compileJava depend on code generation
tasks.named("compileJava") {
    dependsOn(generateLex, generateCup)
}

// Map project properties to match Maven representation
val props = mapOf(
    "project.version" to project.version,
)

// Helper to expand properties
val expandLineRegex = Regex("""\$\{([\w.]+)\}""")
fun expandLine(line: String): String {
    return line.replace(expandLineRegex) {
        val varName = it.groupValues[1]
        val value = props[varName] ?: it.groupValues[0]
        "$value"
    }
}

// Configure resources with filtering
tasks.processResources {
    from("src/main/filtered-resources") {
        filter(::expandLine)
    }
}
