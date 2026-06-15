plugins {
    base
}

allprojects {
    group = "org.qenherkhopeshef.jsesh"
    version = "8.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
        maven {
            url = uri("http://www.qenherkhopeshef.org/maven")
            isAllowInsecureProtocol = true
        }
    }
}

subprojects {
    plugins.withId("java") {
        configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        }
        dependencies {
            "testImplementation"("junit:junit:4.13.1")
        }
    }
}

tasks.named("clean") {
    dependsOn(subprojects.map { subproject ->
        subproject.tasks.matching { task -> task.name == "clean" }
    })
}


tasks.register("distClean") {
    dependsOn("clean")
    doLast {
        delete("bin")
        delete(".classpath")
        delete(".gradle")
        delete(".nb-gradle")
        delete(".project")
        delete(".settings")
        delete(".vscode")
        delete(".DS_Store")
        delete(".idea")
    }
}
