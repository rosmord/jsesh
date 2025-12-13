plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenLocal()
    maven {
        url = uri("http://www.qenherkhopeshef.org/maven")
        isAllowInsecureProtocol = true
    }
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.1")
}

group = "org.qenherkhopeshef.jsesh"
version = "7.9.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
        groupId = "org.qenherkhopeshef.jsesh"
        version = project.version.toString()
    }

    repositories {
        maven {
            name = "JSesh"
            url = uri(System.getenv("JSESH_MAVEN_REPO_URL") ?: "")
            credentials {
                username = System.getenv("JSESH_MAVEN_REPO_USERNAME")
                password = System.getenv("JSESH_MAVEN_REPO_PASSWORD")
            }
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(11)
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}
