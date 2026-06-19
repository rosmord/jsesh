import org.gradle.plugins.ide.eclipse.model.EclipseModel
//import org.gradle.api.artifacts.VersionCatalogsExtension

description = "JSesh complete distribution"


plugins {
    base
    id("eclipse")
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


allprojects {
    apply(plugin = "eclipse")

    configure<EclipseModel> {
        project {
            name = "${rootProject.name}-${project.name}"
        }
        classpath {
            isDownloadSources = true
            isDownloadJavadoc = false
        }
    }
}
