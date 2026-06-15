plugins {
    java
    `maven-publish`
}

dependencies {
    implementation(project(":jsesh"))
    implementation(project(":jseshGlyphs"))
    implementation(project(":jhotdrawfw"))
    implementation(project(":qenherkhopeshefUtils"))
    implementation(project(":jseshLabels"))
    implementation("com.miglayout:miglayout:3.7.4:swing")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "jsesh.utilitysoftwares.signinfoeditor.Main",
            "Class-Path" to configurations.runtimeClasspath.get().files.joinToString(" ") { it.name }
        )
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
