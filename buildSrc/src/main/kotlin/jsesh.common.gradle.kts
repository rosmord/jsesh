
repositories {
    mavenLocal()
    maven {
        url = uri("https://www.qenherkhopeshef.org/maven")
    }
    mavenCentral()
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