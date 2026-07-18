description = "An application to edit signs properties."

plugins {
    id("jsesh.java-conventions")
    application
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


application {
    mainClass.set("jsesh.utilitysoftwares.signinfoeditor.Main")
    applicationName = "JSesh SignInfoEditor"
    applicationDefaultJvmArgs = listOf("-Xmx2G")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to application.mainClass,
            "Class-Path" to configurations.runtimeClasspath.get().files.joinToString(" ") { it.name }
        )
    }
}
