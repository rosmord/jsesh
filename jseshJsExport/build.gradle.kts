description = """
    Exports JSesh data (glyphs, sign database, labels) and golden test fixtures
    for the JavaScript port in jsesh-js/.
    """

plugins {
    id("jsesh.java-conventions")
}

dependencies {
    implementation(project(":jsesh"))
    implementation(project(":jseshGlyphs"))
}

val jsRoot = rootProject.layout.projectDirectory.dir("jsesh-js")

val exportFixtures = tasks.register<JavaExec>("exportFixtures") {
    group = "jsesh-js"
    description = "Writes parser, writer and layout fixtures into jsesh-js/fixtures/generated."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("jsesh.jsexport.ExportFixtures")
    systemProperty("java.awt.headless", "true")
    args(
        jsRoot.dir("fixtures/generated").asFile.absolutePath,
        rootProject.layout.projectDirectory.dir("jsesh-texts/src/main/assets/jsesh-texts").asFile.absolutePath,
    )
}

val exportGlyphData = tasks.register<JavaExec>("exportGlyphData") {
    group = "jsesh-js"
    description = "Writes the glyph index and the sign shapes into jsesh-js/packages/glyphs/data."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("jsesh.jsexport.ExportGlyphData")
    systemProperty("java.awt.headless", "true")
    args(jsRoot.dir("packages/glyphs/data").asFile.absolutePath)
}

val exportSignInfo = tasks.register<JavaExec>("exportSignInfo") {
    group = "jsesh-js"
    description = "Writes the sign database (families, values, tags, variants) into jsesh-js/packages/glyphs/data/sign-info.json."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("jsesh.jsexport.ExportSignInfo")
    systemProperty("java.awt.headless", "true")
    args(jsRoot.dir("packages/glyphs/data").asFile.absolutePath)
}

val exportPictures = tasks.register<JavaExec>("exportPictures") {
    group = "jsesh-js"
    description = "Draws pictures of the test texts with JSesh, into jsesh-js/fixtures/generated/pictures (for jsesh-js/examples/compare)."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("jsesh.jsexport.ExportPictures")
    systemProperty("java.awt.headless", "true")
    args(
        jsRoot.dir("fixtures/generated/pictures").asFile.absolutePath,
        rootProject.layout.projectDirectory.dir("jsesh-texts/src/main/assets/jsesh-texts").asFile.absolutePath,
    )
}

tasks.register("exportAll") {
    group = "jsesh-js"
    description = "Regenerates all data and fixtures used by jsesh-js."
    dependsOn(exportFixtures, exportGlyphData, exportSignInfo, exportPictures)
}
