plugins {
    `java-gradle-plugin`
}

val javaToolchainVersion = providers.gradleProperty("javaToolchainVersion")
    .map(String::toInt)
    .orElse(21)
    .get()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaToolchainVersion))
    }
}

gradlePlugin {
    plugins {
        create("cupAndLex") {
            id = "org.qenherkhopeshef.jsesh.cupandlex"
            implementationClass = "org.qenherkhopeshef.jsesh.gradle.CupAndLexPlugin"
        }
    }
}
