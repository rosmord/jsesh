description = "An application to edit signs properties."

plugins {
    id("jsesh.java-conventions")
}

dependencies {
    implementation(project(":jsesh"))
    implementation(project(":jseshGlyphs"))
    implementation(project(":jhotdrawfw"))
    implementation(project(":qenherkhopeshefUtils"))
    implementation(project(":jseshLabels"))
    implementation(libs.miglayout) {
        artifact {
            classifier = "swing"
        }
    }
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "jsesh.utilitysoftwares.signinfoeditor.Main",
            "Class-Path" to configurations.runtimeClasspath.get().files.joinToString(" ") { it.name }
        )
    }
}
