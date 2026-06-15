plugins {
    `java-gradle-plugin`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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
