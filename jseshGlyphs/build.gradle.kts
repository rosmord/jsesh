description = "JSesh extended glyphs library."

plugins {
    id("jsesh.java-conventions")
}


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
