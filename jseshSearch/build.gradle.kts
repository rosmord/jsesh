plugins {
    java
    `maven-publish`
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
