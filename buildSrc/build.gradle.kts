plugins {
    `kotlin-dsl`    
}

repositories {
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        create("cupAndLex") {
            id = "org.qenherkhopeshef.jsesh.cupandlex"
            implementationClass = "org.qenherkhopeshef.jsesh.gradle.CupAndLexPlugin"
        }
    }
}
