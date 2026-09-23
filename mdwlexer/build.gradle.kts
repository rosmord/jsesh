description = "A library for code lexical analysis which performs directly in memory"

plugins {
    id("jsesh.java-conventions")
}

dependencies {
}


tasks.register<Test>("demo") {
    description = "Generates markdown files with Mermaid diagrams showcasing sample automata at each build stage."
    group = "documentation"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform {
        includeTags("demo")
    }
    outputs.upToDateWhen { false }
    doLast {
        println("Wrote demo diagrams to ${layout.projectDirectory.dir("build/demo-automata").asFile}")
    }
}