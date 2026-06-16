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
