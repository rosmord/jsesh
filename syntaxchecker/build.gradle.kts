description = "An application to find JSesh files with syntax errors."

var myMain = "jsesh.syntaxcheckerutil.Main"

plugins {
    id("jsesh.java-conventions")
    application
}

dependencies {
    implementation(project(":jsesh"))
    implementation(project(":qenherkhopeshefUtils"))
}


application {
    mainClass = myMain
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to myMain,
            "Class-Path" to configurations.runtimeClasspath.get().files.joinToString(" ") { it.name }
        )
    }
}
