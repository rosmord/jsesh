plugins {
    java
    `maven-publish`
}

// JHotDraw stores .properties files alongside Java source; include them as resources.
tasks.processResources {
    from("src/main/java") {
        include("**/*.properties")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
