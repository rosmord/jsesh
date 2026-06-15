plugins {
    java
    `maven-publish`
}

dependencies {
    // Maven API kept as compileOnly so the existing Maven Mojos (CupMojo/LexMojo) still compile.
    // These are not included in the published jar – they are provided by Maven at plugin runtime.
    compileOnly(libs.mavenPluginApi)
    compileOnly(libs.mavenPluginAnnotations)
    compileOnly(libs.mavenCore)
    compileOnly(libs.plexusCompilerApi)
    compileOnly(libs.plexusBuildApi)
    compileOnly(libs.plexusUtils)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
