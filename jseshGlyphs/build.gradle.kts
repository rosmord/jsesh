plugins {
    id("jsesh.java-conventions")
}

description = "JSesh extended glyphs library."



// Secondary task to create a list of glyphs.
val generateGlyphList = tasks.register("generateGlyphList") {

    // Define sources and output
    val svgSource = sourceSets["main"].resources.matching { include("jseshGlyphs/*.svg") }
    val outputFile = layout.buildDirectory.file("resources/main/jseshGlyphs/list.txt")

    // declare them as such, to avoid rebuilding when nothing has changed
    inputs.files(svgSource)
    outputs.file(outputFile)

    doLast {
        outputFile.get().asFile.printWriter().use { writer ->
            svgSource.files.sortedBy { it.name }.forEach { writer.println(it.name) }
        }
    }
}

tasks.named("jar") {
    dependsOn(generateGlyphList)
}

// This module has no Java sources, so compileJava is skipped (NO-SOURCE) and
// never creates build/classes/java/main. VS Code's Gradle build server still
// puts that directory on the classpath of dependent projects (jseshAppli,
// signInfoAppli) and reports it as a missing library. Create it with the resources.
val javaClassesDir = sourceSets["main"].java.destinationDirectory
tasks.named("processResources") {
    doLast { javaClassesDir.get().asFile.mkdirs() }
}
