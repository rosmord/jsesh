
repositories {
    //mavenLocal()
    //maven {
    //    url = uri("https://www.qenherkhopeshef.org/maven")
    //}
    // Uncomment the following line
    // as a temporary workaround when developping the 
    // other libraries (e.g. jvectclipboard).
    // It's important that JSesh can compile without them.
    // mavenLocal() 
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