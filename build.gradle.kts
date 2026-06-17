description = "JSesh complete distribution"

//import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    base
}


tasks.named("clean") {
    dependsOn(subprojects.map { subproject ->
        subproject.tasks.matching { task -> task.name == "clean" }
    })
}


tasks.register("distClean") {
    dependsOn("clean", subprojects.map { subproject ->
        subproject.tasks.matching { task -> task.name == "distClean" }
    })
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
