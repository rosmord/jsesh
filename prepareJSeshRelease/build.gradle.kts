plugins {
    java
    `maven-publish`
}

dependencies {
    // Maven API kept compileOnly so PrepareFontsMojo still compiles
    compileOnly(libs.mavenPluginApi)
    compileOnly(libs.mavenPluginAnnotations)

    // The font preparation logic uses jsesh at runtime
    implementation(project(":jsesh"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
