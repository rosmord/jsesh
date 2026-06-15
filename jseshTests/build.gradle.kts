plugins {
    java
}

dependencies {
    implementation(project(":jsesh"))
    implementation(project(":qenherkhopeshefUtils"))
    implementation(libs.miglayout) {
        artifact {
            classifier = "swing"
        }
    }
    implementation(libs.jvectclipboard)
}
