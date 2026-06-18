description = "The JSesh application, using jhotdraw as GUI framework."

plugins {
    id("jsesh.java-conventions")
}

dependencies {
    implementation(project(":jsesh"))
    implementation(project(":jseshGlyphs"))
    implementation(project(":jhotdrawfw"))
    implementation(project(":jseshSearch"))
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
            "Main-Class" to "jsesh.jhotdraw.Main",
            "Class-Path" to configurations.runtimeClasspath.get().files.joinToString(" ") { it.name }
        )
    }
}
