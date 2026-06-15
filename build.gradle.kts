import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    base
}

val javaToolchainVersion = providers.gradleProperty("javaToolchainVersion")
    .map(String::toInt)
    .get()

allprojects {
    group = "org.qenherkhopeshef.jsesh"
    version = rootProject.version

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
        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

        configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(javaToolchainVersion))
            }
        }
        dependencies {
            add("testImplementation", libs.findLibrary("junit4").get())
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
