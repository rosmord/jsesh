plugins {
    java
    `maven-publish`
}

val myMavenVersion = "3.9.11"

dependencies {
    // Maven API kept compileOnly so PrepareFontsMojo still compiles
    compileOnly("org.apache.maven:maven-plugin-api:$myMavenVersion")
    compileOnly("org.apache.maven.plugin-tools:maven-plugin-annotations:3.15.2")

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
