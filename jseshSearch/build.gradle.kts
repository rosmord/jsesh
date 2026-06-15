plugins {
    java
    `maven-publish`
}

dependencies {
    implementation(project(":jsesh"))
    implementation(project(":jseshLabels"))
    implementation(project(":qenherkhopeshefUtils"))
    implementation("com.miglayout:miglayout:3.7.4:swing")
    implementation("org.qenherkhopeshef:finitestate:2.0")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
