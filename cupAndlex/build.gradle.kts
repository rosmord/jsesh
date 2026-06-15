plugins {
    java
    `maven-publish`
}

val myMavenVersion = "3.9.11"

dependencies {
    // Maven API kept as compileOnly so the existing Maven Mojos (CupMojo/LexMojo) still compile.
    // These are not included in the published jar – they are provided by Maven at plugin runtime.
    compileOnly("org.apache.maven:maven-plugin-api:$myMavenVersion")
    compileOnly("org.apache.maven.plugin-tools:maven-plugin-annotations:3.15.2")
    compileOnly("org.apache.maven:maven-core:$myMavenVersion")
    compileOnly("org.codehaus.plexus:plexus-compiler-api:2.16.2")
    compileOnly("org.codehaus.plexus:plexus-build-api:1.2.0")
    compileOnly("org.codehaus.plexus:plexus-utils:4.0.3")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
