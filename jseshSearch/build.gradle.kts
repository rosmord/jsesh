description = """
    Advanced search functions for JSesh.
    We separate them from the original JSesh widget, as they require some external libraries.
""".trimIndent()

plugins {
    id("jsesh.java-conventions")
}

dependencies {
    implementation(project(":jsesh"))
    implementation(project(":jseshLabels"))
    implementation(project(":qenherkhopeshefUtils"))
    implementation(libs.miglayout) {
        artifact {
            classifier = "swing"
        }
    }
    implementation(libs.finitestate)
}
