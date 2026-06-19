plugins {
    `java`
    `java-library`
    `maven-publish`
    id("jsesh.common")
}


group = "org.qenherkhopeshef.jsesh"
version = rootProject.version


val javaToolchainVersion = providers.gradleProperty("javaToolchainVersion")
    .map(String::toInt)
    .get()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaToolchainVersion)
    }
}


tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:deprecation")
    options.compilerArgs.add("-Xlint:removal")    
}


tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
}


dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
        groupId = project.group.toString()
        version = project.version.toString()
    }

    val jseshMavenRepoUrl = System.getenv("JSESH_MAVEN_REPO_URL")
        ?.takeIf { it.isNotBlank() }

    repositories {
        if (jseshMavenRepoUrl != null) {
            maven {
                name = "JSesh"
                url = uri(jseshMavenRepoUrl)
                credentials {
                    username = System.getenv("JSESH_MAVEN_REPO_USERNAME")
                    password = System.getenv("JSESH_MAVEN_REPO_PASSWORD")
                }
            }
        }
    }
}
