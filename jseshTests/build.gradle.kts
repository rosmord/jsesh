description = "Integration tests for JSesh."

plugins {
    id("jsesh.java-conventions")
}

dependencies {
    implementation(project(":jsesh"))
    // The standard JSesh font: `jsesh` itself only embeds Gnutrace, and tests depending on the real signs live here.
    implementation(project(":jseshGlyphs"))
    implementation(project(":jseshSearch"))
    implementation(project(":qenherkhopeshefUtils"))
    implementation(libs.miglayout) {
        artifact {
            classifier = "swing"
        }
    }
    implementation(libs.jvectclipboard)
}
