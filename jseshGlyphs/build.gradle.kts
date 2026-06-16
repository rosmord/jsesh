plugins {
    id("jsesh.java-conventions")
}

dependencies {
    // jsesh provides ResourcesHieroglyphicShapeRepository used by the prepareFonts task
    implementation(project(":jsesh"))
}

// Font preparation: ResourcesHieroglyphicShapeRepository.main() indexes the compiled
// jseshGlyphs font files (the .svg resources under build/classes/java/main/jseshGlyphs).
// It already has a main() method so we invoke it directly via JavaExec.
val prepareFonts = tasks.register<JavaExec>("prepareFonts") {
    dependsOn(tasks.named("classes"))
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("jsesh.hieroglyphs.fonts.ResourcesHieroglyphicShapeRepository")
    // Resources land in build/resources/main in Gradle (unlike Maven's target/classes)
    args(layout.buildDirectory.dir("resources/main/jseshGlyphs").get().asFile.absolutePath)
    // Must run after classes are compiled so the font files are in the output dir
}

tasks.named("classes") {
    finalizedBy(prepareFonts)
}
